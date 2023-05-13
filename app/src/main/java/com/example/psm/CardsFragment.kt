package com.example.psm

import CardAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.psm.helpers.FirebaseHelper
import com.example.psm.models.Card
import com.example.psm.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CardsFragment : Fragment() {
    // Variables
    private val db = FirebaseHelper()
    lateinit var recyclerView: RecyclerView
    lateinit var user: User
    private lateinit var adapter: CardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cards, container, false)
        recyclerView = view.findViewById<RecyclerView>(R.id.cards_list)

        CoroutineScope(Dispatchers.Main).launch {
            user = db.getCurrentUser()!!

            // Get the list of cards from the user
            val cardsList = mutableListOf<Card>()

            user.getAllCards(onSuccess = { cards ->
                cardsList.addAll(cards)
                val emptyCard = Card("0", "0", "0", "0", "0", "0")
                cardsList.add(0, emptyCard) // Add an empty card to the beginning of the list

                adapter = CardAdapter(this@CardsFragment, cardsList)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(context)

            }, onFailure = {
                Log.e("CardsFragment", "Failed to get cards")
                val emptyCard = Card("0", "0", "0", "0", "0", "0")
                cardsList.add(0, emptyCard) // Add an empty card to the beginning of the list

                adapter = CardAdapter(this@CardsFragment, cardsList)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(context)
            })
        }

        return view
    }
}
