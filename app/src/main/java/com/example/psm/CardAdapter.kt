
import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.psm.CardsFragment
import com.example.psm.R
import com.example.psm.helpers.FirebaseHelper
import com.example.psm.models.Card
import com.example.psm.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CardAdapter(
    private val fragment: CardsFragment,
    private var cardList: List<Card>
    ) :
    RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    private val db = FirebaseHelper()

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Define the views for each card item
        val newCardIcon: ImageView? = itemView.findViewById<ImageView>(R.id.card_info_add)
        val delCardIcon: ImageView? = itemView.findViewById(R.id.card_info_delete)
        val primaryCardIcon: ImageView? = itemView.findViewById(R.id.card_info_primary)

        val cardNumber: TextView? = itemView.findViewById(R.id.card_number)
        val cardHolder: TextView? = itemView.findViewById(R.id.card_holder)
        val cardExpiry: TextView? = itemView.findViewById(R.id.card_expiry)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        Log.d("CardAdapter", "Card list size: ${cardList.size}")

        // Inflate the appropriate layout based on the view type
        val layoutResId = if (viewType == VIEW_TYPE_FIRST_CARD) {
            R.layout.item_new_card_info
        } else {
            R.layout.item_card_info
        }
        val itemView = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return CardViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        Log.d("CardAdapter", "Binding card at position: $position")
        // Bind the data from the card list to the views in the CardViewHolder
        val card = cardList[position]

        holder.cardNumber?.text = card.getCardNumber()
        holder.cardHolder?.text = card.getCardHolder()
        holder.cardExpiry?.text = card.getExpiryDate()
        if (card.isPrimary()) {
            holder.primaryCardIcon?.visibility = View.VISIBLE
        } else {
            holder.primaryCardIcon?.visibility = View.GONE
        }

        // Icon listeners
        /// Add card icon listener
        holder.newCardIcon?.setOnClickListener {
            // Show the card info form
            showAddDialogForm(holder.itemView.context)
        }

        /// Delete card icon listener
        holder.delCardIcon?.setOnClickListener {
            showDeleteDialogForm(holder.itemView.context, card)
        }

        // card body listener
        holder.itemView.setOnClickListener {
            if (position == 0) {
                // Show the card info form
                showAddDialogForm(holder.itemView.context)
            } else {
                // Show the card info form
                showPrimaryDialogForm(holder.itemView.context, card)
            }
        }
    }

    override fun getItemCount(): Int {
        // Return the size of the card list
        return cardList.size
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

    fun refreshFragmentView() {
        fragment.user.getAllCards(
            onSuccess = { cards ->
                val cardsList = mutableListOf<Card>()
                cardsList.addAll(cards)
                val emptyCard = Card("0", "0", "0", "0", "0", "0")
                cardsList.add(0, emptyCard) // Add an empty card to the beginning of the list

                val adapter = CardAdapter(fragment, cardsList)
                fragment.recyclerView.adapter = adapter
                adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
            },
            onFailure = {
                Log.e("CardsFragment", "Failed to get cards")
                val emptyCard = Card("0", "0", "0", "0", "0", "0")
                val cardsList = mutableListOf(emptyCard) // Create a list with only the empty card

                val adapter = CardAdapter(fragment, cardsList)
                fragment.recyclerView.adapter = adapter
                adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
            }
        )
    }

    //  Dialogs
    private fun showAddDialogForm(context: Context) {
        // Create a dialog window with a custom layout for the card info form
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_card_info_form)

        val window = dialog.window
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Find the form fields and buttons in the dialog layout
        val cardNumberEditText = dialog.findViewById<EditText>(R.id.card_number_edit_text)
        val cardHolderEditText = dialog.findViewById<EditText>(R.id.card_holder_edit_text)
        val cardExpiryEditText = dialog.findViewById<EditText>(R.id.card_expiry_edit_text)
        val cvvEditText = dialog.findViewById<EditText>(R.id.cvv_edit_text)
        val isPrimaryCardCheckBox = dialog.findViewById<CheckBox>(R.id.is_primary_checkbox)
        val submitButton = dialog.findViewById<Button>(R.id.submit_button)
        val cancelButton = dialog.findViewById<Button>(R.id.cancel_button)

        // Save card listener
        submitButton.setOnClickListener {
            val cardNumber = cardNumberEditText.text.toString()
            val cardHolder = cardHolderEditText.text.toString()
            val cardExpiry = cardExpiryEditText.text.toString()
            val cvv = cvvEditText.text.toString()
            val isPrimaryCard = isPrimaryCardCheckBox.isChecked

            // Validate the card info and if they are foucsed on the invalid field and show an error
            if (cardNumber.length != 16) {
                cardNumberEditText.requestFocus()
                cardNumberEditText.error = "Invalid card number"
                return@setOnClickListener
            }

            if (cardHolder.isEmpty()) {
                cardHolderEditText.requestFocus()
                cardHolderEditText.error = "Invalid card holder"
                return@setOnClickListener
            }

            //if card expiry is not in the format of MM/YY
            if (cardExpiry.length != 5) {
                cardExpiryEditText.requestFocus()
                cardExpiryEditText.error = "Invalid card expiry"
                return@setOnClickListener
            }

            if (cvv.length != 3) {
                cvvEditText.requestFocus()
                cvvEditText.error = "Invalid CVV"
                return@setOnClickListener
            }

            val newCard = Card("0", "0", cardNumber, cardHolder, cardExpiry, cvv, isPrimaryCard) // 0 is a placeholder for the card id and wont be used

            // Add the card to the database
            db.addCard(
                newCard,
                onSuccess = {
                    Toast.makeText(context, "Card added successfully", Toast.LENGTH_SHORT).show()
                    Log.d("CardAdapter", "Card added successfully")
                    refreshFragmentView()
                },
                onFailure = { e ->
                    when (e.toString()) {
                        "Maximum number of cards reached" -> {
                            Toast.makeText(context, "Maximum number of cards reached", Toast.LENGTH_SHORT).show()
                            Log.d("CardAdapter", "Maximum number of cards reached")
                        }
                        "Card number already exists" -> {
                            Toast.makeText(context, "Card number already exists", Toast.LENGTH_SHORT).show()
                            Log.d("CardAdapter", "Card already exists")
                        }
                        else -> {
                            Toast.makeText(context, "Failed to add card", Toast.LENGTH_SHORT).show()
                            Log.d("CardAdapter", "Failed to add card", e)
                        }
                    }
                }
            )

            // Dismiss the dialog
            dialog.dismiss()
        }

        // Set click listener for the cancel button to dismiss the dialog
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }

    private fun showDeleteDialogForm(context: Context, card: Card) {
        // Create a dialog window with a custom layout for the card info form
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_card_info_delete)

        val window = dialog.window
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Find the form fields and buttons in the dialog layout
        val cardNumberText : TextView? = dialog.findViewById<TextView>(R.id.card_number)
        cardNumberText?.text = card.getCardNumber()

        val confirmButton = dialog.findViewById<Button?>(R.id.confirm_button)
        val cancelButton = dialog.findViewById<Button?>(R.id.cancel_button)

        // Save card listener
        confirmButton?.setOnClickListener {
            // Delete the card from the database
            db.deleteCard(
                card,
                onSuccess = {
                    Toast.makeText(context, "Card deleted successfully", Toast.LENGTH_SHORT).show()
                    Log.d("CardAdapter", "Card deleted successfully")
                    refreshFragmentView()
                },
                onFailure = { e ->
                    Toast.makeText(context, "Failed to delete card", Toast.LENGTH_SHORT).show()
                    Log.d("CardAdapter", "Failed to delete card", e)
                }
            )

            // Dismiss the dialog
            dialog.dismiss()
        }

        // Set click listener for the cancel button to dismiss the dialog
        cancelButton?.setOnClickListener {
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }

    private fun showPrimaryDialogForm(context: Context, card: Card) {
        // Create a dialog window with a custom layout for the card info form
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_card_info_primary)

        val window = dialog.window
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Find the form fields and buttons in the dialog layout
        val cardNumberText : TextView? = dialog.findViewById<TextView>(R.id.card_number)
        cardNumberText?.text = card.getCardNumber()

        val confirmButton = dialog.findViewById<Button?>(R.id.confirm_button)
        val cancelButton = dialog.findViewById<Button?>(R.id.cancel_button)

        // Save card listener
        confirmButton?.setOnClickListener {
            // Delete the card from the database
            db.updateCard(
                card,
                onSuccess = {
                    Toast.makeText(context, "Card set as primary successfully", Toast.LENGTH_SHORT).show()
                    Log.d("CardAdapter", "Card set as primary successfully")
                    refreshFragmentView()
                },
                onFailure = { e ->
                    Toast.makeText(context, "Failed to set card as primary", Toast.LENGTH_SHORT).show()
                    Log.d("CardAdapter", "Failed to set card as primary", e)
                }
            )

            // Dismiss the dialog
            dialog.dismiss()
        }

        // Set click listener for the cancel button to dismiss the dialog
        cancelButton?.setOnClickListener {
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }

}
