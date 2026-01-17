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

package org.unchained.rsvp_fast_read.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class UserSettings(
    val wpm: Int = 300,
    val fontSize: Int = 32,
    val isDarkMode: Boolean = true,
    val orpEnabled: Boolean = true,
    val punctuationDelays: Boolean = true,
    val warmupEnabled: Boolean = true,
    val focusMaskEnabled: Boolean = true,
    val fontFamily: String = "SansSerif",
    val ttsEnabled: Boolean = true,
    val bionicReadingEnabled: Boolean = false,
    val contextualHybridEnabled: Boolean = false,
    val smartPauseEnabled: Boolean = false,
    val tapToResumeEnabled: Boolean = false,
    val readingGoalsEnabled: Boolean = false,
    val focusTimerEnabled: Boolean = false,
    val focusTimerMinutes: Int = 25,
    val orpColor: Int = Color.Red.toArgb(),
    val splitViewEnabled: Boolean = false
)

class UserSettingsRepository(private val context: Context) {
    private object PreferencesKeys {
        val WPM = intPreferencesKey("wpm")
        val FONT_SIZE = intPreferencesKey("font_size")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val ORP_ENABLED = booleanPreferencesKey("orp_enabled")
        val PUNCTUATION_DELAYS = booleanPreferencesKey("punctuation_delays")
        val WARMUP_ENABLED = booleanPreferencesKey("warmup_enabled")
        val FOCUS_MASK_ENABLED = booleanPreferencesKey("focus_mask_enabled")
        val FONT_FAMILY = stringPreferencesKey("font_family")
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
        val BIONIC_READING = booleanPreferencesKey("bionic_reading")
        val CONTEXTUAL_HYBRID = booleanPreferencesKey("contextual_hybrid")
        val SMART_PAUSE = booleanPreferencesKey("smart_pause")
        val TAP_TO_RESUME = booleanPreferencesKey("tap_to_resume")
        val READING_GOALS = booleanPreferencesKey("reading_goals")
        val FOCUS_TIMER = booleanPreferencesKey("focus_timer")
        val FOCUS_TIMER_MINUTES = intPreferencesKey("focus_timer_minutes")
        val ORP_COLOR = intPreferencesKey("orp_color")
        val SPLIT_VIEW = booleanPreferencesKey("split_view")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data
        .map { preferences ->
            UserSettings(
                wpm = preferences[PreferencesKeys.WPM] ?: 300,
                fontSize = preferences[PreferencesKeys.FONT_SIZE] ?: 32,
                isDarkMode = preferences[PreferencesKeys.DARK_MODE] ?: true,
                orpEnabled = preferences[PreferencesKeys.ORP_ENABLED] ?: true,
                punctuationDelays = preferences[PreferencesKeys.PUNCTUATION_DELAYS] ?: true,
                warmupEnabled = preferences[PreferencesKeys.WARMUP_ENABLED] ?: true,
                focusMaskEnabled = preferences[PreferencesKeys.FOCUS_MASK_ENABLED] ?: true,
                fontFamily = preferences[PreferencesKeys.FONT_FAMILY] ?: "SansSerif",
                ttsEnabled = preferences[PreferencesKeys.TTS_ENABLED] ?: true,
                bionicReadingEnabled = preferences[PreferencesKeys.BIONIC_READING] ?: false,
                contextualHybridEnabled = preferences[PreferencesKeys.CONTEXTUAL_HYBRID] ?: false,
                smartPauseEnabled = preferences[PreferencesKeys.SMART_PAUSE] ?: false,
                tapToResumeEnabled = preferences[PreferencesKeys.TAP_TO_RESUME] ?: false,
                readingGoalsEnabled = preferences[PreferencesKeys.READING_GOALS] ?: false,
                focusTimerEnabled = preferences[PreferencesKeys.FOCUS_TIMER] ?: false,
                focusTimerMinutes = preferences[PreferencesKeys.FOCUS_TIMER_MINUTES] ?: 25,
                orpColor = preferences[PreferencesKeys.ORP_COLOR] ?: Color.Red.toArgb(),
                splitViewEnabled = preferences[PreferencesKeys.SPLIT_VIEW] ?: false
            )
        }

    suspend fun updateWpm(wpm: Int) {
        context.dataStore.edit { it[PreferencesKeys.WPM] = wpm }
    }

    suspend fun updateFontSize(fontSize: Int) {
        context.dataStore.edit { it[PreferencesKeys.FONT_SIZE] = fontSize }
    }

    suspend fun updateDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DARK_MODE] = isDarkMode }
    }

    suspend fun updateOrpEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ORP_ENABLED] = enabled }
    }

    suspend fun updatePunctuationDelays(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.PUNCTUATION_DELAYS] = enabled }
    }

    suspend fun updateWarmupEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.WARMUP_ENABLED] = enabled }
    }

    suspend fun updateFocusMaskEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.FOCUS_MASK_ENABLED] = enabled }
    }

    suspend fun updateFontFamily(family: String) {
        context.dataStore.edit { it[PreferencesKeys.FONT_FAMILY] = family }
    }

    suspend fun updateTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.TTS_ENABLED] = enabled }
    }

    suspend fun updateBionicReadingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.BIONIC_READING] = enabled }
    }

    suspend fun updateContextualHybridEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.CONTEXTUAL_HYBRID] = enabled }
    }

    suspend fun updateSmartPauseEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SMART_PAUSE] = enabled }
    }

    suspend fun updateTapToResumeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.TAP_TO_RESUME] = enabled }
    }

    suspend fun updateReadingGoalsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.READING_GOALS] = enabled }
    }

    suspend fun updateFocusTimerEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.FOCUS_TIMER] = enabled }
    }

    suspend fun updateFocusTimerMinutes(minutes: Int) {
        context.dataStore.edit { it[PreferencesKeys.FOCUS_TIMER_MINUTES] = minutes }
    }

    suspend fun updateOrpColor(color: Int) {
        context.dataStore.edit { it[PreferencesKeys.ORP_COLOR] = color }
    }

    suspend fun updateSplitViewEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SPLIT_VIEW] = enabled }
    }
}
