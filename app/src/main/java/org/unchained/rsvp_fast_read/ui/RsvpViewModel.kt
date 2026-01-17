/*
 * Copyright (C) 2023 Unchained
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.unchained.rsvp_fast_read.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.unchained.rsvp_fast_read.data.Bookmark
import org.unchained.rsvp_fast_read.data.Document
import org.unchained.rsvp_fast_read.data.DocumentRepository
import org.unchained.rsvp_fast_read.data.UserSettingsRepository
import org.unchained.rsvp_fast_read.extraction.Chapter
import org.unchained.rsvp_fast_read.speech.TtsManager
import java.text.BreakIterator
import java.text.SimpleDateFormat
import java.util.*

class RsvpViewModel(
    private val documentRepository: DocumentRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val ttsManager: TtsManager
) : ViewModel() {

    private val _words = MutableStateFlow<List<String>>(emptyList())
    private val _currentIndex = MutableStateFlow(0)
    private val _isPlaying = MutableStateFlow(false)
    private val _currentWord = MutableStateFlow("")
    private val _orpIndex = MutableStateFlow(0)
    private val _countdown = MutableStateFlow<Int?>(null)
    private val _isFinished = MutableStateFlow(false)
    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    private val _focusTimerRemaining = MutableStateFlow<Long?>(null)

    val currentWord: StateFlow<String> = _currentWord
    val orpIndex: StateFlow<Int> = _orpIndex
    val isPlaying: StateFlow<Boolean> = _isPlaying
    val currentIndex: StateFlow<Int> = _currentIndex
    val totalWords: StateFlow<Int> = _words.map { it.size }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val countdown: StateFlow<Int?> = _countdown
    val isFinished: StateFlow<Boolean> = _isFinished
    val chapters: StateFlow<List<Chapter>> = _chapters
    val focusTimerRemaining: StateFlow<Long?> = _focusTimerRemaining
    val words: StateFlow<List<String>> = _words

    private var playbackJob: Job? = null
    private var timerJob: Job? = null
    private var currentDocument: Document? = null
    
    private var sessionStartTime: Long = 0
    private var totalSessionTimeMs: Long = 0
    private var wordsInCurrentSentence = 0
    private var wordsReadInSession = 0

    val bookmarks: StateFlow<List<Bookmark>> = _currentIndex.flatMapLatest {
        val docId = currentDocument?.id ?: -1L
        documentRepository.getBookmarksForDocument(docId)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchResults = MutableStateFlow<List<Int>>(emptyList())
    val searchResults: StateFlow<List<Int>> = _searchResults

    fun loadDocument(documentId: Long) {
        viewModelScope.launch {
            val doc = documentRepository.getDocumentById(documentId) ?: return@launch
            currentDocument = doc
            val content = documentRepository.getDocumentContent(doc)
            val normalizedText = normalizeText(content.text)
            val processedWords = processText(normalizedText, doc.language)
            _words.value = processedWords
            _chapters.value = content.chapters
            _currentIndex.value = doc.lastReadPosition.coerceIn(0, (processedWords.size - 1).coerceAtLeast(0))
            _isFinished.value = false
            updateWordDisplay()
        }
    }

    private fun normalizeText(text: String): String {
        val urlRegex = Regex("(https?://|www\\.)[\\w\\d\\.#@/\\?%&~=\\-]*", RegexOption.IGNORE_CASE)
        var result = text.replace(urlRegex, "[URL]")
        result = result.replace(Regex("[^\\w\\s\\p{Punct}a-zA-Z0-9\\u4e00-\\u9fa5\\u3040-\\u309f\\u30a0-\\u30ff]"), "")
        return result.replace(Regex("\\s+"), " ").trim()
    }

    private fun processText(text: String, language: String): List<String> {
        val rawWords = if (isCjk(language)) {
            segmentCjkText(text, language)
        } else {
            text.split(Regex("\\s+")).filter { it.isNotBlank() }
        }

        val finalWords = mutableListOf<String>()
        for (word in rawWords) {
            if (word.length > 13) {
                val mid = word.length / 2
                finalWords.add(word.substring(0, mid) + "-")
                finalWords.add(word.substring(mid))
            } else {
                finalWords.add(word)
            }
        }
        return finalWords
    }

    private fun isCjk(language: String): Boolean {
        val lang = language.lowercase()
        return lang.startsWith("zh") || lang.startsWith("ja") || lang.startsWith("ko")
    }

    private fun segmentCjkText(text: String, language: String): List<String> {
        val locale = Locale.forLanguageTag(language)
        val boundary = BreakIterator.getWordInstance(locale)
        boundary.setText(text)
        val segments = mutableListOf<String>()
        var start = boundary.first()
        var end = boundary.next()
        while (end != BreakIterator.DONE) {
            val word = text.substring(start, end).trim()
            if (word.isNotEmpty()) {
                segments.add(word)
            }
            start = end
            end = boundary.next()
        }
        return segments
    }

    fun togglePlayback() {
        if (_isPlaying.value) {
            pause()
        } else {
            startCountdownAndPlay()
        }
    }

    fun resumeAt(index: Int) {
        _currentIndex.value = index.coerceIn(0, (_words.value.size - 1).coerceAtLeast(0))
        updateWordDisplay()
        _isFinished.value = false
        startCountdownAndPlay()
    }

    private fun startCountdownAndPlay() {
        if (_isFinished.value) {
            reRead()
            return
        }
        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val settings = userSettingsRepository.userSettingsFlow.first()
            
            if (settings.warmupEnabled) {
                for (i in 3 downTo 1) {
                    _countdown.value = i
                    delay(1000)
                }
                _countdown.value = null
            }
            
            startFocusTimer(settings)
            runPlaybackLoop()
        }
    }

    private fun startFocusTimer(settings: org.unchained.rsvp_fast_read.data.UserSettings) {
        if (!settings.focusTimerEnabled) return
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remaining = settings.focusTimerMinutes * 60 * 1000L
            while (remaining > 0 && _isPlaying.value) {
                _focusTimerRemaining.value = remaining
                delay(1000)
                remaining -= 1000
            }
            if (remaining <= 0) {
                pause()
                _focusTimerRemaining.value = 0
            }
        }
    }

    private suspend fun runPlaybackLoop() {
        sessionStartTime = System.currentTimeMillis()
        val settingsFlow = userSettingsRepository.userSettingsFlow
        val initialSettings = settingsFlow.first()
        var currentWpm = if (initialSettings.warmupEnabled) initialSettings.wpm / 2 else initialSettings.wpm

        while (currentCoroutineContext().isActive && _currentIndex.value < _words.value.size) {
            val currentSettings = settingsFlow.first()
            updateWordDisplay()
            
            val word = _words.value[_currentIndex.value]
            
            if (currentSettings.ttsEnabled) {
                ttsManager.speak(word)
            }

            val baseDelay = 60000L / currentWpm
            var multiplier = if (currentSettings.punctuationDelays) {
                when {
                    word.endsWith(".") || word.endsWith("?") || word.endsWith("!") || word.endsWith(":") || word.endsWith("\n") -> 2.0
                    word.endsWith(",") || word.endsWith(";") -> 1.5
                    else -> 1.0
                }
            } else 1.0

            if (currentSettings.smartPauseEnabled) {
                wordsInCurrentSentence++
                if (word.endsWith(".") || word.endsWith("?") || word.endsWith("!")) {
                    if (wordsInCurrentSentence > 25) {
                        multiplier *= 1.5
                    }
                    wordsInCurrentSentence = 0
                }
            }

            delay((baseDelay * multiplier).toLong())

            if (currentSettings.warmupEnabled) {
                if (currentWpm < currentSettings.wpm) {
                    currentWpm = (currentWpm + 10).coerceAtMost(currentSettings.wpm)
                } else if (currentWpm > currentSettings.wpm) {
                    currentWpm = currentSettings.wpm
                }
            } else {
                currentWpm = currentSettings.wpm
            }

            _currentIndex.value++
            wordsReadInSession++
            saveProgress()
        }
        
        if (_currentIndex.value >= _words.value.size) {
            _isPlaying.value = false
            _isFinished.value = true
            totalSessionTimeMs += System.currentTimeMillis() - sessionStartTime
            saveStats()
        }
    }

    fun pause() {
        if (_isPlaying.value) {
            totalSessionTimeMs += System.currentTimeMillis() - sessionStartTime
            saveStats()
        }
        _isPlaying.value = false
        _countdown.value = null
        playbackJob?.cancel()
        timerJob?.cancel()
        ttsManager.stop()
        saveProgress()
    }

    private fun saveStats() {
        viewModelScope.launch {
            val settings = userSettingsRepository.userSettingsFlow.first()
            if (!settings.readingGoalsEnabled) return@launch
            
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val minutes = (totalSessionTimeMs / 60000).toInt()
            documentRepository.incrementDailyStats(date, wordsReadInSession, minutes)
            wordsReadInSession = 0
            totalSessionTimeMs = 0
        }
    }

    fun rewind() {
        seekTo(_currentIndex.value - 10)
    }

    fun skip() {
        seekTo(_currentIndex.value + 10)
    }

    fun reRead() {
        _isFinished.value = false
        totalSessionTimeMs = 0
        wordsReadInSession = 0
        seekTo(0)
        startCountdownAndPlay()
    }

    private fun updateWordDisplay() {
        if (_currentIndex.value < _words.value.size) {
            val word = _words.value[_currentIndex.value]
            _currentWord.value = word
            _orpIndex.value = calculateOrpIndex(word)
        }
    }

    private fun calculateOrpIndex(word: String): Int {
        if (word.isEmpty()) return 0
        return word.length / 2
    }

    private fun saveProgress() {
        val doc = currentDocument ?: return
        viewModelScope.launch {
            documentRepository.updateProgress(doc, _currentIndex.value)
        }
    }

    fun seekTo(index: Int) {
        _currentIndex.value = index.coerceIn(0, (_words.value.size - 1).coerceAtLeast(0))
        updateWordDisplay()
        saveProgress()
    }

    fun getSessionStats(): SessionStats {
        val totalWordsRead = _currentIndex.value
        val timeInMinutes = totalSessionTimeMs / 60000.0
        val avgWpm = if (timeInMinutes > 0) (totalWordsRead / timeInMinutes).toInt() else 0
        return SessionStats(
            totalTimeMs = totalSessionTimeMs,
            avgWpm = avgWpm,
            wordCount = _words.value.size
        )
    }

    fun addBookmark() {
        val doc = currentDocument ?: return
        viewModelScope.launch {
            val label = "Position ${_currentIndex.value}"
            documentRepository.addBookmark(Bookmark(documentId = doc.id, position = _currentIndex.value, label = label))
        }
    }

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            documentRepository.deleteBookmark(bookmark)
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch(Dispatchers.Default) {
            val results = mutableListOf<Int>()
            _words.value.forEachIndexed { index, word ->
                if (word.contains(query, ignoreCase = true)) {
                    results.add(index)
                }
            }
            _searchResults.value = results
        }
    }

    fun jumpToSearchResult(index: Int) {
        val results = _searchResults.value
        if (index in results.indices) {
            seekTo(results[index])
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}

data class SessionStats(
    val totalTimeMs: Long,
    val avgWpm: Int,
    val wordCount: Int
)
