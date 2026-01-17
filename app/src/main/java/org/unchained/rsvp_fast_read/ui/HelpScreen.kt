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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Instructions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            HelpSection(title = "Controls & Gestures") {
                HelpItem("Play / Pause", "Tap the center of the screen during reading or use hardware media buttons.")
                HelpItem("Adjust Speed (WPM)", "Vertical swipe on the right half of the screen. High speeds (>500 WPM) automatically deactivate TTS for synchronization.")
                HelpItem("Adjust Font Size", "Vertical swipe on the left half of the screen.")
                HelpItem("Rewind / Skip", "Buttons appear when playback is paused to move 10 words back or forward.")
            }

            HelpSection(title = "Reading Features") {
                HelpItem("TTS Voice Output", "Listen to words as they flash on screen. Uses your device's built-in offline TTS engine. Toggle this in Settings.")
                HelpItem("ORP (Optimal Recognition Point)", "A colored visual anchor in the center of words to minimize eye movement. Customize the color in Settings.")
                HelpItem("Bionic Reading", "Bolds the beginning of words to help your eyes 'anchor' faster on text.")
                HelpItem("Contextual Hybrid View", "Shows a faint background of the surrounding text behind the active RSVP word for spatial awareness.")
                HelpItem("Smart Pause", "Automatically adds extra processing time after long sentences (>25 words).")
                HelpItem("Warm-up & Countdown", "Starts playback slowly and ramps up to your target speed. Includes an optional 3-2-1 countdown.")
                HelpItem("Punctuation Delays", "Adds natural pauses at periods, commas, and paragraph breaks.")
            }

            HelpSection(title = "Navigation & Utility") {
                HelpItem("Tap-to-Resume", "When paused, scroll through the full text and tap any word to resume reading from that exact spot.")
                HelpItem("Split View", "Shows both the high-speed RSVP word and the scrollable context view simultaneously.")
                HelpItem("Chapters & Bookmarks", "Navigate using detected document chapters or save your own manual bookmarks.")
                HelpItem("Clipboard & Share", "Import text directly from your clipboard or share files from other apps to start reading immediately.")
            }

            HelpSection(title = "Productivity") {
                HelpItem("Goals & Streaks", "Track your daily word count and reading time locally. View your progress dashboard in the Library.")
                HelpItem("Focus Session Timer", "Set a timer (e.g., 25 min) to automatically pause and remind you to take a break.")
            }

            HelpSection(title = "Privacy & Licensing") {
                Text("RSVP Fast Read is 100% offline. It requires zero internet permissions. Your documents, reading statistics, and habits never leave your device.", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("This application is built exclusively using open-source and free-of-charge components, ensuring transparency and respect for user freedom.", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HelpSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        content()
        Spacer(modifier = Modifier.height(8.dp))
        Divider()
    }
}

@Composable
fun HelpItem(label: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Text(text = description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
