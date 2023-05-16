package com.example.psm

import Booking
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.psm.helpers.FirebaseHelper
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookedAdapter(val activity: BookedHolidaysActivity) :
    RecyclerView.Adapter<BookedAdapter.DestinationViewHolder>() {

    // Instance variables
    private val db = FirebaseHelper()
    private var bookings = mutableListOf<Booking>()

    // Companion object
    companion object {
        private const val VIEW_TYPE_NO_BOOKINGS = 0
        private const val VIEW_TYPE_HOLIDAY = 1
    }

    /**
     * DestinationViewHolder is a ViewHolder class for RecyclerView that holds views for each card item.
     * @property itemView: View of each card item.
     */
    inner class DestinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // No Holiday View
        val noBookingsText: TextView? = itemView.findViewById(R.id.NoBookingsText)

        // Holiday View
        val destinationFavorite : ImageButton? = itemView.findViewById(R.id.destination_favorite)
        val destinationName: TextView? = itemView.findViewById(R.id.destination_name)
        val destinationImage: ImageView? = itemView.findViewById(R.id.destination_image)
        val destinationLocation : TextView? = itemView.findViewById(R.id.destination_location)
        val desinationRating : RatingBar? = itemView.findViewById(R.id.destination_rating)
        val destinationDepartureAirport : TextView? = itemView.findViewById(R.id.destination_departure_airport)
        val destinationDate : TextView? = itemView.findViewById(R.id.destination_date)
        val destinationBoard : TextView? = itemView.findViewById(R.id.destination_board)
        val destinationExcursionsList : LinearLayout? = itemView.findViewById(R.id.excursionList)
        val destinationExcursionsItemsList : LinearLayout? = itemView.findViewById(R.id.excursionItemsList)
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
        return if (bookings.isEmpty()) {
            VIEW_TYPE_NO_BOOKINGS
        } else {
            VIEW_TYPE_HOLIDAY
        }
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        // Check if there are no bookings
        if (bookings.isEmpty()) {
            holder.noBookingsText?.text = "You have no bookings!"
            return
        }

        // Get booking
        val booking = bookings[position]
        val destination = booking.destination

        // Get query details
        val queryDate = booking.queryDate
        val queryAdults = booking.queryAdults
        val queryChildren = booking.queryChildren
        val totalPrice = booking.totalPrice

        // Hide unneeded elements
        holder.destinationFavorite?.visibility = View.GONE
        holder.desinationRating?.visibility = View.GONE
        holder.destinationDiscount?.visibility = View.GONE
        holder.destinationPriceBtn?.visibility = View.GONE

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
                Log.e("HolidayPlannerAdapter", "Failed to get picture for ${destination?.getId()} => ${destination?.getName()}")
            })

        holder.destinationName?.text = destination.getName()
        holder.destinationLocation?.text = destination.getLocation()
        holder.destinationDepartureAirport?.text = booking.queryFrom

        var dateText = "${booking.queryNights} Nights,"
        if (booking.queryChildren <= 0) {
            dateText = dateText.plus(" $queryAdults Adults, ${queryDate}")
        } else {
            dateText = dateText.plus(" $queryAdults Adults, $queryChildren Children, ${queryDate}")
        }
        holder.destinationDate?.text = dateText
        holder.destinationBoard?.text = destination?.getBoardType()

        // Setting Excursions
        if (booking.selectedExtras.isNotEmpty()) {
            holder.destinationExcursionsList?.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.Main).launch {

                booking.selectedExtras.forEach() {
                    val excursion = db.getExcursion(it)
                    val excursionName = excursion["name"] as String
                    addExcursion(excursionName, holder)
                }
            }

        } else {
            holder.destinationExcursionsList?.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        if (bookings.isEmpty()) {
            return 1
        }
        return bookings.size
    }

    // Helper methods
    /**
     * Sets the list of bookings
     * @param bookings: List of bookings
     */
    fun setBookings(bookings: MutableList<Booking>) {
        this.bookings = bookings

        println("Bookings: ${this.bookings}")

        notifyDataSetChanged()
    }

    fun addExcursion(excursionText: String, holder: DestinationViewHolder) {
        val excursionTextView = TextView(activity)
        excursionTextView.text = Html.fromHtml(excursionText)
        excursionTextView.textSize = 16F
        excursionTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
        excursionTextView.setTextColor(activity.resources.getColor(R.color.dark_blue))
        excursionTextView.setPadding(0, 0, 0, 5)
        holder.destinationExcursionsItemsList?.addView(excursionTextView)
    }
}
