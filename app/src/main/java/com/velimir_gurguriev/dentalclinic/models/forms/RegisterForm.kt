package com.velimir_gurguriev.dentalclinic.models.forms

data class RegisterForm(
    val name: String,
    val email: String,
    val password: String,
    val accountType: String
)
