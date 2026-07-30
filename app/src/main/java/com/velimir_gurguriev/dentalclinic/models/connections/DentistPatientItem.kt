package com.velimir_gurguriev.dentalclinic.models.connections

import com.velimir_gurguriev.dentalclinic.models.User

data class DentistPatientItem(
    val connectionId: String,
    val patient: User
)