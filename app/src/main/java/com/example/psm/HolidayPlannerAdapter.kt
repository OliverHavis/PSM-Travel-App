package com.example.psm

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.psm.helpers.FirebaseHelper
import com.example.psm.models.Card

class HolidayPlannerAdapter (
//    private var destinations: List<Destination>
) :
    RecyclerView.Adapter<HolidayPlannerAdapter.DestinationViewHolder>() {

    private val db = FirebaseHelper()

    inner class DestinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Define the views for each card item
        val destinationFavorite : ImageButton = itemView.findViewById(R.id.destination_favorite)
        val destinationName: TextView = itemView.findViewById(R.id.destination_name)
        val destinationImage: ImageView = itemView.findViewById(R.id.destination_image)
        val destinationLocation : TextView = itemView.findViewById(R.id.destination_location)
        val destinationDepartureAirport : TextView = itemView.findViewById(R.id.destination_departure_airport)
        val destinationDate : TextView = itemView.findViewById(R.id.destination_date)
        val destinationBoard : TextView = itemView.findViewById(R.id.destination_board)
        val destinationPriceBtn : Button = itemView.findViewById(R.id.destination_price_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        // Inflate the appropriate layout based on the view type
        val layoutResId = R.layout.item_holiday
        val itemView = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return DestinationViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {

        holder.destinationFavorite.setOnClickListener {
            // toggle src of image button from ic_heart to ic_heart_red
            if (holder.destinationFavorite.tag == "ic_heart") {
                holder.destinationFavorite.setImageResource(R.drawable.ic_heart_red)
                holder.destinationFavorite.tag = "ic_heart_red"
                Toast.makeText(holder.itemView.context, "Added to favorites", Toast.LENGTH_SHORT).show()
            } else {
                holder.destinationFavorite.setImageResource(R.drawable.ic_heart)
                holder.destinationFavorite.tag = "ic_heart"
                Toast.makeText(holder.itemView.context, "Removed from favorites", Toast.LENGTH_SHORT).show()
            }

            // Add to favorites
        }

//        val card = cardList[position]
//
//        // card body listener
//        holder.itemView.setOnClickListener {
//            if (position == 0) {
//                // Show the card info form
//                showAddDialogForm(holder.itemView.context)
//            } else {
//                // Show the card info form
//                showPrimaryDialogForm(holder.itemView.context, card)
//            }
//        }
    }

    override fun getItemCount(): Int {
        // Return the size of the card list
//        return cardList.size
        return 3
    }

    override fun getItemViewType(position: Int): Int {
        // Determine the view type based on the position
        return if (position == 0) {
            VIEW_TYPE_FIRST_CARD
        } else {
            VIEW_TYPE_NORMAL_CARD
        }
    }

    companion object {
        private const val VIEW_TYPE_FIRST_CARD = 0
        private const val VIEW_TYPE_NORMAL_CARD = 1
    }

//    fun refreshFragmentView() {
//        fragment.user.getAllCards(
//            onSuccess = { cards ->
//                val cardsList = mutableListOf<Card>()
//                cardsList.addAll(cards)
//                val emptyCard = Card("0", "0", "0", "0", "0", "0")
//                cardsList.add(0, emptyCard) // Add an empty card to the beginning of the list
//
//                val adapter = CardAdapter(fragment, cardsList)
//                fragment.recyclerView.adapter = adapter
//                adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
//            },
//            onFailure = {
//                Log.e("CardsFragment", "Failed to get cards")
//                val emptyCard = Card("0", "0", "0", "0", "0", "0")
//                val cardsList = mutableListOf(emptyCard) // Create a list with only the empty card
//
//                val adapter = CardAdapter(fragment, cardsList)
//                fragment.recyclerView.adapter = adapter
//                adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
//            }
//        )
//    }

    //  Dialogs

}
