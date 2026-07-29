package com.velimir_gurguriev.dentalclinic.models.connections

import com.google.firebase.Timestamp

data class DentistPatientConnection(
    val id: String = "",
    val dentistId: String = "",
    val patientId: String = "",
    val status: String = DentistPatientStatus.PENDING.name,
    val requestedAt: Timestamp? = null,
    val respondedAt: Timestamp? = null
)