package com.example.psm

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
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
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.psm.helpers.FirebaseHelper
import com.example.psm.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    // initiaization
    private lateinit var db: FirebaseHelper
    private var user : User? = null

    private var isHandburgerMenuOpen = false
    private lateinit var hamburgerMenu: ImageButton
    private lateinit var hiddenMenu: LinearLayout
    private lateinit var toolbar: Toolbar
    private lateinit var searchIcon: ImageButton
    private lateinit var searchEditText: EditText
    private lateinit var loginBtn: Button;
    private lateinit var login_text : TextView;
    private lateinit var display_name : TextView;
    private lateinit var screen: ConstraintLayout
    private lateinit var myProfileBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // varaible declaration
        db = FirebaseHelper()
        hamburgerMenu = findViewById<ImageButton>(R.id.hamburger_menu)
        hiddenMenu = findViewById<LinearLayout>(R.id.hidden_menu)
        toolbar = findViewById<Toolbar>(R.id.toolbar)
        searchIcon = findViewById<ImageButton>(R.id.search_icon)
        searchEditText = findViewById<EditText>(R.id.searchEditText)
        screen = findViewById<ConstraintLayout>(R.id.homeView)
        loginBtn = findViewById<Button>(R.id.login_button)
        login_text = findViewById<TextView>(R.id.login_text)
        display_name = findViewById<TextView>(R.id.display_name)
        myProfileBtn = findViewById<ImageButton>(R.id.user_icon)

        // setup View
        hideStatusBar()
        setupUI()
        setupListeners()

    }

    /**
     * Opens hamburger menu.
     */
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

    /**
     * Closes hamburger menu.
     */
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

    /**
     * Updates the handburger menu animation.
     */
    private fun runMenuAnimation(drawableResId : Int) {
        hamburgerMenu.setImageResource(drawableResId)

        val drawable = hamburgerMenu.drawable
        if (drawable is Animatable) {
            (drawable as Animatable).start()
        }
    }

    /**
     * Opens the search bar.
     */
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

    /**
     * Closes the search bar.
     */
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

    /**
     * Gets the current user and updates the UI.
     */
    private fun setupUI() {
        // Get the current user
        CoroutineScope(Dispatchers.Main).launch {
            val currentUser = db.getCurrentUser()

            updateUI(currentUser)
        }
    }

    /**
     * Hides the status bar.
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

    /**
     * Updates the UI based on the current user.
     *
     * @param currentUser The current user.
     */
    private fun updateUI(currentUser: User?) {
        if (currentUser != null) {
            user = currentUser
            login_text.visibility = View.INVISIBLE
            loginBtn.visibility = View.INVISIBLE
            display_name.visibility = View.VISIBLE
            display_name.text = currentUser.getFirstName()
        } else {
            login_text.visibility = View.VISIBLE
            loginBtn.visibility = View.VISIBLE
            display_name.visibility = View.INVISIBLE
        }
    }

    /**
     * Sets up the listeners for the UI.
     */
    private fun setupListeners() {

        /**
         * Closes the menu and the search bar when the screen is clicked.
         */
        screen.setOnClickListener {
            // Close Menu
            closeMenu(true)
            isHandburgerMenuOpen = false

            // Close Search
            closeSearch()
        }

        // Handburger Menu Listeners
        /**
         * Opens/Closes the hamburger menu.
         */
        hamburgerMenu.setOnClickListener {

            if (isHandburgerMenuOpen) {
                closeMenu()
            } else {
                openMenu()
            }

            isHandburgerMenuOpen = !isHandburgerMenuOpen
        }

        /**
         * Goes to the My Profile activity.
         * If the user is not logged in, it will go to the login activity.
         *
         * @see LoginActivity
         * @see MyProfileActivity
         */
        myProfileBtn.setOnClickListener {
            // check if the user val is set
            if (user != null) {
                val intent = Intent(this, MyProfileActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top)
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
            }
        }

        // Search bar Listeners
        /**
         * Opens/Closes the search bar.
         */
        searchIcon.setOnClickListener {
            if (searchEditText.visibility == View.INVISIBLE) {
                openSearch()
            } else {
                closeSearch()
            }
        }

        // Navigatetion Listeners

        /**
         * Opens the login activity.
         *
         * @see LoginActivity
         */
        loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
        }
    }

    override fun onResume() {
        super.onResume()
        setupUI()
        // Close Menu
        closeMenu(true)
        isHandburgerMenuOpen = false

        // Close Search
        closeSearch()
    }
}
