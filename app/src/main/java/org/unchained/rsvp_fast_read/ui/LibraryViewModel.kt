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

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.unchained.rsvp_fast_read.data.DailyStats
import org.unchained.rsvp_fast_read.data.Document
import org.unchained.rsvp_fast_read.data.DocumentRepository
import java.text.SimpleDateFormat
import java.util.*

class LibraryViewModel(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFolder = MutableStateFlow<String?>(null)
    val selectedFolder: StateFlow<String?> = _selectedFolder

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting

    val documents: Flow<List<Document>> = combine(
        repository.allDocuments,
        _searchQuery,
        _selectedFolder
    ) { docs, query, folder ->
        docs.filter { doc ->
            (query.isEmpty() || doc.title.contains(query, ignoreCase = true)) &&
            (folder == null || doc.folderName == folder)
        }
    }

    val folders: Flow<List<String>> = repository.allFolders

    val weeklyStats: Flow<List<DailyStats>> = repository.getWeeklyStats()

    val todayStats: Flow<DailyStats?> = repository.getWeeklyStats().map { stats ->
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        stats.find { it.date == today }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedFolder(folder: String?) {
        _selectedFolder.value = folder
    }

    fun addDocument(uri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                repository.addDocument(uri, _selectedFolder.value ?: "Default")
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun addDocumentFromClipboard(text: String) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                repository.addTextFromClipboard(text)
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun deleteDocument(document: Document) {
        viewModelScope.launch {
            repository.deleteDocument(document)
        }
    }
}
