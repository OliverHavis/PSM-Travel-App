package com.example.psm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.psm.helpers.FirebaseHelper

class BookedHolidaysActivity : AppCompatActivity() {

    /** variables */
    private lateinit var db: FirebaseHelper
    private lateinit var adapter: SavedAdapter

    /** Activity Lifecycle Methods */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booked_holidays)

        db = FirebaseHelper()

        hideStatusBar()
        setupUI()
    }

    /** Helper Methods */
}