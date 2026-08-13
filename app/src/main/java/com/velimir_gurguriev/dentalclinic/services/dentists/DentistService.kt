package com.velimir_gurguriev.dentalclinic.services.dentists

import com.velimir_gurguriev.dentalclinic.models.User
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository

class DentistService(
    private val userRepository: UserRepository
) {

    fun getAllDentists(
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userRepository.getAllDentists(
            onSuccess,
            onFailure
        )
    }

    fun getDentistById(
        uid: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userRepository.getUserById(
            uid = uid,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
}