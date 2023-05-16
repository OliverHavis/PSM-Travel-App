package com.example.psm

import Booking
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.psm.helpers.FirebaseHelper

class BookedHolidaysActivity : AppCompatActivity() {

    /** variables */
    private lateinit var db: FirebaseHelper
    private lateinit var adapter: BookedAdapter

    /** Activity Lifecycle Methods */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booked_holidays)

        db = FirebaseHelper()

        hideStatusBar()
        setupUI()
    }

    override fun onBackPressed() {
        finishWithAnimation()
    }

    /** Helper Methods */
    /**
     * Sets up the UI
     */
    private fun setupUI() {
        val recyclerView = findViewById<RecyclerView>(R.id.bookedRecyclerView)

        adapter = BookedAdapter(this)
        recyclerView.adapter = adapter
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.item_spacing)
        recyclerView.addItemDecoration(SpaceItemDecoration(spacingInPixels))
        recyclerView.layoutManager = LinearLayoutManager(this)

        db.getBookings(
            onSuccess = { bookings ->
                adapter.setBookings(bookings as MutableList<Booking>)
            },
            onFailure = {
                Toast.makeText(this, "Failed to get bookings please try again.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    /**
     * Animates the back button
     */
    private fun finishWithAnimation() {
        finish()
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
    }

    /**
     * Hides the status and navigation bars
     */
    private fun hideStatusBar() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = Color.TRANSPARENT

        val w = getWindow()
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }
}