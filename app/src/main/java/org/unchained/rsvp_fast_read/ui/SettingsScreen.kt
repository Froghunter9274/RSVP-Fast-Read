package org.unchained.rsvp_fast_read.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.unchained.rsvp_fast_read.BuildConfig
import org.unchained.rsvp_fast_read.data.UserSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: UserSettings,
    onBack: () -> Unit,
    onUpdateSettings: (UserSettings) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Appearance", style = MaterialTheme.typography.titleMedium)
                
                ListItem(
                    headlineContent = { Text("Dark Mode") },
                    trailingContent = {
                        Switch(
                            checked = settings.isDarkMode,
                            onCheckedChange = { onUpdateSettings(settings.copy(isDarkMode = it)) }
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("Custom ORP Color") },
                    trailingContent = {
                        val colors = listOf(Color.Red, Color.Green, Color.Cyan, Color.Yellow, Color.Magenta)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            colors.forEach { color ->
                                val isSelected = settings.orpColor == color.toArgb()
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(
                                                    width = 3.dp,
                                                    color = if (settings.isDarkMode) Color.White else Color.Black,
                                                    shape = CircleShape
                                                )
                                            } else Modifier
                                        )
                                        .clickable { onUpdateSettings(settings.copy(orpColor = color.toArgb())) }
                                )
                            }
                        }
                    }
                )

                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Advanced Reading", style = MaterialTheme.typography.titleMedium)

                ListItem(
                    headlineContent = { Text("Split View Reader") },
                    supportingContent = { Text("Simultaneous RSVP and Scroll view") },
                    trailingContent = {
                        Switch(
                            checked = settings.splitViewEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(splitViewEnabled = it)) }
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("Bionic Reading") },
                    supportingContent = { Text("Bold the beginning of words") },
                    trailingContent = {
                        Switch(
                            checked = settings.bionicReadingEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(bionicReadingEnabled = it)) }
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("Contextual Hybrid View") },
                    supportingContent = { Text("Show faint background text") },
                    trailingContent = {
                        Switch(
                            checked = settings.contextualHybridEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(contextualHybridEnabled = it)) }
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("Smart Pause") },
                    supportingContent = { Text("Longer pause on long sentences") },
                    trailingContent = {
                        Switch(
                            checked = settings.smartPauseEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(smartPauseEnabled = it)) }
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("Tap-to-Resume") },
                    supportingContent = { Text("Switch to scrollable view on pause") },
                    trailingContent = {
                        Switch(
                            checked = settings.tapToResumeEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(tapToResumeEnabled = it)) }
                        )
                    }
                )

                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Productivity", style = MaterialTheme.typography.titleMedium)

                ListItem(
                    headlineContent = { Text("Reading Goals & Streaks") },
                    trailingContent = {
                        Switch(
                            checked = settings.readingGoalsEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(readingGoalsEnabled = it)) }
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("Focus Session Timer") },
                    supportingContent = { Text("${settings.focusTimerMinutes} minutes") },
                    trailingContent = {
                        Switch(
                            checked = settings.focusTimerEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(focusTimerEnabled = it)) }
                        )
                    }
                )

                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Standard Features", style = MaterialTheme.typography.titleMedium)

                ListItem(
                    headlineContent = { Text("TTS Voice Output") },
                    supportingContent = { Text("Speak words during playback") },
                    trailingContent = {
                        Switch(
                            checked = settings.ttsEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(ttsEnabled = it)) }
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("ORP Highlighting") },
                    supportingContent = { Text("Highlight the Optimal Recognition Point") },
                    trailingContent = {
                        Switch(
                            checked = settings.orpEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(orpEnabled = it)) }
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("Punctuation Delays") },
                    trailingContent = {
                        Switch(
                            checked = settings.punctuationDelays,
                            onCheckedChange = { onUpdateSettings(settings.copy(punctuationDelays = it)) }
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("Warm-up") },
                    trailingContent = {
                        Switch(
                            checked = settings.warmupEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(warmupEnabled = it)) }
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("Focus Mask") },
                    trailingContent = {
                        Switch(
                            checked = settings.focusMaskEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(focusMaskEnabled = it)) }
                        )
                    }
                )

                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Font", style = MaterialTheme.typography.titleMedium)
                
                val fontFamilies = listOf("SansSerif", "Serif", "Monospace", "OpenDyslexic")
                var expanded by remember { mutableStateOf(false) }

                Box {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text("Font Family: ${settings.fontFamily}")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        fontFamilies.forEach { family ->
                            DropdownMenuItem(
                                text = { Text(family) },
                                onClick = {
                                    onUpdateSettings(settings.copy(fontFamily = family))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Version info at the bottom right of the scrollable view
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        modifier = Modifier.align(Alignment.BottomEnd),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
