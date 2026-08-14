package com.velimir_gurguriev.dentalclinic.models.dentist

data class DentistProfile(
    val uid: String = "",
    val specialization: String = "",
    val clinicName: String = "",
    val clinicAddress: String = ""
)