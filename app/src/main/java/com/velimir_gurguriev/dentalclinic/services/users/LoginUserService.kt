package com.velimir_gurguriev.dentalclinic.services.users

import com.velimir_gurguriev.dentalclinic.models.forms.LoginForm
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository

class LoginUserService(
    private val authRepository: AuthRepository
) {

    fun login(
        form: LoginForm,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        authRepository.loginUser(
            email = form.email,
            password = form.password,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
}
