package com.example.psm

import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.psm.helpers.FirebaseHelper
import com.example.psm.models.Saved
import com.squareup.picasso.Picasso

class SavedAdapter(val activity: SavedActivity) :
    RecyclerView.Adapter<SavedAdapter.DestinationViewHolder>() {

    // Instance variables
    private val db = FirebaseHelper()
    private var saves = mutableListOf<Saved>()

    // Default query values
    private var queryFrom : String = "Any"
    private var queryTo : String = "Any"
    private var queryDate : String = randomDate()
    private var queryNights : Int = 7
    private var queryAdults : Int = 2
    private var queryChildren : Int = 0

    /**
     * DestinationViewHolder is a ViewHolder class for RecyclerView that holds views for each card item.
     * @property itemView: View of each card item.
     */
    inner class DestinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val destinationFavorite : ImageButton? = itemView.findViewById(R.id.destination_favorite)
        val destinationName: TextView? = itemView.findViewById(R.id.destination_name)
        val destinationImage: ImageView? = itemView.findViewById(R.id.destination_image)
        val destinationLocation : TextView? = itemView.findViewById(R.id.destination_location)
        val desinationRating : RatingBar? = itemView.findViewById(R.id.destination_rating)
        val destinationDepartureAirport : TextView? = itemView.findViewById(R.id.destination_departure_airport)
        val destinationDate : TextView? = itemView.findViewById(R.id.destination_date)
        val destinationBoard : TextView? = itemView.findViewById(R.id.destination_board)
        val destinationDiscount : TextView? = itemView.findViewById(R.id.destination_discount)
        val destinationPriceBtn : Button? = itemView.findViewById(R.id.destination_price_button)
    }

    // Adapter methods
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val layoutResId = if (viewType == VIEW_TYPE_NO_BOOKINGS) {
            R.layout.item_no_bookings
        } else {
            R.layout.item_holiday
        }

        val itemView = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return DestinationViewHolder(itemView)
    }

    override fun getItemViewType(position: Int): Int {
        return if (saves.isEmpty()) {
            VIEW_TYPE_NO_BOOKINGS
        } else {
            VIEW_TYPE_HOLIDAY
        }
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        if (saves.isEmpty()) {
            return
        }
        val saved = saves[position]
        val destination = saved.destination

        holder.destinationFavorite?.setImageResource(R.drawable.ic_heart_red)

        queryFrom = saved.query["from"].toString()
        queryTo = saved.query["to"].toString()
        queryDate = saved.query["date"].toString()
        queryNights = saved.query["nights"].toString().toInt()
        queryAdults = saved.query["adults"].toString().toInt()
        queryChildren = saved.query["children"].toString().toInt()

        var totalPrice = destination.caluteTotalPrice(queryAdults, queryChildren, queryNights)
        var partySize =  queryAdults + queryChildren

        // Setting Text
        db.getPicture(
            "Destinations",
            destination.getId(),
            onSuccess = { url ->
                if (holder.destinationImage != null) {  // Check if destinationImage is not null
                    Picasso.get()
                        .load(url)
                        .into(holder.destinationImage)
                }
            },
            onFailure = {
                Log.e("HolidayPlannerAdapter", "Failed to get picture for ${destination.getId()} => ${destination.getName()}")
            })

        holder.destinationName?.text = destination.getName()
        holder.desinationRating?.rating = destination.getRating().toFloat()
        holder.destinationLocation?.text = destination.getLocation()

        if (queryFrom == "Any") {
            holder.destinationDepartureAirport?.text = activity.allUkAirports.random()
        } else {
            holder.destinationDepartureAirport?.text = queryFrom
        }

        var dateText = "$queryNights Nights,"
        if (queryChildren <= 0) {
            dateText = dateText.plus(" $queryAdults Adults, ${queryDate}")
        } else {
            dateText = dateText.plus(" $queryAdults Adults, $queryChildren Children, ${queryDate}")
        }

        holder.destinationDate?.text = dateText
        holder.destinationBoard?.text = destination.getBoardType()

        if (queryNights < 5) {
            destination.setDiscount(0.0)
            holder.destinationBoard?.text = "Room Only"
        }

        // Get the current month
        val currentMonth = extractMonth(queryDate)

        val isPeakSeason = destination.getPeakSeason().contains(currentMonth)

        if (isPeakSeason) {
            totalPrice *= 1.1
        }

        if (destination.getDiscount() > 0) {
            val discountedPrice = totalPrice - destination.getDiscount()
            val originalPricePerPerson = totalPrice / partySize
            val discountedPricePerPerson = discountedPrice / partySize
            val moneySaved = originalPricePerPerson - discountedPricePerPerson

            val formattedTotalPrice = String.format("%.2f", totalPrice)
            val formattedMoneySaved = String.format("%.2f", moneySaved)
            val formattedNormalPrice = String.format("%.2f", originalPricePerPerson)
            val formattedDiscountedPrice = String.format("%.2f", discountedPricePerPerson)

            val displayPrice = "£${formattedNormalPrice}PP £${formattedDiscountedPrice}PP"

            val spannableString = SpannableString(displayPrice)
            val strikeThroughSpan = StrikethroughSpan()
            val smallerFontSizeSpan = RelativeSizeSpan(0.8f)

            spannableString.setSpan(strikeThroughSpan, 0, formattedNormalPrice.length + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(smallerFontSizeSpan, 0, formattedNormalPrice.length + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            holder.destinationDiscount?.text = "Was £$formattedTotalPrice, Save £${formattedMoneySaved}PP"
            holder.destinationPriceBtn?.text = spannableString

        } else {
            holder.destinationDiscount?.visibility = View.GONE
            holder.destinationPriceBtn?.text = "£${String.format("%.2f", totalPrice)}"
        }


        // Listeners
        holder.destinationFavorite?.setOnClickListener {
            Log.d("HolidayPlannerAdapter", "Clicked favorite button for ${destination.getId()} => ${destination.getName()}")
            db.removeFromFavorites(
                destination,
                queryFrom,
                queryTo,
                queryDate,
                queryNights,
                queryAdults,
                queryChildren,
                onSuccess = {
                    Toast.makeText(
                        holder.itemView.context,
                        "Removed from favorites",
                        Toast.LENGTH_SHORT
                    ).show()

                    removeItem(holder.adapterPosition)
                },
                onFailure = {
                    Toast.makeText(
                        holder.itemView.context,
                        "Failed to remove from favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    override fun getItemCount(): Int {
        if (saves.isEmpty()) {
            return 1
        }
        return saves.size
    }

    // Helper methods
    /**
     * Sets the data for the adapter
     *
     * @param saves The list of saves to set
     */
    fun setData(saves: List<Saved>) {
        this.saves.clear()
        this.saves.addAll(saves)
        notifyDataSetChanged()
    }

    /**
     * Removes an item from the adapter
     *
     * @param position The position of the item to remove
     */
    fun removeItem(position: Int) {
        saves.removeAt(position)
        notifyItemRemoved(position)
    }

    /**
     * Generates a random date
     *
     * @return A random date in the format DD/MM/YYYY
     */
    fun randomDate(): String {
        val day = (1..28).random()
        val month = (1..12).random()
        val year = (2023..2024).random()
        return "$day/$month/$year"
    }

    /**
     * Extracts the month from a date
     *
     * @param date The date to extract the month from
     * @return The month in the format "January"
     */
    fun extractMonth(date: String): String {
        val dateParts = date.split("/")
        val monthIndex = dateParts[1].toInt() - 1 // Months are 0-indexed
        val monthNames = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return monthNames[monthIndex]
    }

    companion object {
        private const val VIEW_TYPE_NO_BOOKINGS = 0
        private const val VIEW_TYPE_HOLIDAY = 1
    }

}
