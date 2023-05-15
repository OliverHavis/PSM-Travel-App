package com.example.psm

import Booking
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.psm.helpers.FirebaseHelper
import com.example.psm.models.Destination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CheckOutActivity : AppCompatActivity() {
    // Initialize variables
    private lateinit var db: FirebaseHelper
    private lateinit var booking: Booking
    private lateinit var destination: Destination
    private var extras: MutableList<Map<String, Any>> = mutableListOf()

    var newTotalPrice: Double = 0.00


    // Activity lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out)

        CoroutineScope(Dispatchers.Main).launch {
            setVariables()
            setupUI()
            setupListeners()
        }
    }

    override fun onBackPressed() {
        finishWithAnimation()
    }

    /** Custom methods */

    /**
     * Set the variables from the intent
     */
    private suspend fun setVariables() {
        db = FirebaseHelper()

        booking = intent.getParcelableExtra("Booking")!!
        destination = booking.destination!!

        println("Extras: ${booking.selectedExtras}")

        booking.selectedExtras.forEach() {
            println("ExtraID: $it")
            extras.add(db.getExcursion(it))
        }
    }

    /**
     * Sets up the UI
     */
    private fun setupUI() {
        hideStatusBar()

        // Load booking details
        var bookingFromText : TextView = findViewById(R.id.bookingFrom)
        var bookingWhenText : TextView = findViewById(R.id.bookingWhen)
        var bookingPassengersText : TextView = findViewById(R.id.bookingPassengers)

        bookingFromText.text = booking.queryFrom
        bookingWhenText.text = booking.queryDate
        bookingPassengersText.text = "${booking.queryAdults} Adults, ${booking.queryChildren} Children"

        // Load destination details
        var bookingDestinationNameText : TextView = findViewById(R.id.bookingHotel)
        var bookingDestinationLocationText : TextView = findViewById(R.id.bookingLocation)
        var bookingDestinationNightsText : TextView = findViewById(R.id.bookingNights)

        bookingDestinationNameText.text = destination.getName()
        bookingDestinationLocationText.text = destination.getLocation()
        bookingDestinationNightsText.text = "${booking.queryNights} Nights"

        if (extras.size > 0) {
            extras.forEach() {
                val extraText = "${it["name"]} +${String.format("%.2f", it["price"])}€"
                createExcursionText(it["name"].toString())
            }
        } else {
            createExcursionText("No excursions selected")
        }

        // Load price details
        var bookingDestinationPriceText: TextView = findViewById(R.id.bookingPrice)
        var bookingDestinationExtrasPriceText: TextView = findViewById(R.id.bookingExtrasPrice)
        var bookingTotalPriceText: TextView = findViewById(R.id.bookingTotalPrice)

        val totalPrice = booking.totalPrice
        val extrasPrice = extras.sumByDouble { it["price"].toString().toDouble() }
        newTotalPrice = totalPrice + extrasPrice

        bookingDestinationPriceText.text =  "£" + String.format("%.2f", totalPrice)
        bookingDestinationExtrasPriceText.text = "+£" + String.format("%.2f", extrasPrice)
        bookingTotalPriceText.text = "£" + String.format("%.2f", newTotalPrice)

    }

    /**
     * Sets up the listeners
     */
    private fun setupListeners() {
        // Back button
        val buyBtn: Button = findViewById(R.id.buy_now_button)
        var cancelBtn: Button = findViewById(R.id.cancel_button)

        buyBtn.setOnClickListener {
            booking.totalPrice = newTotalPrice
            db.addBooking(
                booking,
                onSuccess = {
                    println("Booking added successfully")
                    goToBookedHolidays()
                    Toast.makeText(this, "Booking Made. Have a good day!", Toast.LENGTH_SHORT).show()
                },
                onFailure = {
                    println("Booking failed to add")
                    Toast.makeText(this, "There was an error please try again.", Toast.LENGTH_SHORT).show()
                }
            )
        }

        cancelBtn.setOnClickListener {
            finishWithAnimation()
        }
    }

    /**
     * Sends the user to the booked holidays page
     */
    private fun goToBookedHolidays() {
        val intent = intent
        intent.setClass(this, BookedHolidaysActivity::class.java)
        startActivity(intent)
    }

    /**
     * Create a text view with the excursion name
     *
     * @param text The excursion name and price
     */
    private fun createExcursionText(text: String) {
        var bookingDestinationExtraLayout: LinearLayout = findViewById(R.id.bookingSelectedExcursions)

        var excursionText = TextView(this)
        excursionText.text = text
        excursionText.setTextColor(getColor(R.color.dark_blue))
        excursionText.textSize = 16F
        excursionText.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END

        bookingDestinationExtraLayout.addView(excursionText)
    }

    /**
     * Finish the activity with a slide animation
     *
     * @see overridePendingTransition
     */
    private fun finishWithAnimation() {
        finish()
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
    }

    /**
     * Hide the status bar and make the app fullscreen
     *
     * @see window
     * @see WindowManager.LayoutParams
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