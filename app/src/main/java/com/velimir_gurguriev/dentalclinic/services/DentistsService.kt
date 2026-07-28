package com.velimir_gurguriev.dentalclinic.services

import com.velimir_gurguriev.dentalclinic.models.User
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository

class DentistsService(
    private val userRepository: UserRepository
) {

    fun loadDentists(
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userRepository.getAllDentists(
            onSuccess,
            onFailure
        )
    }
}