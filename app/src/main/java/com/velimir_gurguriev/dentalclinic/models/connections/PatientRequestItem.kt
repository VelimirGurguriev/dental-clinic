package com.velimir_gurguriev.dentalclinic.models.connections

import com.velimir_gurguriev.dentalclinic.models.User

data class PatientRequestItem(
    val connectionId: String,
    val patient: User
)