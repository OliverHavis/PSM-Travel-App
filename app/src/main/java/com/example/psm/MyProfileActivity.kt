package com.example.psm

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.psm.helpers.FirebaseHelper
import com.example.psm.models.User
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MyProfileActivity : AppCompatActivity() {
    // initialization
    private lateinit var db: FirebaseHelper
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        // initialization
        db = FirebaseHelper()

        // setup View
        hideStatusBar()
        setupUI()
        setupTabs()

        findViewById<Button>(R.id.logout_button).setOnClickListener {
            db.signOut()
            finish()
        }
    }

    /**
     * Gets the current user and updates the UI.
     */
    fun setupUI() {
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
    fun updateUI(currentUser: User?) {
        if (currentUser != null) {
            user = currentUser
            findViewById<TextView>(R.id.user_name).text = user.first_name + " " + user.last_name
            findViewById<TextView>(R.id.user_email).text = user.email
            findViewById<TextView>(R.id.user_phone).text = user.phone
            findViewById<TextView>(R.id.user_address).text = user.address
        }
    }

    /**
     * Sets up the tabs.
     */
    private fun setupTabs() {
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val pagerAdapter = MyPagerAdapter(supportFragmentManager, lifecycle, this)
        viewPager.adapter = pagerAdapter

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Account Settings" else "Cards" // set tab text
        }.attach()
    }

    private fun finishWithAnimation() {
        finish()
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
    }

    // Override onBackPressed() to apply the reverse animation
    override fun onBackPressed() {
        finishWithAnimation()
    }
}

class MyPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, private val activity: MyProfileActivity) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 2 // number of tabs

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> AccountInfoFragment(activity)
            1 -> CardsFragment()
            else -> throw IndexOutOfBoundsException()
        }
    }
}