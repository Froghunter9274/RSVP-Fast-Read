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

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import org.unchained.rsvp_fast_read.data.Bookmark
import org.unchained.rsvp_fast_read.data.UserSettings
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RsvpScreen(
    viewModel: RsvpViewModel,
    userSettings: UserSettings,
    onBack: () -> Unit,
    onUpdateWpm: (Int) -> Unit,
    onUpdateFontSize: (Int) -> Unit
) {
    val word by viewModel.currentWord.collectAsState()
    val orpIndex by viewModel.orpIndex.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val totalWords by viewModel.totalWords.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val focusTimerRemaining by viewModel.focusTimerRemaining.collectAsState()
    val words by viewModel.words.collectAsState()

    var overlayValue by remember { mutableStateOf<String?>(null) }
    var overlayId by remember { mutableStateOf(0L) }
    
    var showBookmarks by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showChapters by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val listState = rememberLazyListState()

    val textColor = if (userSettings.isDarkMode) Color.White else Color.Black
    val backgroundColor = if (userSettings.isDarkMode) Color.Black else Color.White

    val context = LocalContext.current
    val view = LocalView.current
    
    // Logic to hide overlay after timeout
    LaunchedEffect(overlayId) {
        if (overlayValue != null) {
            delay(1500)
            overlayValue = null
        }
    }

    // Status Bar logic
    SideEffect {
        val window = (context as? Activity)?.window
        if (window != null) {
            window.statusBarColor = backgroundColor.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !userSettings.isDarkMode
        }
    }

    // Keep Screen On logic
    DisposableEffect(isPlaying, countdown) {
        val window = (context as? Activity)?.window
        val shouldKeepOn = isPlaying || countdown != null
        if (shouldKeepOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val selectedFontFamily = when (userSettings.fontFamily) {
        "Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        "OpenDyslexic" -> {
            val fontId = context.resources.getIdentifier("opendyslexic_regular", "font", context.packageName)
            if (fontId != 0) {
                try {
                    FontFamily(Font(fontId))
                } catch (e: Exception) {
                    FontFamily.Default
                }
            } else {
                FontFamily.Default
            }
        }
        else -> FontFamily.SansSerif
    }

    // Scroll list to current position
    LaunchedEffect(currentIndex) {
        if (words.isNotEmpty() && (userSettings.splitViewEnabled || (!isPlaying && userSettings.tapToResumeEnabled))) {
            listState.animateScrollToItem(currentIndex)
        }
    }

    val isScrollMode = !isPlaying && userSettings.tapToResumeEnabled && countdown == null && !isFinished

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .then(
                if (!isScrollMode) {
                    Modifier
                        .pointerInput(userSettings) {
                            detectTapGestures(
                                onTap = { viewModel.togglePlayback() }
                            )
                        }
                        .pointerInput(userSettings) {
                            var accumulatedDrag = 0f
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    accumulatedDrag = 0f
                                }
                            ) { change, dragAmount ->
                                accumulatedDrag += dragAmount
                                val isLeftHalf = change.position.x < size.width / 2
                                
                                if (isLeftHalf) {
                                    if (kotlin.math.abs(accumulatedDrag) > 20f) {
                                        val steps = (accumulatedDrag / 20f).roundToInt()
                                        val newSize = (userSettings.fontSize - (steps * 4)).coerceIn(16, 72)
                                        if (newSize != userSettings.fontSize) {
                                            onUpdateFontSize(newSize)
                                            accumulatedDrag = 0f
                                        }
                                        overlayValue = "$newSize sp"
                                        overlayId = System.nanoTime()
                                    }
                                } else {
                                    if (kotlin.math.abs(accumulatedDrag) > 10f) {
                                        val steps = (accumulatedDrag / 10f).roundToInt()
                                        val newWpm = (userSettings.wpm - (steps * 10)).coerceIn(100, 1000)
                                        if (newWpm != userSettings.wpm) {
                                            onUpdateWpm(newWpm)
                                            accumulatedDrag = 0f
                                        }
                                        overlayValue = "$newWpm WPM"
                                        overlayId = System.nanoTime()
                                    }
                                }
                            }
                        }
                } else Modifier
            )
    ) {
        // Contextual Hybrid View
        if (userSettings.contextualHybridEnabled && isPlaying && !userSettings.splitViewEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                val start = (currentIndex - 20).coerceAtLeast(0)
                val end = (currentIndex + 20).coerceAtMost(words.size - 1)
                if (start <= end) {
                    val contextText = words.slice(start..end).joinToString(" ")
                    Text(
                        text = contextText,
                        color = textColor.copy(alpha = 0.1f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Overlay for gesture values
        if (overlayValue != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = CircleShape
                ) {
                    Text(
                        text = overlayValue!!,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Focus Timer Display
        if (userSettings.focusTimerEnabled && focusTimerRemaining != null && isPlaying) {
            val minutes = focusTimerRemaining!! / 60000
            val seconds = (focusTimerRemaining!! % 60000) / 1000
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                color = textColor.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                fontSize = 14.sp
            )
        }

        // Top Bar
        if (!isPlaying && !isFinished) {
            TopAppBar(
                title = { Text("Reader", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                actions = {
                    IconButton(onClick = { showChapters = true }) {
                        Icon(Icons.Default.List, contentDescription = "Chapters", tint = textColor)
                    }
                    IconButton(onClick = { showSearch = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = textColor)
                    }
                    IconButton(onClick = { viewModel.addBookmark() }) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = "Add Bookmark", tint = textColor)
                    }
                    IconButton(onClick = { showBookmarks = true }) {
                        Icon(Icons.Default.Bookmarks, contentDescription = "Show Bookmarks", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.zIndex(1f)
            )
        }

        // Focus Mask
        if (userSettings.focusMaskEnabled && isPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
        }

        // Main Content Area
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isFinished) {
                FinishedSummary(
                    stats = viewModel.getSessionStats(),
                    textColor = textColor,
                    onReRead = { viewModel.reRead() },
                    onReturn = onBack
                )
            } else if (countdown != null) {
                Text(
                    text = countdown.toString(),
                    fontSize = 72.sp,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            } else if (userSettings.splitViewEnabled) {
                // Split View Layout
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        RsvpWordDisplay(
                            word = word,
                            orpIndex = orpIndex,
                            fontSize = userSettings.fontSize,
                            textColor = textColor,
                            fontFamily = selectedFontFamily,
                            orpEnabled = userSettings.orpEnabled,
                            orpColor = Color(userSettings.orpColor),
                            bionicEnabled = userSettings.bionicReadingEnabled
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(textColor.copy(alpha = 0.05f))
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(24.dp)
                        ) {
                            items(words.indices.toList()) { index ->
                                Text(
                                    text = words[index],
                                    color = if (index == currentIndex) Color.Red else textColor.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            viewModel.seekTo(index)
                                        }
                                        .padding(vertical = 4.dp),
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            } else if (isScrollMode) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 100.dp, bottom = 200.dp, start = 24.dp, end = 24.dp)
                ) {
                    items(words.indices.toList()) { index ->
                        Text(
                            text = words[index],
                            color = if (index == currentIndex) Color.Red else textColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    viewModel.resumeAt(index)
                                }
                                .padding(vertical = 12.dp),
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            } else {
                RsvpWordDisplay(
                    word = word,
                    orpIndex = orpIndex,
                    fontSize = userSettings.fontSize,
                    textColor = textColor,
                    fontFamily = selectedFontFamily,
                    orpEnabled = userSettings.orpEnabled,
                    orpColor = Color(userSettings.orpColor),
                    bionicEnabled = userSettings.bionicReadingEnabled
                )
            }
        }

        // Controls (Bottom Bar when paused)
        if (!isPlaying && countdown == null && !isFinished) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
                    .fillMaxWidth()
                    .background(backgroundColor.copy(alpha = 0.8f)),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.rewind() }) {
                    Icon(Icons.Default.FastRewind, contentDescription = "Rewind 10", tint = textColor, modifier = Modifier.size(48.dp))
                }
                
                FloatingActionButton(
                    onClick = { viewModel.togglePlayback() },
                    containerColor = Color.Red,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(36.dp))
                }

                IconButton(onClick = { viewModel.skip() }) {
                    Icon(Icons.Default.FastForward, contentDescription = "Skip 10", tint = textColor, modifier = Modifier.size(48.dp))
                }
            }
        }

        // Bottom Progress Bar
        if (!isPlaying && !isFinished) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                val progress = if (totalWords > 0) currentIndex.toFloat() / totalWords else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Red,
                    trackColor = textColor.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "${(progress * 100).toInt()}%", color = textColor, fontSize = 12.sp)
                    val remainingWords = (totalWords - currentIndex).coerceAtLeast(0)
                    val minutesRemaining = remainingWords / userSettings.wpm
                    Text(text = "~$minutesRemaining min left", color = textColor, fontSize = 12.sp)
                }
            }
        }

        // Dialogs...
        if (showSearch) {
            AlertDialog(
                onDismissRequest = { showSearch = false },
                title = { Text("Search Document") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { 
                                searchQuery = it
                                viewModel.search(it)
                            },
                            label = { Text("Search for phrase...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.heightIn(max = 200.dp)) {
                            if (searchResults.isEmpty() && searchQuery.isNotBlank()) {
                                Text("No results found.")
                            } else {
                                LazyColumn {
                                    items(searchResults.indices.toList()) { resultIndex ->
                                        val wordIndex = searchResults[resultIndex]
                                        ListItem(
                                            headlineContent = { Text("Result at word $wordIndex") },
                                            modifier = Modifier.clickable {
                                                viewModel.jumpToSearchResult(resultIndex)
                                                showSearch = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSearch = false }) {
                        Text("Close")
                    }
                }
            )
        }

        if (showChapters) {
            AlertDialog(
                onDismissRequest = { showChapters = false },
                title = { Text("Chapters") },
                text = {
                    Box(modifier = Modifier.heightIn(max = 300.dp)) {
                        if (chapters.isEmpty()) {
                            Text("No chapters detected.")
                        } else {
                            LazyColumn {
                                items(chapters) { chapter ->
                                    ListItem(
                                        headlineContent = { Text(chapter.title) },
                                        supportingContent = { Text("Word ${chapter.startIndex}") },
                                        modifier = Modifier.clickable {
                                            viewModel.seekTo(chapter.startIndex)
                                            showChapters = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showChapters = false }) {
                        Text("Close")
                    }
                }
            )
        }

        if (showBookmarks) {
            AlertDialog(
                onDismissRequest = { showBookmarks = false },
                title = { Text("Bookmarks") },
                text = {
                    Box(modifier = Modifier.heightIn(max = 300.dp)) {
                        if (bookmarks.isEmpty()) {
                            Text("No bookmarks saved.")
                        } else {
                            LazyColumn {
                                items(bookmarks) { bookmark ->
                                    ListItem(
                                        headlineContent = { Text(bookmark.label) },
                                        trailingContent = {
                                            IconButton(onClick = { viewModel.deleteBookmark(bookmark) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                            }
                                        },
                                        modifier = Modifier.clickable {
                                            viewModel.seekTo(bookmark.position)
                                            showBookmarks = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBookmarks = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun RsvpWordDisplay(
    word: String,
    orpIndex: Int,
    fontSize: Int,
    textColor: Color,
    fontFamily: FontFamily,
    orpEnabled: Boolean,
    orpColor: Color,
    bionicEnabled: Boolean
) {
    if (word.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            val leftText = word.substring(0, orpIndex)
            val annotatedLeft = buildAnnotatedString {
                if (bionicEnabled) {
                    val half = (leftText.length + 1) / 2
                    withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                        append(leftText.substring(0, half))
                    }
                    append(leftText.substring(half))
                } else {
                    append(leftText)
                }
            }
            Text(
                text = annotatedLeft,
                fontSize = fontSize.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal, 
                color = textColor,
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }

        Text(
            text = word[orpIndex].toString(),
            fontSize = fontSize.sp,
            fontFamily = fontFamily,
            fontWeight = if (bionicEnabled || orpEnabled) FontWeight.ExtraBold else FontWeight.Normal,
            color = if (orpEnabled) orpColor else textColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            Text(
                text = word.substring(orpIndex + 1),
                fontSize = fontSize.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Start,
                maxLines = 1
            )
        }
    }
}

@Composable
fun FinishedSummary(
    stats: SessionStats,
    textColor: Color,
    onReRead: () -> Unit,
    onReturn: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Text("Finished!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = textColor)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Total words: ${stats.wordCount}", color = textColor)
        Text("Average speed: ${stats.avgWpm} WPM", color = textColor)
        val minutes = stats.totalTimeMs / 60000
        val seconds = (stats.totalTimeMs % 60000) / 1000
        Text("Reading time: $minutes min $seconds sec", color = textColor)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onReRead, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
            Text("Re-read", color = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onReturn) {
            Text("Return to Library", color = textColor)
        }
    }
}
