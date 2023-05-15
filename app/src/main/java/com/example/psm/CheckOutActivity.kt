package com.example.psm

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.psm.models.Booking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class CheckOutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out)

        val bookingRef = intent.getStringExtra("Booking")
        val booking = Json.decodeFromString<Booking>(bookingRef.toString())

        Log.d("Booking", booking.toString())
    }
}