package com.example.psm

import Booking
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.psm.helpers.FirebaseHelper


class SavedActivity : ComponentActivity() {
    // Variables
    private lateinit var db: FirebaseHelper
    private lateinit var adapter: SavedAdapter

    lateinit var allUkAirports : List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_holidays)

        db = FirebaseHelper()

        hideStatusBar()
        setupUI()
    }

    fun setupUI() {

        // destination list
        val recyclerView = findViewById<RecyclerView>(R.id.savedRecyclerView)

        // Airport lists
        val ukAirports = listOf(
            "London Heathrow Airport",
            "London Gatwick Airport",
            "Manchester Airport",
            "Birmingham Airport",
            "Glasgow Airport",
            "Edinburgh Airport",
            "Bristol Airport",
            "Stansted Airport",
            "Luton Airport",
            "Liverpool John Lennon Airport",
            "Newcastle Airport",
            "Leeds Bradford Airport",
            "East Midlands Airport",
            "Belfast International Airport",
            "Aberdeen Airport",
            "Southampton Airport",
            "Cardiff Airport",
            "Bournemouth Airport",
            "London City Airport",
            "Exeter Airport",
            "Inverness Airport",
            "Norwich Airport",
            "Dundee Airport",
            "Humberside Airport",
            "Jersey Airport",
            "Guernsey Airport"
        )

        val additionalAirports = listOf(
            "Belfast City Airport",
            "London Luton Airport",
            "London Stansted Airport",
            "London Southend Airport",
            "Prestwick Airport",
            "Isle of Man Airport",
            "Blackpool Airport",
            "Newquay Cornwall Airport",
            "Durham Tees Valley Airport",
            "Isle of Wight Sandown Airport"
        )

        allUkAirports = ukAirports + additionalAirports

        updateSaved()
    }


    private fun finishWithAnimation() {
        finish()
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
    }

    // Override onBackPressed() to apply the reverse animation
    override fun onBackPressed() {
        finishWithAnimation()
    }

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

    fun updateSaved() {
        val recyclerView = findViewById<RecyclerView>(R.id.savedRecyclerView)

        adapter = SavedAdapter(this)
        recyclerView.adapter = adapter
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.item_spacing)
        recyclerView.addItemDecoration(SpaceItemDecoration(spacingInPixels))
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Getting 'Top Destinations' from Firebase
        db.getSaves(
            onSuccess = { saves ->
                adapter.setData(saves)
            },
            onFailure = {
                Toast.makeText(this, "Failed to get top destinations", Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun bookNow(booking : Booking) {
        val intent = Intent(this, CheckOutActivity::class.java)
        intent.putExtra("Booking", booking)
        startActivity(intent)
    }

}

class SpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = space
        outRect.right = space
        outRect.bottom = space

        // Add top margin only for the first item to avoid double spacing between items
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = space
        } else {
            outRect.top = 0
        }
    }
}
