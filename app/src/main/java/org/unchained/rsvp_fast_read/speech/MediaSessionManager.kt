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

package org.unchained.rsvp_fast_read.speech

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import org.unchained.rsvp_fast_read.ui.RsvpViewModel

/**
 * Note: Keeping support library imports as they are often preferred for MediaSession 
 * compatibility unless migrating fully to Media3.
 */
class MediaSessionManager(context: Context, private val viewModel: RsvpViewModel) {
    private var mediaSession: MediaSessionCompat = MediaSessionCompat(context, "RSVPFastRead").apply {
        setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                if (!viewModel.isPlaying.value) {
                    viewModel.togglePlayback()
                }
            }

            override fun onPause() {
                if (viewModel.isPlaying.value) {
                    viewModel.togglePlayback()
                }
            }

            override fun onSkipToNext() {
                viewModel.skip()
            }

            override fun onSkipToPrevious() {
                viewModel.rewind()
            }
        })
        isActive = true
    }

    fun updatePlaybackState(isPlaying: Boolean) {
        val state = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
            .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
            .build()
        mediaSession.setPlaybackState(playbackState)
    }

    fun release() {
        mediaSession.isActive = false
        mediaSession.release()
    }
}
