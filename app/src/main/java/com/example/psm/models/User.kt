package com.example.psm.models

import android.util.Log

class User(uid: String, first_name: String, last_name: String, email: String, phone: String, address: String = "", profile_picture: String = "") {
    var uid: String = uid
    var first_name: String = first_name
    var last_name: String = last_name
    var email: String = email
    var phone: String = phone
    var address: String = address
    var profile_picture: String = profile_picture

    fun getFullName(): String {
        return first_name + " " + last_name
    }

    fun getSplitAddress(): Map<String, String> {
        // Split the address into a map of address components (street, city, state, Country, zip) by splitting on the commas. if there are two commas, then the address is missing the a component (e.g. street, city, state, zip) and set it as an empty string.
        val addressComponents = address.split(",")
        val addressMap = mutableMapOf<String, String>()
        addressMap["street"] = addressComponents[0]
        addressMap["city"] = addressComponents[1]
        addressMap["state"] = addressComponents[2]
        addressMap["country"] = addressComponents[3]
        addressMap["zip"] = addressComponents[4]
        return addressMap
    }

    fun updateField(feild: String, value: String) {
        when (feild) {
            "first_name" -> first_name = value
            "last_name" -> last_name = value
            "email" -> email = value
            "phone" -> phone = value
            "address" -> address = value
            "profile_picture" -> profile_picture = value
        }

        Log.d("User", "Updated $feild to $value")
    }


}


