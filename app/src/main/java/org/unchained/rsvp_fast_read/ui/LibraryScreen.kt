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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.unchained.rsvp_fast_read.data.DailyStats
import org.unchained.rsvp_fast_read.data.Document
import org.unchained.rsvp_fast_read.data.UserSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    userSettings: UserSettings,
    onDocumentClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    val documents by viewModel.documents.collectAsState(initial = emptyList())
    val folders by viewModel.folders.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val todayStats by viewModel.todayStats.collectAsState(initial = null)
    val isImporting by viewModel.isImporting.collectAsState()
    
    val clipboardManager = LocalClipboardManager.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { viewModel.addDocument(it) }
        }
    )

    if (isImporting) {
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Importing document...", fontWeight = FontWeight.Bold)
                    Text("Please wait while we extract the text.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Library") },
                    actions = {
                        IconButton(
                            enabled = !isImporting,
                            onClick = {
                                clipboardManager.getText()?.text?.let { text ->
                                    viewModel.addDocumentFromClipboard(text)
                                }
                            }
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Read from Clipboard")
                        }
                        IconButton(onClick = onHelpClick) {
                            Icon(Icons.Default.Info, contentDescription = "Help")
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
                
                if (userSettings.readingGoalsEnabled) {
                    StatsDashboard(todayStats)
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search documents...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )
                LazyRow(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedFolder == null,
                            onClick = { viewModel.setSelectedFolder(null) },
                            label = { Text("All") }
                        )
                    }
                    items(folders) { folder ->
                        FilterChip(
                            selected = selectedFolder == folder,
                            onClick = { viewModel.setSelectedFolder(folder) },
                            label = { Text(folder) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!isImporting) {
                        filePickerLauncher.launch(arrayOf("application/pdf", "application/epub+zip", "text/plain", "text/html"))
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Document")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(documents) { doc ->
                DocumentItem(
                    document = doc,
                    onClick = { if (!isImporting) onDocumentClick(doc.id) },
                    onDelete = { if (!isImporting) viewModel.deleteDocument(doc) }
                )
            }
        }
    }
}

@Composable
fun StatsDashboard(stats: DailyStats?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Today's Progress", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "${stats?.wordsRead ?: 0}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Red)
                    Text(text = "Words Read", fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "${stats?.minutesRead ?: 0}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Minutes Spent", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            val progress = (stats?.minutesRead?.toFloat() ?: 0f) / 20f
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = Color.Red,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun DocumentItem(
    document: Document,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = { Text(document.title) },
        supportingContent = {
            Text("${document.fileType} | ${document.totalWords} words | Progress: ${document.lastReadPosition}")
        },
        modifier = Modifier.clickable { onClick() },
        trailingContent = {
            TextButton(onClick = onDelete) {
                Text("Delete", color = Color.Red)
            }
        }
    )
}
