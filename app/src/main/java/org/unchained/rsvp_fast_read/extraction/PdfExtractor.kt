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
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PdfExtractor : TextExtractor {
    override suspend fun extractText(context: Context, uri: Uri): DocumentContent = withContext(Dispatchers.IO) {
        PDFBoxResourceLoader.init(context)
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            PDDocument.load(inputStream).use { document ->
                val stripper = PDFTextStripper()
                stripper.sortByPosition = true
                val text = stripper.getText(document)
                
                val chapters = mutableListOf<Chapter>()
                val outline = document.documentCatalog.documentOutline
                if (outline != null) {
                    var item = outline.firstChild
                    while (item != null) {
                        // For PDFs, mapping outline to exact word index is complex.
                        // As a heuristic, we'll store the page number or just titles for now.
                        // Better: strip text page by page to get exact indices.
                        chapters.add(Chapter(item.title, 0)) // Simplification
                        item = item.nextSibling
                    }
                }
                
                DocumentContent(text, chapters)
            }
        } ?: DocumentContent("")
    }
}
