package com.velimir_gurguriev.dentalclinic.models.forms

data class DentistRegisterForm(
    val name: String,
    val email: String,
    val password: String,
    val phone: String,
    val city: String,
    val specialization: String,
    val clinicName: String,
    val clinicAddress: String,
)