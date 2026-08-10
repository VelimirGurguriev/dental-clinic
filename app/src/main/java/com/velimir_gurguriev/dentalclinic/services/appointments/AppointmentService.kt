package com.velimir_gurguriev.dentalclinic.services.appointments

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.velimir_gurguriev.dentalclinic.models.appointments.AppointmentSlot
import com.velimir_gurguriev.dentalclinic.models.appointments.TimeSlotItem
import com.velimir_gurguriev.dentalclinic.repositories.AppointmentRepository
import com.velimir_gurguriev.dentalclinic.utils.appointments.AppointmentDateUtils

class AppointmentService(
    private val appointmentRepository: AppointmentRepository
) {

    fun createAppointmentSlots(
        dentistId: String,
        selectedDate: Long,
        timeSlots: List<TimeSlotItem>
    ): Task<Void> {

        if (dentistId.isBlank()) {
            return createFailedTask(
                "Липсва идентификатор на стоматолога."
            )
        }

        if (selectedDate <= 0L) {
            return createFailedTask(
                "Невалидна избрана дата."
            )
        }

        if (timeSlots.isEmpty()) {
            return createFailedTask(
                "Няма избрани часове за добавяне."
            )
        }

        val tasks = timeSlots.map { timeSlot ->

            val startDateTime =
                AppointmentDateUtils.createDateTime(
                    date = selectedDate,
                    hour = timeSlot.startHour,
                    minute = timeSlot.startMinute
                )

            val endDateTime =
                AppointmentDateUtils.createDateTime(
                    date = selectedDate,
                    hour = timeSlot.endHour,
                    minute = timeSlot.endMinute
                )

            createAppointmentSlot(
                dentistId = dentistId,
                startDateTime = startDateTime,
                endDateTime = endDateTime
            )
        }

        return Tasks.whenAll(tasks)
    }

    fun cancelAppointmentSlots(
        timeSlots: List<TimeSlotItem>
    ): Task<Void> {

        val appointmentIds = timeSlots
            .mapNotNull { timeSlot ->
                timeSlot.appointmentId
            }
            .filter { appointmentId ->
                appointmentId.isNotBlank()
            }

        if (appointmentIds.isEmpty()) {
            return createFailedTask(
                "Няма избрани часове за премахване."
            )
        }

        val tasks = appointmentIds.map { appointmentId ->
            appointmentRepository.cancelAppointmentSlot(
                appointmentId
            )
        }

        return Tasks.whenAll(tasks)
    }

    fun getDentistSlotsForDate(
        dentistId: String,
        selectedDate: Long
    ): Task<List<AppointmentSlot>> {

        if (dentistId.isBlank()) {
            return Tasks.forException(
                IllegalArgumentException(
                    "Липсва идентификатор на стоматолога."
                )
            )
        }

        if (selectedDate <= 0L) {
            return Tasks.forException(
                IllegalArgumentException(
                    "Невалидна избрана дата."
                )
            )
        }

        return appointmentRepository.getDentistSlotsForDate(
            dentistId = dentistId,
            startOfDay =
                AppointmentDateUtils.getStartOfDay(
                    selectedDate
                ),
            startOfNextDay =
                AppointmentDateUtils.getStartOfNextDay(
                    selectedDate
                )
        )
    }

    private fun createAppointmentSlot(
        dentistId: String,
        startDateTime: Long,
        endDateTime: Long
    ): Task<Void> {

        if (
            startDateTime <= 0L ||
            endDateTime <= 0L
        ) {
            return createFailedTask(
                "Невалидна дата или час."
            )
        }

        if (endDateTime <= startDateTime) {
            return createFailedTask(
                "Крайният час трябва да бъде след началния час."
            )
        }

        if (startDateTime <= System.currentTimeMillis()) {
            return createFailedTask(
                "Не може да бъде добавен час в миналото."
            )
        }

        val appointmentSlot = AppointmentSlot(
            dentistId = dentistId,
            startDateTime = startDateTime,
            endDateTime = endDateTime
        )

        return appointmentRepository.createAppointmentSlot(
            appointmentSlot
        )
    }

    private fun createFailedTask(
        message: String
    ): Task<Void> {
        return Tasks.forException(
            IllegalArgumentException(message)
        )
    }
}