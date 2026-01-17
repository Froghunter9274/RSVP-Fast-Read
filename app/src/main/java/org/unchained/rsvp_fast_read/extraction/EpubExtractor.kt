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
import java.util.zip.ZipInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EpubExtractor : TextExtractor {
    override suspend fun extractText(context: Context, uri: Uri): DocumentContent = withContext(Dispatchers.IO) {
        val textBuilder = StringBuilder()
        val chapters = mutableListOf<Chapter>()
        var currentWordIndex = 0

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val zipInputStream = ZipInputStream(inputStream)
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                if (entry.name.endsWith(".html") || entry.name.endsWith(".xhtml")) {
                    val content = zipInputStream.bufferedReader().readText()
                    val doc = Jsoup.parse(content)
                    val chapterTitle = doc.select("h1, h2, h3, title").firstOrNull()?.text() ?: entry.name
                    val chapterText = doc.text()
                    
                    chapters.add(Chapter(chapterTitle, currentWordIndex))
                    
                    textBuilder.append(chapterText).append(" ")
                    currentWordIndex += chapterText.split(Regex("\\s+")).filter { it.isNotBlank() }.size
                }
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
        }
        DocumentContent(textBuilder.toString(), chapters)
    }
}
