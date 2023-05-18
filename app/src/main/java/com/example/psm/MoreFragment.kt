package com.example.psm

import CardAdapter
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.psm.helpers.FirebaseHelper
import com.example.psm.models.User

class MoreFragment(activity: MyProfileActivity) : Fragment() {
    // Variables
    private val db = FirebaseHelper()
    lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_more, container, false)

        // basic listners
        view.findViewById<Button>(R.id.download_accountBtn).setOnClickListener {
            val intent = Intent(activity, PdfViewerActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<Button>(R.id.delete_accountBtn).setOnClickListener {
            showConfirmDialog()
        }

        view.findViewById<Button>(R.id.logout_button).setOnClickListener {//Logout
            db.signOut()
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun showConfirmDialog() {
        val dialog = AlertDialog.Builder(activity)
        dialog.setTitle("Delete Account")
        dialog.setMessage("Are you sure you want to delete your account?")
        dialog.setPositiveButton("Yes") { dialog, which ->
            db.deleteAccount(
                onSuccess = {
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                },
                onFailure = {
                    Toast.makeText(activity, "Failed to delete account. Please try again", Toast.LENGTH_SHORT).show()
                }
            )
        }
        dialog.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        dialog.show()
    }
}
