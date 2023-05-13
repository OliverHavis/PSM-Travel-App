package com.example.psm.models

data class Destination(
    val name: String,
    val location: String,
    val pricePerAdult: Double,
    val pricePerChild: Double,
    val boardType: String,
    val discount: Double,
    val peakSeason: String,
    val rating: Double
)
