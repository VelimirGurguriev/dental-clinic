package com.velimir_gurguriev.dentalclinic.services.appointments

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.velimir_gurguriev.dentalclinic.models.appointments.AppointmentSlot
import com.velimir_gurguriev.dentalclinic.repositories.AppointmentRepository
import java.util.Calendar

class AppointmentService(
    private val appointmentRepository: AppointmentRepository
) {

    fun createAppointmentSlot(
        dentistId: String,
        startDateTime: Long,
        endDateTime: Long
    ): Task<Void> {

        if (dentistId.isBlank()) {
            return Tasks.forException(
                IllegalArgumentException(
                    "Липсва идентификатор на стоматолога."
                )
            )
        }

        if (startDateTime <= 0L || endDateTime <= 0L) {
            return Tasks.forException(
                IllegalArgumentException(
                    "Невалидна дата или час."
                )
            )
        }

        if (endDateTime <= startDateTime) {
            return Tasks.forException(
                IllegalArgumentException(
                    "Крайният час трябва да бъде след началния час."
                )
            )
        }

        if (startDateTime <= System.currentTimeMillis()) {
            return Tasks.forException(
                IllegalArgumentException(
                    "Не може да бъде добавен час в миналото."
                )
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

    fun cancelAppointmentSlot(
        appointmentId: String
    ): Task<Void> {

        if (appointmentId.isBlank()) {
            return Tasks.forException(
                IllegalArgumentException(
                    "Липсва идентификатор на часа."
                )
            )
        }

        return appointmentRepository.cancelAppointmentSlot(
            appointmentId
        )
    }

    fun getAvailableSlots(
        dentistId: String,
        startOfDay: Long,
        endOfDay: Long,
        onSuccess: (List<AppointmentSlot>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (dentistId.isBlank()) {
            onFailure(
                IllegalArgumentException(
                    "Липсва идентификатор на стоматолога."
                )
            )
            return
        }

        appointmentRepository.getAvailableSlots(
            dentistId = dentistId,
            startOfDay = startOfDay,
            endOfDay = endOfDay,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
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

        val startOfDay = Calendar.getInstance().apply {
            timeInMillis = selectedDate

            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startOfNextDay = Calendar.getInstance().apply {
            timeInMillis = startOfDay.timeInMillis
            add(Calendar.DAY_OF_MONTH, 1)
        }

        return appointmentRepository.getDentistSlotsForDate(
            dentistId = dentistId,
            startOfDay = startOfDay.timeInMillis,
            startOfNextDay = startOfNextDay.timeInMillis
        )
    }

    fun getDentistAppointments(
        dentistId: String,
        fromDateTime: Long,
        onSuccess: (List<AppointmentSlot>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (dentistId.isBlank()) {
            onFailure(
                IllegalArgumentException(
                    "Липсва идентификатор на стоматолога."
                )
            )
            return
        }

        appointmentRepository.getDentistAppointments(
            dentistId = dentistId,
            fromDateTime = fromDateTime,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun getPatientAppointments(
        patientId: String,
        fromDateTime: Long,
        onSuccess: (List<AppointmentSlot>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (patientId.isBlank()) {
            onFailure(
                IllegalArgumentException(
                    "Липсва идентификатор на пациента."
                )
            )
            return
        }

        appointmentRepository.getPatientAppointments(
            patientId = patientId,
            fromDateTime = fromDateTime,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
}