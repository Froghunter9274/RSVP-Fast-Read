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

package org.unchained.rsvp_fast_read

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.unchained.rsvp_fast_read.data.DocumentRepository
import org.unchained.rsvp_fast_read.data.UserSettingsRepository
import org.unchained.rsvp_fast_read.speech.MediaSessionManager
import org.unchained.rsvp_fast_read.speech.TtsManager
import org.unchained.rsvp_fast_read.ui.*
import org.unchained.rsvp_fast_read.ui.theme.RSVPFastReadTheme

class MainActivity : ComponentActivity() {
    private lateinit var ttsManager: TtsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as RsvpApplication
        val docRepo = app.documentRepository
        val settingsRepo = app.userSettingsRepository
        ttsManager = TtsManager(this)

        setContent {
            val userSettings by settingsRepo.userSettingsFlow.collectAsState(initial = null)
            
            if (userSettings != null) {
                val isDarkMode = userSettings!!.isDarkMode
                val view = LocalView.current
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Library) }

                // Global Status Bar Appearance
                LaunchedEffect(isDarkMode) {
                    val window = (view.context as Activity).window
                    val controller = WindowCompat.getInsetsController(window, view)
                    controller.isAppearanceLightStatusBars = !isDarkMode
                }

                RSVPFastReadTheme(darkTheme = isDarkMode) {
                    var currentDocumentId by remember { mutableStateOf<Long?>(null) }

                    // Handle system back gesture
                    BackHandler(enabled = currentScreen != Screen.Library) {
                        currentScreen = Screen.Library
                    }

                    LaunchedEffect(intent) {
                        handleIntent(intent, docRepo)
                    }

                    when (currentScreen) {
                        Screen.Library -> {
                            val libViewModel: LibraryViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return LibraryViewModel(docRepo) as T
                                    }
                                }
                            )
                            LibraryScreen(
                                viewModel = libViewModel,
                                userSettings = userSettings!!,
                                onDocumentClick = { 
                                    currentDocumentId = it
                                    currentScreen = Screen.Reader
                                },
                                onSettingsClick = { currentScreen = Screen.Settings },
                                onHelpClick = { currentScreen = Screen.Help }
                            )
                        }
                        Screen.Reader -> {
                            val rsvpViewModel: RsvpViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return RsvpViewModel(docRepo, settingsRepo, ttsManager) as T
                                    }
                                }
                            )
                            
                            val isPlaying by rsvpViewModel.isPlaying.collectAsState()
                            val countdown by rsvpViewModel.countdown.collectAsState()
                            
                            // Immersive Mode Logic: Only hide bars when actively reading
                            LaunchedEffect(isPlaying, countdown) {
                                val window = (view.context as Activity).window
                                val controller = WindowCompat.getInsetsController(window, view)
                                if (isPlaying || countdown != null) {
                                    controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
                                    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                                } else {
                                    controller.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
                                }
                            }

                            val mediaSessionManager = remember { MediaSessionManager(this@MainActivity, rsvpViewModel) }
                            
                            LaunchedEffect(isPlaying) {
                                mediaSessionManager.updatePlaybackState(isPlaying)
                            }
                            
                            DisposableEffect(Unit) {
                                onDispose {
                                    mediaSessionManager.release()
                                }
                            }

                            LaunchedEffect(currentDocumentId) {
                                currentDocumentId?.let { rsvpViewModel.loadDocument(it) }
                            }

                            BackHandler {
                                rsvpViewModel.pause()
                                currentScreen = Screen.Library
                            }

                            RsvpScreen(
                                viewModel = rsvpViewModel,
                                userSettings = userSettings!!,
                                onBack = { 
                                    rsvpViewModel.pause()
                                    currentScreen = Screen.Library 
                                },
                                onUpdateWpm = { wpm ->
                                    lifecycleScope.launch { settingsRepo.updateWpm(wpm) }
                                },
                                onUpdateFontSize = { size ->
                                    lifecycleScope.launch { settingsRepo.updateFontSize(size) }
                                }
                            )
                        }
                        Screen.Settings -> {
                            SettingsScreen(
                                settings = userSettings!!,
                                onBack = { currentScreen = Screen.Library },
                                onUpdateSettings = { newSettings ->
                                    lifecycleScope.launch {
                                        val oldSettings = userSettings!!
                                        
                                        if (newSettings.splitViewEnabled && !oldSettings.splitViewEnabled) {
                                            settingsRepo.updateSplitViewEnabled(true)
                                            settingsRepo.updateTapToResumeEnabled(false)
                                            settingsRepo.updateContextualHybridEnabled(false)
                                        } 
                                        else if ((newSettings.tapToResumeEnabled && !oldSettings.tapToResumeEnabled) || 
                                                 (newSettings.contextualHybridEnabled && !oldSettings.contextualHybridEnabled)) {
                                            settingsRepo.updateSplitViewEnabled(false)
                                            settingsRepo.updateTapToResumeEnabled(newSettings.tapToResumeEnabled)
                                            settingsRepo.updateContextualHybridEnabled(newSettings.contextualHybridEnabled)
                                        }
                                        else {
                                            settingsRepo.updateSplitViewEnabled(newSettings.splitViewEnabled)
                                            settingsRepo.updateTapToResumeEnabled(newSettings.tapToResumeEnabled)
                                            settingsRepo.updateContextualHybridEnabled(newSettings.contextualHybridEnabled)
                                        }

                                        settingsRepo.updateDarkMode(newSettings.isDarkMode)
                                        settingsRepo.updateOrpEnabled(newSettings.orpEnabled)
                                        settingsRepo.updatePunctuationDelays(newSettings.punctuationDelays)
                                        settingsRepo.updateWarmupEnabled(newSettings.warmupEnabled)
                                        settingsRepo.updateFocusMaskEnabled(newSettings.focusMaskEnabled)
                                        settingsRepo.updateFontFamily(newSettings.fontFamily)
                                        settingsRepo.updateTtsEnabled(newSettings.ttsEnabled)
                                        settingsRepo.updateBionicReadingEnabled(newSettings.bionicReadingEnabled)
                                        settingsRepo.updateSmartPauseEnabled(newSettings.smartPauseEnabled)
                                        settingsRepo.updateReadingGoalsEnabled(newSettings.readingGoalsEnabled)
                                        settingsRepo.updateFocusTimerEnabled(newSettings.focusTimerEnabled)
                                        settingsRepo.updateOrpColor(newSettings.orpColor)
                                    }
                                }
                            )
                        }
                        Screen.Help -> {
                            HelpScreen(onBack = { currentScreen = Screen.Library })
                        }
                    }
                }
            }
        }
    }

    private fun handleIntent(intent: Intent, repository: DocumentRepository) {
        if (intent.action == Intent.ACTION_SEND) {
            val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            uri?.let {
                lifecycleScope.launch {
                    repository.addDocument(it)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}

sealed class Screen {
    object Library : Screen()
    object Reader : Screen()
    object Settings : Screen()
    object Help : Screen()
}
