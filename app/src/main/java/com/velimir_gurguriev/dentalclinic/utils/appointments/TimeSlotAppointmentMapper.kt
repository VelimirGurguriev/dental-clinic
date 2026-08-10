package com.velimir_gurguriev.dentalclinic.utils.appointments

import com.velimir_gurguriev.dentalclinic.models.appointments.AppointmentSlot
import com.velimir_gurguriev.dentalclinic.models.appointments.AppointmentStatus
import com.velimir_gurguriev.dentalclinic.models.appointments.TimeSlotItem
import java.util.Calendar

object TimeSlotAppointmentMapper {

    fun applyAppointments(
        timeSlots: List<TimeSlotItem>,
        appointmentSlots: List<AppointmentSlot>
    ) {
        timeSlots.forEach { timeSlot ->

            val matchingAppointment =
                findMatchingAppointment(
                    timeSlot = timeSlot,
                    appointmentSlots = appointmentSlots
                )

            timeSlot.appointmentId =
                matchingAppointment?.id

            timeSlot.appointmentStatus =
                getAppointmentStatus(
                    matchingAppointment
                )

            timeSlot.isSelected = false
            timeSlot.isSelectedForCancellation = false
        }
    }

    private fun findMatchingAppointment(
        timeSlot: TimeSlotItem,
        appointmentSlots: List<AppointmentSlot>
    ): AppointmentSlot? {

        return appointmentSlots.find { appointmentSlot ->

            if (
                appointmentSlot.status ==
                AppointmentStatus.CANCELLED.name
            ) {
                return@find false
            }

            val calendar = Calendar.getInstance().apply {
                timeInMillis =
                    appointmentSlot.startDateTime
            }

            calendar.get(Calendar.HOUR_OF_DAY) ==
                    timeSlot.startHour &&
                    calendar.get(Calendar.MINUTE) ==
                    timeSlot.startMinute
        }
    }

    private fun getAppointmentStatus(
        appointmentSlot: AppointmentSlot?
    ): AppointmentStatus? {

        val status =
            appointmentSlot?.status
                ?: return null

        return runCatching {
            AppointmentStatus.valueOf(status)
        }.getOrNull()
    }
}