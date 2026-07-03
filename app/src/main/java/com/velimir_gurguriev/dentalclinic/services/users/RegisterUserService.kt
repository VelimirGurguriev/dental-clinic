package com.velimir_gurguriev.dentalclinic.services.users

import com.velimir_gurguriev.dentalclinic.models.User
import com.velimir_gurguriev.dentalclinic.models.forms.RegisterForm
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository

class RegisterUserService(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {

    fun register(
        form: RegisterForm,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        authRepository.registerUser(
            email = form.email,
            password = form.password,
            onSuccess = { uid ->
                val user = User(
                    uid = uid,
                    name = form.name,
                    email = form.email,
                    accountType = form.accountType
                )

                userRepository.saveUserProfile(
                    user = user,
                    onSuccess = onSuccess,
                    onFailure = onFailure
                )
            },
            onFailure = onFailure
        )
    }
}
