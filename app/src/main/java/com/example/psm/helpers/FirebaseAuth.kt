package com.example.psm.helpers

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import com.example.psm.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


class FirebaseHelper {

    // initiaization
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Get the current user.
     *
     * @return FirebaseUser
     */
    suspend fun getCurrentUser(): User? {
        Log.d(TAG, "${auth.currentUser}")
        // Check if the user is signed in (non-null)
        val firebaseUser = auth.currentUser ?: return null

        Log.e(TAG, "${auth.currentUser!!.uid}")
        // Get the user data from Firestore
        return try {
            val document = firestore.collection("Users").document(firebaseUser.uid).get().await()
            if (document.exists()) {
                User(
                    document.getString("uid")!!,
                    document.getString("first_name")!!,
                    document.getString("last_name")!!,
                    document.getString("email")!!,
                    document.getString("phone")!!
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user data", e)
            null
        }
    }


    /**
     * Create a new user with the provided email and password and add the user to the database.
     *
     * @param email The email of the user.
     * @param password The password of the user.
     * @param name The name of the user.
     * @param phone The phone number of the user.
     * @param onSuccess A function to be called when the user is successfully created.
     * @param onFailure A function to be called when the user creation fails.
     * @return Unit
     * @see [FirebaseAuth.createUserWithEmailAndPassword]
     * @see [FirebaseFirestore.set]
     * @see [FirebaseFirestore.collection]
     * @see [FirebaseFirestore.document]
     */
    fun signUp(
        email: String,
        password: String,
        first_name: String,
        last_name: String,
        phone: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "createUserWithEmail:success")

                    val user = User(auth.currentUser!!.uid, first_name, last_name, email, phone)

                    firestore.collection("Users").document(auth.currentUser!!.uid)
                        .set(user)
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!")
                            println("DocumentSnapshot successfully written!")
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            Log.w(ContentValues.TAG, "Error writing document", e)
                            println("Error writing document: $e")
                            onFailure(e)
                        }
                } else {
                    Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                    println("Error: ${task.exception}")
                    task.exception?.let { onFailure(it) }
                }
            }
    }

    /**
     * Sign in the user with the provided email and password.
     *
     * @param email The email of the user.
     * @param password The password of the user.
     * @param onSuccess A function to be called when the user is successfully signed in.
     * @param onFailure A function to be called when the user sign in fails.
     * @return Unit
     * @see [FirebaseAuth.signInWithEmailAndPassword]
     */
    fun signIn(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    onFailure()
                }
            }
    }

    /**
     * Sign out the user.
     *
     * @see [FirebaseAuth.signOut]
     */
    fun signOut() {
        auth.signOut()
    }

}