package com.example.psm

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
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
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MyProfileActivity : AppCompatActivity() {
    // Constants
    private val REQUEST_IMAGE_FROM_GALLERY = 1

    // Variables
    private lateinit var db: FirebaseHelper
    private lateinit var user: User
    private var selectedImageUri: Uri = Uri.EMPTY
    private lateinit var picView: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        // initialization
        db = FirebaseHelper()

        // setup View
        hideStatusBar()
        setupUI()
        setupTabs()

        // basic listners
        findViewById<Button>(R.id.logout_button).setOnClickListener {//Logout
            db.signOut()
            finish()
        }

        findViewById<ImageView>(R.id.profile_picture).setOnClickListener {// Profile Picture
            showProfilePictureDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_FROM_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data!!
            Picasso.get().load(selectedImageUri).into(picView)
        }
    }

    /**
     * Shows the profile picture dialog
     *
     * This dialog allows the user to upload a profile picture
     * and save it to Firebase Storage
     *
     */
    private fun showProfilePictureDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_profile_picture)

        picView = dialog.findViewById<ImageView>(R.id.profile_picture_image_view)
        val uploadButton = dialog.findViewById<Button>(R.id.upload_picture_button)
        val saveButton = dialog.findViewById<Button>(R.id.save_button)

        // Load profile picture
        user.getProfilePicUrl(
            onSuccess = { url ->
                println(url)
                Picasso.get().load(url).into(picView)
            },
            onFailure = { exception ->
                Log.e("MyProfileActivity", "Error getting profile picture url: $exception")
            }
        )

        // Set click listeners for the buttons
        uploadButton.setOnClickListener {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, REQUEST_IMAGE_FROM_GALLERY)

            // Load the image into the image view
            if (selectedImageUri != Uri.EMPTY) {
                Picasso.get().load(selectedImageUri).into(picView)
            }
        }

        saveButton.setOnClickListener {
            // Upload the image to Firebase Storage
            db.uploadProfilePicture(
                selectedImageUri,
                onSuccess = {
                    Log.d("MyProfileActivity", "Successfully uploaded profile picture")
                    user.setProfilePicture(true)
                    db.updateUser(user, onSuccess = {
                        Log.d("MyProfileActivity", "Successfully updated user")
                        updateUI(user)
                    }, onFailure = { exception ->
                        Log.e("MyProfileActivity", "Error updating user: $exception")
                    })
                },
                onFailure = { exception ->
                    Log.e("MyProfileActivity", "Error uploading profile picture: $exception")
                }
            )

            dialog.dismiss()
        }


        dialog.show()
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
            findViewById<TextView>(R.id.user_name).text = user.getFullName()
            findViewById<TextView>(R.id.user_email).text = user.getEmail()
            findViewById<TextView>(R.id.user_phone).text = user.getPhone()
            findViewById<TextView>(R.id.user_address).text = user.getAddress()
            println("Getting profile picture")
            user.getProfilePicUrl(
                onSuccess = { url ->
                    println("URL: $url")
                    Picasso.get()
                        .load(url)
                        .into(findViewById<ImageView>(R.id.profile_picture))
                },
                onFailure = { e ->
                    Log.e("MyProfileActivity", "Error getting profile picture: $e")
                }
            )
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

    /**
     * Finishes the activity with an animation.
     */
    private fun finishWithAnimation() {
        finish()
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
    }

    // Override onBackPressed() to apply the reverse animation
    override fun onBackPressed() {
        finishWithAnimation()
    }
}

/**
 * The pager adapter for the tabs.
 *
 * @param fragmentManager The fragment manager.
 * @param lifecycle The lifecycle.
 * @param activity The activity.
 */
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