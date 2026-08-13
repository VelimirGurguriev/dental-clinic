package com.velimir_gurguriev.dentalclinic.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.velimir_gurguriev.dentalclinic.models.User

class UserRepository {

    private val database = FirebaseFirestore.getInstance()

    private val usersCollection =
        database.collection(
            USERS_COLLECTION
        )

    fun saveUserProfile(
        user: User,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        usersCollection
            .document(user.uid)
            .set(user)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getUserById(
        uid: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        usersCollection
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                val user =
                    document.toObject(
                        User::class.java
                    )

                if (user != null) {
                    onSuccess(user)
                } else {
                    onFailure(
                        IllegalStateException(
                            "Потребителят не е намерен."
                        )
                    )
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getAllDentists(
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        usersCollection
            .whereEqualTo(
                ACCOUNT_TYPE_FIELD,
                DENTIST_ACCOUNT_TYPE
            )
            .get()
            .addOnSuccessListener { result ->

                val dentists =
                    result.toObjects(
                        User::class.java
                    )

                onSuccess(dentists)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun updateUserName(
        uid: String,
        name: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        usersCollection
            .document(uid)
            .update(
                NAME_FIELD,
                name
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    companion object {
        private const val USERS_COLLECTION = "users"

        private const val ACCOUNT_TYPE_FIELD = "accountType"

        private const val NAME_FIELD = "name"

        private const val DENTIST_ACCOUNT_TYPE = "dentist"
    }
}