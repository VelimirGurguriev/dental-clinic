package com.velimir_gurguriev.dentalclinic.models.appointments

data class TimeSlotItem(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,

    var isSelected: Boolean = false,
    var isPublished: Boolean = false,

    var appointmentId: String? = null,
    var appointmentStatus: AppointmentStatus? = null,
    var isSelectedForCancellation: Boolean = false
)