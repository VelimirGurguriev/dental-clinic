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

    fun getUserById(
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



    fun getAllDentists(
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        database.collection("users")
            .whereEqualTo("accountType", "dentist")
            .get()
            .addOnSuccessListener { result ->

                val dentists = result.toObjects(User::class.java)

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
        database.collection("users")
            .document(uid)
            .update("name", name)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}