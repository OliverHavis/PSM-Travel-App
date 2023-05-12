package com.example.psm.models

import com.example.psm.helpers.FirebaseHelper

class Card(
    private val uid: String? = null,
    private val userID: String? = null,
    private val card_number : String,
    private val card_holder : String,
    private val card_expiry : String,
    private val card_cvv : String,
    private var primary : Boolean = false
) {
    fun getUid() : String? {
        return uid
    }

    fun getUserID() : String? {
        return userID
    }

    fun getCardHolder() : String {
        return card_holder
    }

    fun getExpiryDate() : String {
        return card_expiry
    }

    fun getCvv() : String {
        return card_cvv
    }

    fun getCardNumberFull() : String {
        return card_number
    }

    fun isPrimary() : Boolean {
        return primary
    }

    fun setPrimary(primary: Boolean) {
        this.primary = primary
    }

    // Methods
    fun getCardNumber(): String {
        val cardNumber = getCardNumberFull()
        val visibleDigits = cardNumber.takeLast(4)
        val hiddenDigitsCount = cardNumber.length - 4
        val hiddenDigits = "*".repeat(hiddenDigitsCount)

        val formattedCardNumber = hiddenDigits.chunked(4).joinToString(" ") + " " + visibleDigits
        return formattedCardNumber
    }

}