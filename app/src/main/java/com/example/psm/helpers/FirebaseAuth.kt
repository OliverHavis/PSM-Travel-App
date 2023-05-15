package com.example.psm.helpers

import Booking
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import com.example.psm.models.Card
import com.example.psm.models.Destination
import com.example.psm.models.Saved
import com.example.psm.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.io.*
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class FirebaseHelper {

    // initiaization
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage : FirebaseStorage = FirebaseStorage.getInstance()

    /**
     * Get the current user.
     *
     * @return FirebaseUser
     */
    suspend fun getCurrentUser(): User? {
        Log.d(TAG, "${auth.currentUser}")
        // Check if the user is signed in (non-null)
        val firebaseUser = auth.currentUser ?: return null

        Log.e(TAG, "${auth.currentUser!!.uid}")
        // Get the user data from Firestore
        return try {
            val document = firestore.collection("Users").document(firebaseUser.uid).get().await()
            if (document.exists()) {
                User(
                    document.getString("uid") ?: "",
                    document.getString("first_name") ?: "",
                    document.getString("last_name") ?: "",
                    EncryptionUtils.decrypt(
                        document.getString("email") ?: "") ?: "",
                    EncryptionUtils.decrypt(
                        document.getString("phone") ?: "") ?: "",
                    EncryptionUtils.decrypt(
                        document.getString("address") ?: "") ?: "",
                    document.getBoolean("profile_picture") ?: false
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user data", e)
            null
        }
    }


    /**
     * Create a new user with the provided email and password and add the user to the database.
     *
     * @param email The email of the user.
     * @param password The password of the user.
     * @param name The name of the user.
     * @param phone The phone number of the user.
     * @param onSuccess A function to be called when the user is successfully created.
     * @param onFailure A function to be called when the user creation fails.
     * @return Unit
     * @see [FirebaseAuth.createUserWithEmailAndPassword]
     * @see [FirebaseFirestore.set]
     * @see [FirebaseFirestore.collection]
     * @see [FirebaseFirestore.document]
     */
    fun signUp(
        email: String,
        password: String,
        first_name: String,
        last_name: String,
        phone: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "createUserWithEmail:success")
                    val encryptedEmail = EncryptionUtils.encrypt(email)
                    val encryptedPhone = EncryptionUtils.encrypt(phone)
                    val user = mapOf(
                        "uid" to auth.currentUser!!.uid,
                        "first_name" to first_name,
                        "last_name" to last_name,
                        "email" to encryptedEmail,
                        "phone" to encryptedPhone
                    ).toMutableMap()

                    firestore.collection("Users").document(auth.currentUser!!.uid)
                        .set(user)
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!")
                            println("DocumentSnapshot successfully written!")
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            Log.w(ContentValues.TAG, "Error writing document", e)
                            println("Error writing document: $e")
                            onFailure(e)
                        }
                } else {
                    Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                    println("Error: ${task.exception}")
                    task.exception?.let { onFailure(it) }
                }
            }
    }

    /**
     * Sign in the user with the provided email and password.
     *
     * @param email The email of the user.
     * @param password The password of the user.
     * @param onSuccess A function to be called when the user is successfully signed in.
     * @param onFailure A function to be called when the user sign in fails.
     * @return Unit
     * @see [FirebaseAuth.signInWithEmailAndPassword]
     */
    fun signIn(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "signInWithEmail:success")
                    onSuccess()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    onFailure()
                }
            }
    }

    /**
     * Update the user data in the database.
     *
     * @param user The user object.
     * @param onSuccess A function to be called when the user data is successfully updated.
     * @param onFailure A function to be called when the user data update fails.
     */
    fun updateUser(
        user: User,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit)
    {
        val userMap = mutableMapOf<String, Any>(
            "uid" to auth.currentUser!!.uid,
            "first_name" to user.getFirstName(),
            "last_name" to user.getLastName(),
            "email" to EncryptionUtils.encrypt(user.getEmail()),
            "phone" to EncryptionUtils.encrypt(user.getPhone()),
            "profile_picture" to user.getProfilePicture()
        )

        user.getAddress()?.let {
            EncryptionUtils.encrypt(it)?.let { address ->
                userMap["address"] = address
            }
        }

        firestore.collection("Users").document(auth.currentUser!!.uid)
            .set(userMap)
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!")
                println("DocumentSnapshot successfully written!")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error writing document", e)
                println("Error writing document: $e")
                onFailure(e)
            }
    }

    /**
     * Updates the user's Auth profile.
     *
     * @param user The user object.
     * @param onSuccess A function to be called when the user data is successfully updated.
     * @param onFailure A function to be called when the user data update fails.
     */
    fun updateAuthProfile(
        user: User,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // re-authenticate user
//        reAuthUser(user)

        // update users auth profile
        auth.currentUser!!.updateEmail(user.getEmail())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User email address updated.")
                    updateUser(user, onSuccess, onFailure)
                } else {
                    Log.w(TAG, "Error updating user email address.", task.exception)
                    onFailure(task.exception!!)
                }
            }
    }

    /**
     * Reauthenticate the user.
     *
     * @param user The user object.
     * @see [FirebaseUser.reauthenticate]
     */
//    private fun reAuthUser(user: User) {
//        println("${user.email}, ${user.password}")
//        val credential = EmailAuthProvider.getCredential(user?.email ?: "", user?.password ?: "")
//
//        // re authenticate user
//        auth.currentUser!!.reauthenticate(credential)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    Log.d(TAG, "User re-authenticated.")
//                } else {
//                    Log.w(TAG, "Error re-authenticating user.", task.exception)
//                }
//            }
//    }

    /**
     * Sign out the user.
     *
     * @see [FirebaseAuth.signOut]
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Uploads the user's profile picture to Firebase Storage.
     *
     * @param imageUri The image URI.
     * @param onSuccess A function to be called when the image is successfully uploaded.
     * @param onFailure A function to be called when the image upload fails.
     */
    fun uploadProfilePicture(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val storageRef = storage.reference.child("profile_pictures/${auth.currentUser!!.uid}.jpg")

        val uploadTask = storageRef.putFile(imageUri)
        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                onSuccess(uri.toString())
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    // Cards
    fun addCard(
        card: Card,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        getAllCards(
            onSuccess = { cards ->
                val existingCard = cards.find { it.getCardNumberFull() == card.getCardNumberFull() }
                if (existingCard != null) {
                    onFailure(Exception("Card number already exists"))
                    return@getAllCards
                }

                if (cards.size >= 3) {
                    onFailure(Exception("Maximum number of cards reached"))
                    return@getAllCards
                }

                if (card.isPrimary()) {
                    for (c in cards) {
                        if (c.isPrimary()) {
                            // Uncheck the existing primary card
                            c.setPrimary(false)
                            updateCardInFirestore(c)
                            break
                        }
                    }
                }
                saveNewCard(card, onSuccess, onFailure)
            },
            onFailure = { exception ->
                onFailure(exception)
            }
        )
    }

    private fun saveNewCard(
        card: Card,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val cardMap = mutableMapOf<String, Any>(
            "uid" to auth.currentUser!!.uid,
            "card_number" to EncryptionUtils.encrypt(card.getCardNumberFull()),
            "card_holder" to EncryptionUtils.encrypt(card.getCardHolder()),
            "expiry_date" to EncryptionUtils.encrypt(card.getExpiryDate()),
            "cvv" to EncryptionUtils.encrypt(card.getCvv()),
            "primary" to card.isPrimary(),
        )

        firestore.collection("Cards").document()
            .set(cardMap)
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!")
                println("DocumentSnapshot successfully written!")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error writing document", e)
                println("Error writing document: $e")
                onFailure(e)
            }
    }

    fun updateCard(
        card: Card,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (card.isPrimary()) {
            // If the card is already the primary card, no further action is needed
            onSuccess()
            return
        }

        getAllCards(
            onSuccess = { cards ->
                var previousPrimaryCard: Card? = null

                for (c in cards) {
                    if (c.isPrimary()) {
                        previousPrimaryCard = c
                        break
                    }
                }

                if (previousPrimaryCard != null) {
                    previousPrimaryCard.setPrimary(false)
                    updateCardInFirestore(previousPrimaryCard)
                }

                card.setPrimary(true)
                updateCardInFirestore(card)

                onSuccess()
            },
            onFailure = { exception ->
                onFailure(exception)
            }
        )
    }

    private fun updateCardInFirestore(card: Card) {
        val cardMap = mutableMapOf<String, Any>(
            "uid" to auth.currentUser!!.uid,
            "card_number" to EncryptionUtils.encrypt(card.getCardNumberFull()),
            "card_holder" to EncryptionUtils.encrypt(card.getCardHolder()),
            "expiry_date" to EncryptionUtils.encrypt(card.getExpiryDate()),
            "cvv" to EncryptionUtils.encrypt(card.getCvv()),
            "primary" to card.isPrimary(),
        )

        firestore.collection("Cards").document(card.getUid()!!)
            .set(cardMap)
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!")
                println("DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error writing document", e)
                println("Error writing document: $e")
            }
    }


    fun deleteCard (
        card: Card,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("Cards").document(card.getUid()!!)
            .delete()
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "DocumentSnapshot successfully deleted!")
                println("DocumentSnapshot successfully deleted!")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error deleting document", e)
                println("Error deleting document: $e")
                onFailure(e)
            }
    }

    fun getAllCards(
        onSuccess: (List<Card>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("Cards")
            .whereEqualTo("uid", auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { result ->
                println(result)
                val cards = mutableListOf<Card>()
                for (document in result) {
                    val card = Card(
                        document.id,
                        document.data["uid"] as String,
                        EncryptionUtils.decrypt(document.data["card_number"] as String),
                        EncryptionUtils.decrypt(document.data["card_holder"] as String),
                        EncryptionUtils.decrypt(document.data["expiry_date"] as String),
                        EncryptionUtils.decrypt(document.data["cvv"] as String),
                        document.data["primary"] as Boolean
                    )
                    cards.add(card)
                }
                onSuccess(cards)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
                onFailure(exception)
            }
    }

    // Destinations
    fun getAvailableLocations(onSuccess: (List<String>) -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("Destinations")

        collectionRef.get()
            .addOnSuccessListener { result ->
                val locations = mutableListOf<String>("Any")
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    var location = document.data["location"] as List<String>
                    if (!locations.contains(location.last())) {
                        locations.add(location.last())
                    }
                }

                locations.sort()
                onSuccess(locations)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
                onFailure(exception)
            }
    }

    fun getRandomLocations(limit: Int = 3, onSuccess: (List<Destination>) -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("Destinations")

        collectionRef.get().addOnSuccessListener { result ->
            val destinations = mutableListOf<Destination>()
            val totalDestinations = result.size()

            // Generate random indices within the range of available destinations
            val randomIndices = (0 until totalDestinations).shuffled().take(limit)

            // Retrieve destinations based on the random indices
            for (index in randomIndices) {
                val document = result.documents[index]
                val destination = Destination(
                    document.id,
                    document.data!!.get("name") as String,
                    document.data!!["location"] as List<String>,
                    document.data!!["pricePerAdult"] as Double,
                    document.data!!["pricePerChild"] as Double,
                    document.data!!["boardType"] as String,
                    document.data!!["discount"] as Double,
                    document.data!!["peakSeason"] as String,
                    document.data!!["rating"] as Double
                )
                destinations.add(destination)
            }

            onSuccess(destinations)
        }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
                onFailure(exception)
            }
    }

    fun halveCostFromDB(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("Destinations")

        collectionRef.get()
            .addOnSuccessListener { result ->
                val batch = db.batch()

                for (document in result) {
                    val destinationRef = collectionRef.document(document.id)

                    val newPricePerChild = (document.data["pricePerChild"] as Double) * 2
                    batch.update(destinationRef, "pricePerChild", newPricePerChild)
                }

                batch.commit()
                    .addOnSuccessListener {
                        // Commit successful
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        // Commit failed
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                // Query failed
                onFailure(exception)
            }
    }



    fun queryDestinations(
        country: String,
        onSuccess: (List<Destination>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        if (country == "Any") {
            getRandomLocations(limit = 5,
                onSuccess = { destinations ->
                onSuccess(destinations)
            }, onFailure = { exception ->
                onFailure(exception)
            })
            return
        }

        val collectionRef = db.collection("Destinations")
        collectionRef.whereArrayContains("location", country)
            .get()
            .addOnSuccessListener { result ->
                val destinations = mutableListOf<Destination>()
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    val destination = Destination(
                        document.id,
                        document.data["name"] as String,
                        document.data["location"] as List<String>,
                        document.data["pricePerAdult"] as Double,
                        document.data["pricePerChild"] as Double,
                        document.data["boardType"] as String,
                        document.data["discount"] as Double,
                        document.data["peakSeason"] as String,
                        document.data["rating"] as Double
                    )
                    destinations.add(destination)
                }

                onSuccess(destinations)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
                onFailure(exception)
            }
    }

    fun getSaves(
        onSuccess: (List<Saved>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("Saved")

        collectionRef.whereEqualTo("user_id", FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnSuccessListener { result ->
                val saves = mutableListOf<Saved>()
                val totalDestinations = result.size()
                var destinationsRetrieved = 0

                for (document in result) {
                    db.collection("Destinations").document(document.data["destination_id"] as String)
                        .get()
                        .addOnSuccessListener { destination ->
                            val save = Saved(
                                document.id,
                                Destination(
                                    destination.id,
                                    destination.data!!["name"] as String,
                                    destination.data!!["location"] as List<String>,
                                    destination.data!!["pricePerAdult"] as Double,
                                    destination.data!!["pricePerChild"] as Double,
                                    destination.data!!["boardType"] as String,
                                    destination.data!!["discount"] as Double,
                                    destination.data!!["peakSeason"] as String,
                                    destination.data!!["rating"] as Double
                                ),
                                FirebaseAuth.getInstance().currentUser!!.uid,
                                document.data["query"] as Map<String, Any>,
                            )
                            saves.add(save)

                            destinationsRetrieved++
                            if (destinationsRetrieved == totalDestinations) {
                                onSuccess(saves)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d(TAG, "Error getting documents: ", exception)
                            onFailure(exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
                onFailure(exception)
            }
    }


    fun addToFavorites(
        destination: Destination,
        queryFrom: String,
        queryTo: String,
        queryDate: String,
        queryNights: Int,
        queryAdults: Int,
        queryChildren: Int,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("Saved")

        val query = mutableMapOf<String, Any>(
            "from" to queryFrom,
            "to" to queryTo,
            "date" to queryDate,
            "nights" to queryNights,
            "adults" to queryAdults,
            "children" to queryChildren
        )

        val savedMap = mutableMapOf(
            "destination_id" to destination.getId(),
            "user_id" to FirebaseAuth.getInstance().currentUser!!.uid,
            "query" to query,
        )

        collectionRef.add(savedMap)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                onFailure()
            }
    }

    fun removeFromFavorites(
        destination: Destination,
        queryFrom: String,
        queryTo: String,
        queryDate: String,
        queryNights: Int,
        queryAdults: Int,
        queryChildren: Int,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("Saved")

        val query = mutableMapOf<String, Any>(
            "from" to queryFrom,
            "to" to queryTo,
            "date" to queryDate,
            "nights" to queryNights,
            "adults" to queryAdults,
            "children" to queryChildren
        )

        Log.d(TAG, "Destination: ${destination.getId()}, User: ${FirebaseAuth.getInstance().currentUser!!.uid}, Query: ${query}")

        // Query to find the document to remove
        val queryToRemove = collectionRef
            .whereEqualTo("destination_id", destination.getId())
            .whereEqualTo("user_id", FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo("query", query)

        queryToRemove.get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "DocumentSnapshot successfully retrieved: ${result}")
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
                // Check if the document exists
                if (!result.isEmpty) {
                    val document = result.documents[0]
                    // Remove the document
                    document.reference.delete()
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully removed")
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error removing document", e)
                            onFailure()
                        }
                } else {
                    // Document not found
                    onFailure()
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
                onFailure()
            }
    }


    fun getPicture(directory: String, id :String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val storage = Firebase.storage
        val storageRef = storage.reference

        val imageRef: StorageReference = storageRef.child("$directory/$id.jpeg")

        imageRef.downloadUrl
            .addOnSuccessListener { uri ->
                onSuccess(uri.toString())
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getExcursions(destination_id: String, onSuccess: (List<Map<String, Any>>) -> Unit, onFailure: (Exception) -> Unit) {
        Log.d(TAG, "Destination ID: $destination_id")
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("Excursions") // Corrected collection name to "excursions"

        collectionRef.whereEqualTo("destination_id", destination_id)
            .get()
            .addOnSuccessListener { result ->
                val excursions = mutableListOf<Map<String, Any>>()
                Log.d(TAG, "DocumentSnapshot successfully retrieved: $result")
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    val excursion = mutableMapOf<String, Any>(
                        "id" to document.id,
                        "name" to document.data["name"] as String,
                        "price" to document.data["price"] as Double
                    )
                    excursions.add(excursion)
                }
                onSuccess(excursions)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
                onFailure(exception)
            }
    }

    suspend fun getExcursion(id: String): Map<String, Any> {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("Excursions")

        return try {
            val result = collectionRef.document(id).get().await()
            val excursion = mutableMapOf<String, Any>(
                "id" to result.id,
                "name" to result.data!!["name"] as String,
                "price" to result.data!!["price"] as Double
            )
            excursion
        } catch (exception: Exception) {
            Log.d(TAG, "Error getting documents: ", exception)
            throw exception
        }
    }

    fun addBooking(booking: Booking, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("Bookings")

        val bookingMap = mutableMapOf<String, Any>(
            "user_id" to FirebaseAuth.getInstance().currentUser!!.uid,
            "destination_id" to booking.destination!!.getId(),
            "excursion_ids" to booking.selectedExtras,
            "from" to booking.queryFrom,
            "date" to booking.queryDate,
            "adults" to booking.queryAdults,
            "children" to booking.queryChildren,
            "nights" to booking.queryNights,
            "total" to booking.totalPrice
        )

        collectionRef.add(bookingMap)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                onFailure()
            }
    }

    fun importExcursionsToFirestore() {

        val data = "A5tS0HLWX6Di4dcDUJ4M,Jungfraujoch Excursion,180.00\n" +
                "A5tS0HLWX6Di4dcDUJ4M,Trümmelbach Falls,25.00\n" +
                "A5tS0HLWX6Di4dcDUJ4M,Lake Thun Cruise,45.00\n" +
                "DbAxCOsBC82Q1wTT42NJ,Palmitos Park,45.00\n" +
                "DbAxCOsBC82Q1wTT42NJ,Jeep Safari,80.00\n" +
                "DbAxCOsBC82Q1wTT42NJ,Scuba Diving,60.00\n" +
                "HrPMgZnwMSIjO3BDrzkx,Loro Parque,45.00\n" +
                "HrPMgZnwMSIjO3BDrzkx,Teide National Park,30.00\n" +
                "HrPMgZnwMSIjO3BDrzkx,Whale Watching Tour,50.00\n" +
                "MlsG9RyIwpJeQqoCvsla,Los Gigantes Boat Trip,40.00\n" +
                "MlsG9RyIwpJeQqoCvsla,Teide National Park,30.00\n" +
                "MlsG9RyIwpJeQqoCvsla,Masca Valley Hike,60.00\n" +
                "MluTdWgSgUFpxlkkjTk0,Capri Island Tour,90.00\n" +
                "MluTdWgSgUFpxlkkjTk0,Pompeii and Herculaneum Excursion,75.00\n" +
                "MluTdWgSgUFpxlkkjTk0,Amalfi Coast Drive,50.00\n" +
                "Was836RhhFygtKSKlCif,Siam Park,50.00\n" +
                "Was836RhhFygtKSKlCif,Jeep Safari,80.00\n" +
                "Was836RhhFygtKSKlCif,Submarine Safari,55.00\n" +
                "euv4ft21bCrWb7dIyJpP,Capri Island Tour,90.00\n" +
                "euv4ft21bCrWb7dIyJpP,Pompeii and Herculaneum Excursion,75.00\n" +
                "euv4ft21bCrWb7dIyJpP,Amalfi Coast Drive,50.00\n" +
                "i81oOypy1NnbI22C5N4L,Capri Island Tour,90.00\n" +
                "i81oOypy1NnbI22C5N4L,Pompeii and Herculaneum Excursion,75.00\n" +
                "i81oOypy1NnbI22C5N4L,Amalfi Coast Drive,50.00\n" +
                "ir0dfyTwHtKI8nPxWqeC,Loro Parque,45.00\n" +
                "ir0dfyTwHtKI8nPxWqeC,Teide National Park,30.00\n" +
                "ir0dfyTwHtKI8nPxWqeC,Whale Watching Tour,50.00\n" +
                "oObAhtoW5JD5xCxLYl9A,La Maddalena Archipelago,70.00\n" +
                "oObAhtoW5JD5xCxLYl9A,Olbia City Tour,30.00\n" +
                "oObAhtoW5JD5xCxLYl9A,Tavolara Island,55.00\n" +
                "qARUCx54uknZuy661qmR,Jungfraujoch Excursion,180.00\n" +
                "qARUCx54uknZuy661qmR,Trümmelbach Falls,25.00\n" +
                "qARUCx54uknZuy661qmR,Lake Thun Cruise,45.00\n" +
                "xEv9G5EZfBTzMMgJn7NC,Snorkeling Adventure,60.00\n" +
                "xEv9G5EZfBTzMMgJn7NC,Quad Bike Safari,80.00\n" +
                "xEv9G5EZfBTzMMgJn7NC,Camel Ride,45.00\n" +
                "zrpqKXdm24hiuQQBT8Ic,Loro Parque,45.00\n" +
                "zrpqKXdm24hiuQQBT8Ic,Teide National Park,30.00\n" +
                "zrpqKXdm24hiuQQBT8Ic,Whale Watching Tour,50.00"

        val db = FirebaseFirestore.getInstance()
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid

        val excursions = data.split("\n")

        for (excursion in excursions) {
            val excursionData = excursion.split(",").map { it.trim() }
            val destinationID = excursionData[0]
            val name = excursionData[1]
            val price = excursionData[2].toDouble()

            val excursionMap = hashMapOf(
                "destination_id" to destinationID,
                "user_id" to currentUserID,
                "name" to name,
                "price" to price
            )

            db.collection("Excursions")
                .add(excursionMap)
                .addOnSuccessListener {
                    println("Excursion added successfully: $name")
                }
                .addOnFailureListener { e ->
                    println("Error adding excursion: ${e.message}")
                }
        }
    }


}
object EncryptionUtils {
    private const val AES_KEY = "xExgXQfGUCpi6ROESLi6Hw=="
    private const val AES_IV = "2nkmHddBw1VgQkBA"

    fun encrypt(text: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        val keySpec = SecretKeySpec(AES_KEY.toByteArray(StandardCharsets.UTF_8), "AES")
        val ivSpec = IvParameterSpec(AES_IV.toByteArray(StandardCharsets.UTF_8))
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encryptedBytes = cipher.doFinal(text.toByteArray(StandardCharsets.UTF_8))
        return java.util.Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decrypt(encryptedText: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        val keySpec = SecretKeySpec(AES_KEY.toByteArray(StandardCharsets.UTF_8), "AES")
        val ivSpec = IvParameterSpec(AES_IV.toByteArray(StandardCharsets.UTF_8))
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        val encryptedBytes = java.util.Base64.getDecoder().decode(encryptedText)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }
}

