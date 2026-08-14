package com.velimir_gurguriev.dentalclinic.models.forms

data class PatientRegisterForm(
    val name: String,
    val email: String,
    val password: String,
    val phone: String,
    val city: String,
    val dateOfBirth: Long
)