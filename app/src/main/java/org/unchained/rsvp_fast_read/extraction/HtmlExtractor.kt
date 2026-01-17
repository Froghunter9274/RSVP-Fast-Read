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

package org.unchained.rsvp_fast_read.extraction

import android.content.Context
import android.net.Uri
import org.jsoup.Jsoup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HtmlExtractor : TextExtractor {
    override suspend fun extractText(context: Context, uri: Uri): DocumentContent = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val html = inputStream.bufferedReader().use { it.readText() }
            val doc = Jsoup.parse(html)
            doc.select("script, style").remove()
            DocumentContent(doc.text())
        } ?: DocumentContent("")
    }
}
