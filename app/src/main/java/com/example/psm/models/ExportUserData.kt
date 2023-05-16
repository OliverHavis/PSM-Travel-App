package com.example.psm.models

import Booking
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.example.psm.MyProfileActivity
import com.example.psm.PdfViewerActivity
import com.example.psm.R
import com.example.psm.helpers.FirebaseHelper
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ExportUserData(private var context: Context, private var user: User) {

    private lateinit var db: FirebaseHelper

    private lateinit var cards: List<Card>
    private lateinit var saves: List<Saved>
    private lateinit var bookings: List<Booking>

    init {
        db = FirebaseHelper()

        // Cards
        CoroutineScope(Dispatchers.Main).launch {
            db.getAllCards(
                onSuccess = { cards ->
                    this@ExportUserData.cards = cards
                },
                onFailure = { error ->
                    this@ExportUserData.cards = listOf()
                }
            )
        }

        // Saves
        CoroutineScope(Dispatchers.Main).launch {
            db.getSaves(
                onSuccess = { saves ->
                    this@ExportUserData.saves = saves
                },
                onFailure = { error ->
                    this@ExportUserData.saves = listOf()
                }
            )
        }

        // Bookings
        CoroutineScope(Dispatchers.Main).launch {
            db.getBookings(
                onSuccess = { bookings ->
                    this@ExportUserData.bookings = bookings
                },
                onFailure = {
                    this@ExportUserData.bookings = listOf()
                }
            )
        }

    }

    /**
     * Create a PDF file using the iText library
     */
    fun createPdf() : String {
        println("Creating PDF file")
        // Create a file name
        val fileName = "${user.getFirstName()}-${user.getLastName()}.pdf"
        val filePath = getFilePath(fileName)

        // Create a new PDF document
        val pdfWriter = PdfWriter(FileOutputStream(filePath))
        val pdfDocument = PdfDocument(pdfWriter)

        // Create a new document to add content
        val document = Document(pdfDocument)

        // Add content to the document
        val paragraph = Paragraph("Hello, World!")
        document.add(paragraph)

        // Close the document
        document.close()

        // Open the generated PDF file
        return filePath
    }

    /**
     * Get the file path for the PDF file
     */
    private fun getFilePath(fileName: String): String {
        val directory = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "PDFs"
        )
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return File(directory, fileName).absolutePath
    }
}
