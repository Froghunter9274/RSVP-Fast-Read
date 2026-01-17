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

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatsDao {
    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun getStatsForDate(date: String): DailyStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: DailyStats)

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT 7")
    fun getLastWeekStats(): Flow<List<DailyStats>>

    @Transaction
    suspend fun incrementStats(date: String, words: Int, minutes: Int) {
        val current = getStatsForDate(date) ?: DailyStats(date)
        insertStats(current.copy(
            wordsRead = current.wordsRead + words,
            minutesRead = current.minutesRead + minutes
        ))
    }
}
