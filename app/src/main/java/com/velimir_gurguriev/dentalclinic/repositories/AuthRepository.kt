package com.velimir_gurguriev.dentalclinic.repositories

import com.google.firebase.auth.FirebaseAuth

class AuthRepository {

    private val auth: FirebaseAuth =
        FirebaseAuth.getInstance()

    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(
            email,
            password
        )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun registerUser(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(
            email,
            password
        )
            .addOnSuccessListener { result ->
                val uid =
                    result.user?.uid

                if (uid != null) {
                    onSuccess(uid)
                } else {
                    onFailure(
                        Exception(
                            "Неуспешно създаване на потребител."
                        )
                    )
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun logout() {
        auth.signOut()
    }
}