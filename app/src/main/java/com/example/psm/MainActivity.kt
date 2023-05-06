package com.example.psm

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.psm.ui.login.LoginActivity


class MainActivity : ComponentActivity() {

    // initiaization
    private var isHandburgerMenuOpen = false
    private lateinit var hamburgerMenu: ImageButton
    private lateinit var hiddenMenu: LinearLayout
    private lateinit var toolbar: Toolbar
    private lateinit var searchIcon: ImageButton
    private lateinit var searchEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // varaible declaration
        hamburgerMenu = findViewById<ImageButton>(R.id.hamburger_menu)
        hiddenMenu = findViewById<LinearLayout>(R.id.hidden_menu)
        toolbar = findViewById<Toolbar>(R.id.toolbar)
        searchIcon = findViewById<ImageButton>(R.id.search_icon)
        searchEditText = findViewById<EditText>(R.id.searchEditText)
        val screen = findViewById<ConstraintLayout>(R.id.homeView)
        val loginBtn = findViewById<Button>(R.id.login_button)

        // Setup Functions
        hideStatusBar()

        // Listeners
        screen.setOnClickListener {
            // Close Menu
            closeMenu(true)
            isHandburgerMenuOpen = false

            // Close Search
            closeSearch()

//            val intent = Intent(this, FlightPlannerActivity::class.java)
//            startActivity(intent)
//            overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top)
        }

        loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top)
        }

        hamburgerMenu.setOnClickListener {

            if (isHandburgerMenuOpen) {
                closeMenu()
            } else {
                openMenu()
            }

            isHandburgerMenuOpen = !isHandburgerMenuOpen
        }

        searchIcon.setOnClickListener {
            if (searchEditText.visibility == View.INVISIBLE) {
                openSearch()
            } else {
                closeSearch()
            }
        }

    }

    private fun openMenu() {
        val drawableResId = R.drawable.avd_anim_menu_close
        runMenuAnimation(drawableResId)

        hiddenMenu.measure(View.MeasureSpec.makeMeasureSpec(toolbar.measuredWidth, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        val targetHeight = hiddenMenu.measuredHeight

        hiddenMenu.layoutParams.height = 0
        hiddenMenu.visibility = View.VISIBLE

        val animator = ValueAnimator.ofInt(0, targetHeight)
        animator.duration = 300
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            val layoutParams = hiddenMenu.layoutParams
            layoutParams.height = value
            hiddenMenu.layoutParams = layoutParams
        }
        animator.start()
    }

    private fun closeMenu(wasScreenInput : Boolean = false) {
        if (!wasScreenInput) {
            val drawableResId = R.drawable.avd_anim_menu_open
            runMenuAnimation(drawableResId)
        }

        val initialHeight = hiddenMenu.measuredHeight
        val animator = ValueAnimator.ofInt(initialHeight, 0)
        animator.duration = 300
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            val layoutParams = hiddenMenu.layoutParams
            layoutParams.height = value
            hiddenMenu.layoutParams = layoutParams
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                hiddenMenu.visibility = View.GONE
            }
        })
        animator.start()
    }

    private fun runMenuAnimation(drawableResId : Int) {
        hamburgerMenu.setImageResource(drawableResId)

        val drawable = hamburgerMenu.drawable
        if (drawable is Animatable) {
            (drawable as Animatable).start()
        }
    }

    private fun openSearch(){
        searchEditText.visibility = View.VISIBLE
        val animator = ValueAnimator.ofInt(0, resources.getDimensionPixelSize(R.dimen.desired_width))
        animator.duration = 300
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            val layoutParams = searchEditText.layoutParams
            layoutParams.width = value
            searchEditText.layoutParams = layoutParams
        }
        animator.start()
    }

    private fun closeSearch(){
        val animator = ValueAnimator.ofInt(resources.getDimensionPixelSize(R.dimen.desired_width), 0)
        animator.duration = 300
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            val layoutParams = searchEditText.layoutParams
            layoutParams.width = value
            searchEditText.layoutParams = layoutParams
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                searchEditText.visibility = View.INVISIBLE
            }
        })
        animator.start()
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
