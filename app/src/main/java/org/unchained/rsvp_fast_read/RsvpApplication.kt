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

import android.app.Application
import androidx.room.Room
import org.unchained.rsvp_fast_read.data.AppDatabase
import org.unchained.rsvp_fast_read.data.DocumentRepository
import org.unchained.rsvp_fast_read.data.UserSettingsRepository

class RsvpApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    val documentRepository by lazy {
        DocumentRepository(
            database.documentDao(),
            database.bookmarkDao(),
            database.dailyStatsDao(),
            this
        )
    }

    val userSettingsRepository by lazy {
        UserSettingsRepository(this)
    }
}
