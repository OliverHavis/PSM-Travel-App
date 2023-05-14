package com.example.psm

import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.psm.helpers.FirebaseHelper
import com.example.psm.models.Destination
import com.squareup.picasso.Picasso

class HolidayPlannerAdapter(val activity: FlightPlannerActivity) :
    RecyclerView.Adapter<HolidayPlannerAdapter.DestinationViewHolder>() {

    private val db = FirebaseHelper()
    private var destinations = mutableListOf<Destination>()

    // Default query values
    private var queryFrom : String = "Any"
    private var queryTo : String = "Any"
    private var queryDate : String = randomDate()
    private var queryNights : Int = 7
    private var queryAdults : Int = 2
    private var queryChildren : Int = 0


    inner class DestinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Define the views for each card item
        val destinationFavorite : ImageButton = itemView.findViewById(R.id.destination_favorite)
        val destinationName: TextView = itemView.findViewById(R.id.destination_name)
        val destinationImage: ImageView = itemView.findViewById(R.id.destination_image)
        val destinationLocation : TextView = itemView.findViewById(R.id.destination_location)
        val desinationRating : RatingBar = itemView.findViewById(R.id.destination_rating)
        val destinationDepartureAirport : TextView = itemView.findViewById(R.id.destination_departure_airport)
        val destinationDate : TextView = itemView.findViewById(R.id.destination_date)
        val destinationBoard : TextView = itemView.findViewById(R.id.destination_board)
        val destinationDiscount : TextView = itemView.findViewById(R.id.destination_discount)
        val destinationPriceBtn : Button = itemView.findViewById(R.id.destination_price_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        // Inflate the appropriate layout based on the view type
        val layoutResId = R.layout.item_holiday
        val itemView = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return DestinationViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        val destination = destinations[position]

        var totalPrice = destination.caluteTotalPrice(queryAdults, queryChildren, queryNights)
        var partySize =  queryAdults + queryChildren

        // Setting Text
        db.getPicture(
            "Destinations",
            destination.getId(),
            onSuccess = { url ->
                Picasso.get()
                    .load(url)
                    .into(holder.destinationImage)
            },
            onFailure = {
                Log.e("HolidayPlannerAdapter", "Failed to get picture for ${destination.getId()} => ${destination.getName()}")
            })

        holder.destinationName.text = destination.getName()
        holder.desinationRating.rating = destination.getRating().toFloat()
        holder.destinationLocation.text = destination.getLocation()

        if (queryFrom == "Any") {
            holder.destinationDepartureAirport.text = activity.allUkAirports.random()
        } else {
            holder.destinationDepartureAirport.text = queryFrom
        }

        var dateText = "$queryNights Nights,"
        if (queryChildren <= 0) {
            dateText = dateText.plus(" $queryAdults Adults, ${queryDate}")
        } else {
            dateText = dateText.plus(" $queryAdults Adults, $queryChildren Children, ${queryDate}")
        }

        holder.destinationDate.text = dateText
        holder.destinationBoard.text = destination.getBoardType()

        if (queryNights < 5) {
            destination.setDiscount(0.0)
            holder.destinationBoard.text = "Room Only"
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

            holder.destinationDiscount.text = "Was £$formattedTotalPrice, Save £${formattedMoneySaved}PP"
            holder.destinationPriceBtn.text = spannableString

        } else {
            holder.destinationDiscount.visibility = View.GONE
            holder.destinationPriceBtn.text = "£${String.format("%.2f", totalPrice)}"
        }

        // Listeners
        holder.destinationFavorite.setOnClickListener {
            // toggle src of image button from ic_heart to ic_heart_red
            if (holder.destinationFavorite.tag == "ic_heart") {
                db.addToFavorites(
                    destination,
                    queryFrom,
                    queryTo,
                    queryDate,
                    queryNights,
                    queryAdults,
                    queryChildren,
                    onSuccess = {
                        holder.destinationFavorite.setImageResource(R.drawable.ic_heart_red)
                        holder.destinationFavorite.tag = "ic_heart_red"
                        Toast.makeText(holder.itemView.context, "Added to favorites", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = {
                        Toast.makeText(holder.itemView.context, "Failed to add to favorites", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                db.removeFromFavorites(
                    destination,
                    queryFrom,
                    queryTo,
                    queryDate,
                    queryNights,
                    queryAdults,
                    queryChildren,
                    onSuccess = {
                        holder.destinationFavorite.setImageResource(R.drawable.ic_heart)
                        holder.destinationFavorite.tag = "ic_heart"
                        Toast.makeText(holder.itemView.context, "Removed from favorites", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = {
                        Toast.makeText(holder.itemView.context, "Failed to remove from favorites", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    fun randomDate(): String {
        val day = (1..28).random()
        val month = (1..12).random()
        val year = (2023..2024).random()
        return "$day/$month/$year"
    }

    override fun getItemCount(): Int {
        // Return the size of the card list
        return destinations.size
    }

    override fun getItemViewType(position: Int): Int {
        // Determine the view type based on the position
        return if (position == 0) {
            VIEW_TYPE_FIRST_CARD
        } else {
            VIEW_TYPE_NORMAL_CARD
        }
    }

    fun setData(destinations: List<Destination>) {
        this.destinations = destinations as MutableList<Destination>
        notifyDataSetChanged()
    }

    fun setQueryData(
        querySelectedFrom: String,
        querySelectedDestination: String,
        departureDate: String,
        nights: Int,
        adultsNum: Int,
        childrenNum: Int
    ) {
        queryFrom = querySelectedFrom
        queryTo = querySelectedDestination
        queryDate = departureDate
        queryNights = nights
        queryAdults = adultsNum
        queryChildren = childrenNum

        notifyDataSetChanged()
    }

    companion object {
        private const val VIEW_TYPE_FIRST_CARD = 0
        private const val VIEW_TYPE_NORMAL_CARD = 1
    }

}
