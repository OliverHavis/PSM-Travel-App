package com.example.psm.models

import android.util.Log
import com.example.psm.helpers.FirebaseHelper
import com.google.firebase.storage.FirebaseStorage
import kotlin.math.log

class User(
    private val uid: String,
    private var firstName: String,
    private var lastName: String,
    private var email: String,
    private var phone: String,
    private var address: String? = null,
    private var profilePicture: Boolean = false
) {

    private val db = FirebaseHelper()
    private val storage : FirebaseStorage = FirebaseStorage.getInstance()
    private var cards: MutableList<Card> = mutableListOf()

    /** Getters  */
    fun getFullName(): String {
        return "$firstName $lastName"
    }

    fun getFirstName(): String {
        return firstName
    }

    fun getLastName(): String {
        return lastName
    }

    fun getEmail(): String {
        return email
    }

    fun getPhone(): String {
        return phone
    }

    fun getAddress(): String? {
        return address
    }

    fun getProfilePicture(): Boolean {
        return profilePicture
    }

    fun getProfilePicUrl(onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        var storageRef = storage.reference.child("profile_pictures/default.jpeg")
        println(this.uid)

        if (this.profilePicture) {
            storageRef = storage.reference.child("profile_pictures/${this.uid}.jpg")
            println("profile_pictures/${this.uid}.jpg")
        }

        storageRef.downloadUrl.addOnSuccessListener { uri ->
            onSuccess(uri.toString())
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    fun getUid(): String {
        return uid
    }

    fun getSplitAddress(): Map<String, String> {
        val addressComponents = getAddress()?.split(",") ?: listOf()
        val addressMap = mutableMapOf<String, String>()
        addressMap["street"] = addressComponents.getOrElse(0) { "" }
        addressMap["city"] = addressComponents.getOrElse(1) { "" }
        addressMap["state"] = addressComponents.getOrElse(2) { "" }
        addressMap["country"] = addressComponents.getOrElse(3) { "" }
        addressMap["zip"] = addressComponents.getOrElse(4) { "" }
        return addressMap
    }

    fun getAllCards(onSuccess: (List<Card>) -> Unit, onFailure: (Exception) -> Unit) {
        println("Getting all cards")
        // get all cards
        db.getAllCards(
            onSuccess = { cardList ->
                println("Cards From USer Class: $cardList")
                onSuccess(cardList)
            },
            onFailure = { exception ->
                Log.e("User", "Error getting cards: ${exception.message}")
                onFailure(exception)
            }
        )
    }

    /** Setters  */
    fun setFirstName(firstName: String) {
        this.firstName = firstName
    }

    fun setLastName(lastName: String) {
        this.lastName = lastName
    }

    fun setEmail(email: String) {
        // Encrypt the email
        this.email = email
    }

    fun setPhone(phone: String) {
        // Encrypt the phone
        this.phone = phone
    }

    fun setAddress(street : String, city : String, state : String, country : String, zip : String) {
        // add the address components to a string
        val address = listOf(street, city, state, country, zip)
            .filter { it.isNotBlank() }
            .joinToString(", ")

        this.address = address
    }

    fun setProfilePicture(profilePicture: Boolean) {
        this.profilePicture = profilePicture
    }

    /** Other methods  */
//    fun getPrimaryCard(onSuccess: (Card?) -> Unit, onFailure: (Exception) -> Unit) {
//        db.getPrimaryCard(this.uid, onSuccess, onFailure)
//    }
}

