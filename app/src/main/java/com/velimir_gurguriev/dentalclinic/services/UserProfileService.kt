package com.velimir_gurguriev.dentalclinic.services

import com.velimir_gurguriev.dentalclinic.models.User
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository

class UserProfileService(
    private val repository: UserRepository,
    private val authRepository: AuthRepository
) {

    fun loadCurrentUser(
        uid: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.getUserById(
            uid = uid,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun logout() {
        authRepository.logout()
    }
}