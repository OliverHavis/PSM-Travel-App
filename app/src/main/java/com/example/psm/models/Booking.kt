package com.example.psm.models

import kotlinx.serialization.Serializable

@Serializable
class Booking(
    val destination: Destination,
    val queryFrom: String,
    val queryTo: String,
    val queryDate: String,
    val queryNights: Int,
    val queryAdults: Int,
    val queryChildren: Int,
    val selectedExtras: List<String>
)