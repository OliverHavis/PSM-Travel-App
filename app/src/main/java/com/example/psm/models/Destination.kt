package com.example.psm.models

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class Destination(
    private val id: String,
    private val name: String,
    private val location: List<String>,
    private val pricePerAdult: Double,
    private val pricePerChild: Double,
    private val boardType: String,
    private var discount: Double = 0.0,
    private val peakSeason: String,
    private val rating: Double
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readString()!!,
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeStringList(location)
        parcel.writeDouble(pricePerAdult)
        parcel.writeDouble(pricePerChild)
        parcel.writeString(boardType)
        parcel.writeDouble(discount)
        parcel.writeString(peakSeason)
        parcel.writeDouble(rating)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Destination> {
        override fun createFromParcel(parcel: Parcel): Destination {
            return Destination(parcel)
        }

        override fun newArray(size: Int): Array<Destination?> {
            return arrayOfNulls(size)
        }
    }
    // Getters
    fun getId(): String {
        return id
    }

    fun getName(): String {
        return name
    }

    fun getLocation(): String {
        println(location)
        return location.joinToString(", ")
    }

    fun getPricePerAdult(): Double {
        return pricePerAdult
    }

    fun getPricePerChild(): Double {
        return pricePerChild
    }

    fun getBoardType(): String {
        return boardType
    }

    fun getDiscount(): Double {
        return discount
    }

    fun getPeakSeason(): List<String> {
        val monthNames = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

        val months = mutableListOf<String>()

        val seasonRanges = peakSeason.split("-")
        for (seasonRange in seasonRanges) {
            val rangeMonths = seasonRange.splitToSequence("-", ignoreCase = true)
                .map { it.trim() }
                .toList()
            if (rangeMonths.size == 2) {
                val startMonth = rangeMonths[0].capitalize(Locale.getDefault())
                val endMonth = rangeMonths[1].capitalize(Locale.getDefault())
                val startMonthIndex = monthNames.indexOf(startMonth)
                val endMonthIndex = monthNames.indexOf(endMonth)
                if (startMonthIndex != -1 && endMonthIndex != -1) {
                    if (startMonthIndex <= endMonthIndex) {
                        val monthsInRange = monthNames.subList(startMonthIndex, endMonthIndex + 1)
                        months.addAll(monthsInRange)
                    } else {
                        val monthsInRange = monthNames.subList(startMonthIndex, monthNames.size)
                        months.addAll(monthsInRange)
                        months.addAll(monthNames.subList(0, endMonthIndex + 1))
                    }
                }
            }
        }

        return months
    }

    fun getRating(): Double {
         return rating
    }

    // Setters
    fun setDiscount(discount: Double) {
        this.discount = discount
    }

    // Methods
    fun caluteTotalPrice(adults: Int, children: Int = 0, nights : Int): Double {
        return ((pricePerAdult * adults) + (pricePerChild * children)) * nights
    }
}

