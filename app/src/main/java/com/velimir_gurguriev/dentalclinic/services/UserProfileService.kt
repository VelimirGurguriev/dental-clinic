package com.velimir_gurguriev.dentalclinic.services

import com.velimir_gurguriev.dentalclinic.models.User
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository

class UserProfileService(
    private val repository: UserRepository
) {

    fun loadCurrentUser(
        uid: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.getCurrentUser(
            uid = uid,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
}