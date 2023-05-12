
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.psm.R
import com.example.psm.helpers.FirebaseHelper
import com.example.psm.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountInfoAdapter(
    private val accountInfoList: List<String>,
    private val context: Context
) :
    RecyclerView.Adapter<AccountInfoAdapter.ViewHolder>() {

    interface OnDataChangedListener {
        fun onDataChanged()
    }

    private lateinit var db: FirebaseHelper
    private lateinit var user: User

    private var onDataChangedListener: OnDataChangedListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account_info, parent, false)
        db = FirebaseHelper()
        CoroutineScope(Dispatchers.Main).launch {
            user = db.getCurrentUser()!!
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.accountInfoText.text = accountInfoList[position]
        holder.accountInfoEdit.setOnClickListener {
            val editContainer: LinearLayout
            when (position) {
                // Update Name
                0 -> {
                    editContainer = holder.itemView.findViewById(R.id.edit_name_container)
                    editContainer.findViewById<EditText>(R.id.account_info_edit_first_name).hint = "First Name:"
                    editContainer.findViewById<EditText>(R.id.account_info_edit_first_name).setText(user.getFirstName())
                    editContainer.findViewById<EditText>(R.id.account_info_edit_last_name).hint = "Last Name:"
                    editContainer.findViewById<EditText>(R.id.account_info_edit_last_name).setText(user.getLastName())

                    editContainer.findViewById<Button>(R.id.save_name_button).setOnClickListener() {
                        val firstName = editContainer.findViewById<EditText>(R.id.account_info_edit_first_name).text.toString()
                        val lastName = editContainer.findViewById<EditText>(R.id.account_info_edit_last_name).text.toString()
                        user.setFirstName(firstName)
                        user.setLastName(lastName)

                        db.updateUser(
                            user,
                            onSuccess = {
                                editContainer.visibility = View.GONE
                                holder.accountInfoText.visibility = View.VISIBLE
                                Toast.makeText(context, "Successfully updated user", Toast.LENGTH_SHORT).show()
                                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(editContainer.windowToken, 0)

                                onDataChangedListener?.onDataChanged()
                            },
                            onFailure = {
                                println("Failed to update user")
                                Log.e("AccountInfoAdapter", "Failed to update user")
                                Toast.makeText(context, "Failed to update user", Toast.LENGTH_SHORT).show()
                            }
                        )

                    }
                }
                //  Update Address
                3 -> {
                    editContainer = holder.itemView.findViewById(R.id.account_info_edit_container_address)

                    // Get a reference to the spinner view in your layout
                    val spinner: Spinner = editContainer.findViewById(R.id.country_spinner)

// Create an ArrayAdapter using the string array and a default spinner layout
                    val adapter = ArrayAdapter.createFromResource(
                        context,
                        R.array.country_names,
                        android.R.layout.simple_spinner_item
                    )

// Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

// Apply the adapter to the spinner
                    spinner.adapter = adapter

                    val addressArr = user.getSplitAddress()
                    editContainer.findViewById<EditText>(R.id.street_edit_text).hint = "Street:"
                    editContainer.findViewById<EditText>(R.id.street_edit_text).setText(addressArr["street"])

                    editContainer.findViewById<EditText>(R.id.city_edit_text).hint = "City:"
                    editContainer.findViewById<EditText>(R.id.city_edit_text).setText(addressArr["city"])

                    editContainer.findViewById<EditText>(R.id.state_edit_text).hint = "State:"
                    editContainer.findViewById<EditText>(R.id.state_edit_text).setText(addressArr["state"])

                    editContainer.findViewById<EditText>(R.id.zip_edit_text).hint = "Zip:"
                    editContainer.findViewById<EditText>(R.id.zip_edit_text).setText(addressArr["zip"])

                    // get Stringer value of country
                    val country = addressArr["country"] ?: "United States"
                    // get index of country in array
                    val index = adapter.getPosition(country)
                    // set spinner to country
                    spinner.setSelection(index)

                    editContainer.findViewById<Button>(R.id.save_address_button).setOnClickListener() {
                        val street = editContainer.findViewById<EditText>(R.id.street_edit_text).text.toString().trim()
                        val city = editContainer.findViewById<EditText>(R.id.city_edit_text).text.toString().trim()
                        val state = editContainer.findViewById<EditText>(R.id.state_edit_text).text.toString().trim()
                        val zip = editContainer.findViewById<EditText>(R.id.zip_edit_text).text.toString().trim()
                        var country = spinner.selectedItem.toString().trim()

                        if (country == "Please Select") {
                            country = ""
                        }

                        user.setAddress(street, city, state, country, zip)

                        db.updateUser(
                            user,
                            onSuccess = {
                                editContainer.visibility = View.GONE
                                holder.accountInfoText.visibility = View.VISIBLE
                                Toast.makeText(context, "Successfully updated user", Toast.LENGTH_SHORT).show()
                                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(editContainer.windowToken, 0)

                                onDataChangedListener?.onDataChanged()
                            },
                            onFailure = {
                                println("Failed to update user")
                                Log.e("AccountInfoAdapter", "Failed to update user")
                                Toast.makeText(context, "Failed to update user", Toast.LENGTH_SHORT).show()
                            }
                        )

                    }
                }
                // Update Email or Phone
                else -> {
                    editContainer = holder.itemView.findViewById(R.id.account_info_edit_container)
                    editContainer.findViewById<EditText>(R.id.account_info_edit_field).hint = accountInfoList[position]

                    if (accountInfoList[position].contains("Email")) {
                        editContainer.findViewById<EditText>(R.id.account_info_edit_field).setText(user.getEmail())
                    } else if (accountInfoList[position].contains("Phone")) {
                        editContainer.findViewById<EditText>(R.id.account_info_edit_field).setText(user.getPhone())
                    }

                    editContainer.findViewById<Button>(R.id.save_button).setOnClickListener() {
                        val field = editContainer.findViewById<EditText>(R.id.account_info_edit_field).text.toString()
                        if (accountInfoList[position].contains("Email")) {
                            user.setEmail(field)
                        } else if (accountInfoList[position].contains("Phone")) {
                            user.setPhone(field)
                        }
                        db.updateAuthProfile(
                            user,
                            onSuccess = {
                                editContainer.visibility = View.GONE
                                holder.accountInfoText.visibility = View.VISIBLE
                                Toast.makeText(context, "Successfully updated user", Toast.LENGTH_SHORT).show()
                                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(editContainer.windowToken, 0)

                                onDataChangedListener?.onDataChanged()
                            },
                            onFailure = {
                                println("Failed to update user")
                                Log.e("AccountInfoAdapter", "Failed to update user")
                                Toast.makeText(context, "Failed to update user. Try logging out/in again.", Toast.LENGTH_SHORT).show()
                            }
                        )

                    }
                }
            }

            // Toggle the visibility of the edit fields
            editContainer.visibility =
                if (editContainer.visibility == View.GONE) View.VISIBLE else View.GONE

            // Open the clicked edit fields
            openEditFields(editContainer)
        }
    }

    fun setOnDataChangedListener(listener: OnDataChangedListener) {
        onDataChangedListener = listener
    }

    private fun openEditFields(editContainer: LinearLayout) {
        // Apply an animation to slide the edit fields down
        val slideDown = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, -1f,
            Animation.RELATIVE_TO_SELF, 0f
        )
        slideDown.duration = 300
        editContainer.startAnimation(slideDown)
    }


    override fun getItemCount(): Int = accountInfoList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val accountInfoText: TextView = itemView.findViewById(R.id.account_info_text)
        val accountInfoEdit: ImageView = itemView.findViewById(R.id.account_info_edit)
    }
}