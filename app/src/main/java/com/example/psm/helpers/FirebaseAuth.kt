package com.example.psm.helpers

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import com.example.psm.models.Card
import com.example.psm.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
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

