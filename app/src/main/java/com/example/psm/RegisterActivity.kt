package com.example.psm

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.psm.helpers.FirebaseHelper

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        hideStatusBar()

        db = FirebaseHelper()


        // Initialization
        val firstNameInput = findViewById<EditText>(R.id.registration_first_name)
        val lastNameInput = findViewById<EditText>(R.id.registration_last_name)
        val phoneInput = findViewById<EditText>(R.id.registration_phone_input)
        val emailInput = findViewById<EditText>(R.id.registration_email_input)
        val passwordInput = findViewById<EditText>(R.id.registration_password_input)
        val registerButton = findViewById<Button>(R.id.register_button)
        val progressBar = findViewById<ProgressBar>(R.id.registration_progress_bar)

        registerButton.setOnClickListener {
            val firstName = firstNameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            progressBar.visibility = View.VISIBLE

            // Validate email and password
            if (isAnyFieldEmpty(firstName, lastName, email, password)) {
                showToast("Please fill in all the required fields.")
                progressBar.visibility = View.INVISIBLE
            } else if (!isValidEmail(email)) {
                showToast("Invalid email address.")
                progressBar.visibility = View.INVISIBLE
            } else if (!isValidPassword(password)) {
                showToast("Invalid password.")
                progressBar.visibility = View.INVISIBLE
            } else {
                db.signUp(email, password, firstName, lastName, phone,
                    onSuccess = {
                        progressBar.visibility = View.INVISIBLE
                        showToast("Successfully registered.")
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top)
                    },
                    onFailure = {
                        progressBar.visibility = View.INVISIBLE
                        showToast("Failed to register.")
                    })
            }
        }
    }

    /**
     * Checks if any of the required fields are empty.
     *
     * @param name The user's name.
     * @param email The user's email address.
     * @param password The user's password.
     * @return True if any field is empty, false otherwise.
     */
    private fun isAnyFieldEmpty(firstName: String, lastName: String, email: String, password: String): Boolean {
        return firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()
    }

    /**
     * Validates the email address.
     *
     * @param email The email address to validate.
     * @return True if the email address is valid, false otherwise.
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z]{2,6}$"
        return email.matches(emailRegex.toRegex(RegexOption.IGNORE_CASE))
    }

    /**
     * Validates the password.
     *
     * @param password The password to validate.
     * @return True if the password is valid, false otherwise.
     */
    private fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$"
        return password.matches(passwordRegex.toRegex())
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
     * Displays a toast message.
     *
     * @param message The message to display.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
