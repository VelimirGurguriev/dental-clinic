package com.velimir_gurguriev.dentalclinic.models.appointments

data class AppointmentSlot(
    val id: String = "",
    val dentistId: String = "",
    val patientId: String? = null,
    val startDateTime: Long = 0L,
    val endDateTime: Long = 0L,
    val status: String = AppointmentStatus.AVAILABLE.name,
    val createdAt: Long = System.currentTimeMillis(),
    val bookedAt: Long? = null
)