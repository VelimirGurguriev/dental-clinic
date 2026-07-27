package com.velimir_gurguriev.dentalclinic.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.velimir_gurguriev.dentalclinic.models.User

class UserRepository {

    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun saveUserProfile(
        user: User,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        database.collection("users")
            .document(user.uid)
            .set(user)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getCurrentUser(
        uid: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        database.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener {

                val user = it.toObject(User::class.java)

                if (user != null)
                    onSuccess(user)

            }
            .addOnFailureListener(onFailure)

    }
}