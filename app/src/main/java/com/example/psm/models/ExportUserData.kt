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

    fun getCards(): List<Card> {
        return cards
    }

    fun getSaves(): List<Saved> {
        return saves
    }

    fun getBookings(): List<Booking> {
        return bookings
    }
}
