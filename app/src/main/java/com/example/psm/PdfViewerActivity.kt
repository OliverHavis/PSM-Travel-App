package com.example.psm

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.psm.helpers.FirebaseHelper
import com.example.psm.models.ExportUserData
import com.example.psm.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var db: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        db = FirebaseHelper()
        var user : User? = null
        CoroutineScope(Dispatchers.Main).launch {
            user = db.getCurrentUser()!!

            findViewById<TextView>(R.id.userTitleText).text = "${user!!.getFirstName()} ${user!!.getLastName()}'s Data"

            val usersInfo = findViewById<LinearLayout>(R.id.usersInfoLayout)

            // user info
            createTextView("First Name", user!!.getFirstName(), usersInfo)
            createTextView("Last Name", user!!.getLastName(), usersInfo)
            createTextView("Email", user!!.getEmail(), usersInfo)
            createTextView("Phone Number", user!!.getPhone(), usersInfo)
            user!!.getAddress()?.let { createTextView("Address", it, usersInfo) }

            // Cards
            CoroutineScope(Dispatchers.Main).launch {
                val cardsInfo = findViewById<LinearLayout>(R.id.cardsInfoLayout)
                db.getAllCards(
                    onSuccess = { cards ->
                        for (card in cards) {
                            createTextView(
                                card.getCardHolder(),
                                "${card.getCardNumber()} (${card.getExpiryDate()})",
                                cardsInfo
                            )
                        }
                    },
                    onFailure = { error ->
                        createTextView("Cards", "No cards found", usersInfo)
                    }
                )
            }
        }

    }

    fun createTextView(title: String, value: String, layout: LinearLayout) {
        val textView = TextView(this)
        textView.text = "$title: $value"
        textView.textSize = 15f
        textView.setTextColor(resources.getColor(R.color.dark_blue))
        textView.setPadding(0, 0, 0, 20)
        layout.addView(textView)
    }
}
