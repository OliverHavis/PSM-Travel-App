package com.example.psm

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class FlightPlannerActivity : ComponentActivity() {
    // Variables
    private lateinit var adapter: HolidayPlannerAdapter
    private var isExpanded = false
    private lateinit var querySearchBtn: View
    private lateinit var querySubmitBtn: Button
    private lateinit var queryResetBtn: Button
    private var querySearchBtnHeight = 0
    private var queryInputsHeight = 0
    private lateinit var queryInputs : LinearLayout
    private var querySelectedFrom = ""
    private var querySelectedDestination = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_planner)

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

        val recyclerView = findViewById<RecyclerView>(R.id.flightPlannerRecyclerView)

        adapter = HolidayPlannerAdapter()
        recyclerView.adapter = adapter
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.item_spacing)
        recyclerView.addItemDecoration(SpaceItemDecoration(spacingInPixels))
        recyclerView.layoutManager = LinearLayoutManager(this)

        val flyingFromSpinner = findViewById<Spinner>(R.id.flightPlanner_From)
        val destinationSpinner = findViewById<Spinner>(R.id.flightPlanner_Destination)
        val flyingFromitems = listOf("Flying From?",)
        val destininationFromItems = listOf("Where to?", "Italy")

        val flayingFromAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, flyingFromitems)
        val destinationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, destininationFromItems)
        flayingFromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        destinationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        flyingFromSpinner.adapter = flayingFromAdapter
        destinationSpinner.adapter = destinationAdapter

        flyingFromSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = flyingFromitems[position]
                querySelectedFrom = selectedItem
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Do nothing
            }
        }

        destinationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = destininationFromItems[position]
                querySelectedDestination = selectedItem
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Do nothing
            }
        }


    }

    fun setupVariables() {
        querySearchBtn = findViewById(R.id.flightPlannerSearch)
        querySubmitBtn = findViewById(R.id.flightPlannerSubmit)
        queryResetBtn = findViewById(R.id.flightPlannerReset)
    }

    fun setupListeners() {
        querySearchBtn.setOnClickListener {
            expandCardView()
        }

        querySubmitBtn.setOnClickListener {
            val departureDate = findViewById<EditText>(R.id.flightPlanner_DepartureDate).text.toString()
            val nights = findViewById<EditText>(R.id.flightPlanner_lengthOfStay).text.toString()
            val adultsNum = findViewById<EditText>(R.id.flightPlanner_passengerAdults).text.toString()
            val childrenNum = findViewById<EditText>(R.id.flightPlanner_passengerChildren).text.toString()

            // Check if the query inputs are valid
            if (departureDate == "" || nights == "" || adultsNum == "" || childrenNum == "" || querySelectedFrom == "" || querySelectedDestination == "") {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update the search button text
            findViewById<TextView>(R.id.flightPlannerSearchText).text = "Holidays for $querySelectedDestination"
            findViewById<TextView>(R.id.flightPlannerSearchTextFrom).text = "$querySelectedFrom"
            findViewById<TextView>(R.id.flightPlannerSearchTextTo).text = "$querySelectedDestination"
            findViewById<TextView>(R.id.flightPlannerSearchTextWhen).text = "$departureDate"
            findViewById<TextView>(R.id.flightPlannerSearchTextWho).text = "$adultsNum Adults, $childrenNum Children"


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
