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
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import org.unchained.rsvp_fast_read.extraction.*
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream

class DocumentRepository(
    private val documentDao: DocumentDao,
    private val bookmarkDao: BookmarkDao,
    private val dailyStatsDao: DailyStatsDao,
    private val context: Context
) {
    val allDocuments: Flow<List<Document>> = documentDao.getAllDocuments()
    val allFolders: Flow<List<String>> = documentDao.getAllFolders()

    suspend fun addDocument(uri: Uri, folderName: String = "Default"): Long {
        try {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (e: Exception) {}

        val fileName = getFileName(uri) ?: "Unknown Document"
        val extension = fileName.substringAfterLast('.', "").lowercase()
        
        val extractor: TextExtractor = when (extension) {
            "pdf" -> PdfExtractor()
            "html", "htm" -> HtmlExtractor()
            "epub" -> EpubExtractor()
            else -> PlainTextExtractor()
        }

        val content = extractor.extractText(context, uri)
        val wordCount = countWords(content.text)

        val document = Document(
            title = fileName,
            filePath = uri.toString(),
            fileType = extension.uppercase(),
            totalWords = wordCount,
            folderName = folderName
        )

        return documentDao.insertDocument(document)
    }

    suspend fun addTextFromClipboard(text: String): Long {
        val fileName = "Clipboard_${System.currentTimeMillis()}.txt"
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { 
            it.write(text.toByteArray())
        }
        
        val wordCount = countWords(text)
        val document = Document(
            title = "Clipboard ${System.currentTimeMillis()}",
            filePath = Uri.fromFile(file).toString(),
            fileType = "TXT",
            totalWords = wordCount,
            folderName = "Clipboard"
        )
        
        return documentDao.insertDocument(document)
    }

    private fun countWords(text: String): Int {
        return text.split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }

    suspend fun getDocumentById(id: Long) = documentDao.getDocumentById(id)

    suspend fun updateProgress(document: Document, position: Int) {
        documentDao.updateDocument(document.copy(lastReadPosition = position))
    }

    suspend fun deleteDocument(document: Document) {
        if (document.filePath.startsWith("file://")) {
            try {
                val file = File(Uri.parse(document.filePath).path!!)
                if (file.exists()) file.delete()
            } catch (e: Exception) {}
        }
        documentDao.deleteDocument(document)
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = cursor.getString(index)
                }
            }
        }
        return name
    }
    
    suspend fun getDocumentContent(document: Document): DocumentContent {
        val uri = Uri.parse(document.filePath)
        
        if (document.filePath.startsWith("file://")) {
            val file = File(uri.path!!)
            return DocumentContent(file.readText())
        }

        val extension = document.fileType.lowercase()
        val extractor: TextExtractor = when (extension) {
            "pdf" -> PdfExtractor()
            "html", "htm" -> HtmlExtractor()
            "epub" -> EpubExtractor()
            else -> PlainTextExtractor()
        }
        
        return extractor.extractText(context, uri)
    }

    // Bookmark Operations
    fun getBookmarksForDocument(documentId: Long): Flow<List<Bookmark>> = bookmarkDao.getBookmarksForDocument(documentId)
    suspend fun addBookmark(bookmark: Bookmark) = bookmarkDao.insertBookmark(bookmark)
    suspend fun deleteBookmark(bookmark: Bookmark) = bookmarkDao.deleteBookmark(bookmark)

    // Stats
    fun getWeeklyStats(): Flow<List<DailyStats>> = dailyStatsDao.getLastWeekStats()
    suspend fun incrementDailyStats(date: String, words: Int, minutes: Int) {
        dailyStatsDao.incrementStats(date, words, minutes)
    }
}
