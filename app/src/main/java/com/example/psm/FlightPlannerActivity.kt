package com.example.psm

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.psm.helpers.FirebaseHelper
import com.example.psm.models.Booking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar


class FlightPlannerActivity : ComponentActivity() {
    // Variables
    private lateinit var db: FirebaseHelper
    private lateinit var adapter: HolidayPlannerAdapter
    private var isExpanded = false
    private lateinit var querySearchBtn: View
    private lateinit var querySubmitBtn: Button
    private lateinit var queryResetBtn: Button
    private var querySearchBtnHeight = 0
    private var queryInputsHeight = 0
    private lateinit var queryInputs : LinearLayout
    private var querySelectedFrom = "Any"
    private var querySelectedDestination = "Any"
    private lateinit var searchText: TextView
    private lateinit var searchTextFrom: TextView
    private lateinit var searchTextTo: TextView
    private lateinit var searchTextWhen: TextView
    private lateinit var searchTextWho: TextView
    private lateinit var flyingFromSpinner : Spinner
    private lateinit var destinationSpinner : Spinner
    private lateinit var resultText : TextView

    lateinit var allUkAirports : List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_planner)

        db = FirebaseHelper()


        queryInputs = findViewById<LinearLayout>(R.id.flightPlannerFields)
        val queryInputsViewTreeObserver = queryInputs.viewTreeObserver
        queryInputsViewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                queryInputs.viewTreeObserver.removeOnGlobalLayoutListener(this)
                queryInputsHeight = queryInputs.height  // Get the original height here, after layout has been calculated
                println("queryInputsHeight: $queryInputsHeight")

                // Now set the height to 0 for the animation
                val layoutParams = queryInputs.layoutParams
                layoutParams.height = 0
                queryInputs.layoutParams = layoutParams
            }
        })

        hideStatusBar()
        setupUI()
        setupVariables()
        setupListeners()
    }

    fun setupUI() {

        // destination list
        val recyclerView = findViewById<RecyclerView>(R.id.flightPlannerRecyclerView)

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
        val flyingFromitems = listOf("Any") + allUkAirports.sorted()

        // Getting 'Top Destinations' from Firebase
        db.getRandomLocations(
            onSuccess = { topDestinations ->
                adapter = HolidayPlannerAdapter(this)
                recyclerView.adapter = adapter
                val spacingInPixels = resources.getDimensionPixelSize(R.dimen.item_spacing)
                recyclerView.addItemDecoration(SpaceItemDecoration(spacingInPixels))
                recyclerView.layoutManager = LinearLayoutManager(this)

                adapter.setData(topDestinations)
            },
            onFailure = {
                Toast.makeText(this, "Failed to get top destinations", Toast.LENGTH_SHORT).show()
            }
        )

        // flight dropdowns
        flyingFromSpinner = findViewById<Spinner>(R.id.flightPlanner_From)
        destinationSpinner = findViewById<Spinner>(R.id.flightPlanner_Destination)


        val flayingFromAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, flyingFromitems)
        flayingFromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        flyingFromSpinner.adapter = flayingFromAdapter

        flyingFromSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = flyingFromitems[position]
                println("selectedItem: $selectedItem")
                querySelectedFrom = selectedItem
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Do nothing
            }
        }

        db.getAvailableLocations(
            onSuccess = { locations ->
                val destinationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)
                destinationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                destinationSpinner.adapter = destinationAdapter

                destinationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val selectedItem = locations[position]
                        querySelectedDestination = selectedItem
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        // Do nothing
                    }
                }
            },
            onFailure = {
                Toast.makeText(this, "Failed to get available locations", Toast.LENGTH_SHORT).show()
            }
        )

    }

    fun setupVariables() {
        querySearchBtn = findViewById(R.id.flightPlannerSearch)
        querySubmitBtn = findViewById(R.id.flightPlannerSubmit)
        queryResetBtn = findViewById(R.id.flightPlannerReset)

        resultText = findViewById(R.id.resultsText)

        searchText = findViewById<TextView>(R.id.flightPlannerSearchText)
        searchTextFrom = findViewById<TextView>(R.id.flightPlannerSearchTextFrom)
        searchTextTo = findViewById<TextView>(R.id.flightPlannerSearchTextTo)
        searchTextWhen = findViewById<TextView>(R.id.flightPlannerSearchTextWhen)
        searchTextWho = findViewById<TextView>(R.id.flightPlannerSearchTextWho)
    }

    fun setupListeners() {
        querySearchBtn.setOnClickListener {
            expandCardView()
        }

        querySubmitBtn.setOnClickListener {
            val departureDate = findViewById<EditText>(R.id.flightPlanner_DepartureDate).text.toString()
            val nightsText = findViewById<EditText>(R.id.flightPlanner_lengthOfStay).text.toString()
            val adultsNumText = findViewById<EditText>(R.id.flightPlanner_passengerAdults).text.toString()
            val childrenNumText = findViewById<EditText>(R.id.flightPlanner_passengerChildren).text.toString()

            val nights = nightsText.toIntOrNull() ?: 0
            val adultsNum = adultsNumText.toIntOrNull() ?: 0
            val childrenNum = childrenNumText.toIntOrNull() ?: 0

            // Check if the query inputs are valid
            if (departureDate == "" || querySelectedFrom == "" || querySelectedDestination == "") {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nights < 1) {
                Toast.makeText(this, "Please enter a valid number of nights", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (adultsNum < 1) {
                Toast.makeText(this, "Please enter a valid number of adults", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            resultText.visibility = View.GONE

            // Update the search button text
            searchText.text = "Holidays for $querySelectedDestination"
            searchTextFrom.text = "$querySelectedFrom"
            searchTextTo.text = "$querySelectedDestination"
            searchTextWhen.text = "$departureDate"
            searchTextWho.text = "$adultsNum Adults, $childrenNum Children"

            db.queryDestinations(
                querySelectedDestination,
                onSuccess = { holidays ->
                    println("holidays: $holidays")
                    // Update the data in your RecyclerView adapter
                    adapter.setData(holidays)
                    adapter.setQueryData(querySelectedFrom, querySelectedDestination, departureDate, nights, adultsNum, childrenNum)
                },
                onFailure = {
                    Toast.makeText(this, "Failed to get holidays", Toast.LENGTH_SHORT).show()
                }
            )

            collapseCardView()

            // hide the keyboard
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }

        queryResetBtn.setOnClickListener {
            findViewById<EditText>(R.id.flightPlanner_DepartureDate).text.clear()
            findViewById<EditText>(R.id.flightPlanner_lengthOfStay).text.clear()
            findViewById<EditText>(R.id.flightPlanner_passengerAdults).text.clear()
            findViewById<EditText>(R.id.flightPlanner_passengerChildren).text.clear()

            searchText.text = "Search for your next holiday"
            searchTextFrom.text = "From"
            searchTextTo.text = "To"
            searchTextWhen.text = "When"
            searchTextWho.text = "Who"

            flyingFromSpinner.setSelection(0)
            destinationSpinner.setSelection(0)

            db.getRandomLocations(
                onSuccess = { topDestinations ->
                    adapter.setData(topDestinations)
                    
                },
                onFailure = {
                    Toast.makeText(this, "Failed to get top destinations", Toast.LENGTH_SHORT).show()
                }
            )

            resultText.visibility = View.VISIBLE

            collapseCardView()
        }

        val departureDateEditText = findViewById<EditText>(R.id.flightPlanner_DepartureDate)
        departureDateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                R.style.CustomDatePickerDialog,
                DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                    departureDateEditText.setText(formattedDate)
                },
                year,
                month,
                day
            )


            datePickerDialog.show()
        }

    }

    private fun expandCardView() {
        val queryInputs = findViewById<LinearLayout>(R.id.flightPlannerFields)
        val imageView = findViewById<ImageView>(R.id.flightPlannerHeader)
        val initialHeight = imageView.height
        val targetHeight = initialHeight * 3
        val animationDuration = 500L

        querySearchBtnHeight = querySearchBtn.measuredHeight
        val targetQueryHeight = queryInputsHeight
        println("targetQueryHeight: $targetQueryHeight")


        val querySearchBtnAnimator = ValueAnimator.ofInt(querySearchBtnHeight, 0)
        querySearchBtnAnimator.addUpdateListener { animator ->
            val height = animator.animatedValue as Int
            val layoutParams = querySearchBtn.layoutParams
            layoutParams.height = height
            querySearchBtn.layoutParams = layoutParams
        }

        val queryAnimator = ValueAnimator.ofInt(0, targetQueryHeight)
        queryAnimator.addUpdateListener { animator ->
            val height = animator.animatedValue as Int
            val queryInputsLayoutParams = queryInputs.layoutParams
            queryInputsLayoutParams.height = height
            queryInputs.layoutParams = queryInputsLayoutParams
        }

        val imageAnimator = ValueAnimator.ofInt(initialHeight, targetHeight)
        imageAnimator.addUpdateListener { animator ->
            val height = animator.animatedValue as Int
            val layoutParams = imageView.layoutParams
            layoutParams.height = height
            imageView.layoutParams = layoutParams
        }

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(querySearchBtnAnimator, queryAnimator, imageAnimator)
        animatorSet.duration = animationDuration
        animatorSet.start()
    }

    private fun collapseCardView() {
        val queryInputs = findViewById<LinearLayout>(R.id.flightPlannerFields)
        val imageView = findViewById<ImageView>(R.id.flightPlannerHeader)
        val initialHeight = imageView.height
        val targetHeight = initialHeight / 3
        val animationDuration = 500L

        val targetQueryHeight = queryInputsHeight
        println("targetQueryHeight: $targetQueryHeight")


        val querySearchBtnAnimator = ValueAnimator.ofInt(0, querySearchBtnHeight)
        querySearchBtnAnimator.addUpdateListener { animator ->
            val height = animator.animatedValue as Int
            val layoutParams = querySearchBtn.layoutParams
            layoutParams.height = height
            querySearchBtn.layoutParams = layoutParams
        }

        val queryAnimator = ValueAnimator.ofInt(targetQueryHeight, 0)
        queryAnimator.addUpdateListener { animator ->
            val height = animator.animatedValue as Int
            val queryInputsLayoutParams = queryInputs.layoutParams
            queryInputsLayoutParams.height = height
            queryInputs.layoutParams = queryInputsLayoutParams
        }

        val imageAnimator = ValueAnimator.ofInt(initialHeight, targetHeight)
        imageAnimator.addUpdateListener { animator ->
            val height = animator.animatedValue as Int
            val layoutParams = imageView.layoutParams
            layoutParams.height = height
            imageView.layoutParams = layoutParams
        }

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(querySearchBtnAnimator, queryAnimator, imageAnimator)
        animatorSet.duration = animationDuration
        animatorSet.start()
    }

    fun bookNow(booking : Booking) {
        val intent = Intent(this, CheckOutActivity::class.java)
        val booking = Json.encodeToString(booking)
        intent.putExtra("Booking", booking)
        startActivity(intent)
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
}
