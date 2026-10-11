package com.velimir_gurguriev.dentalclinic.services.appointments

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.velimir_gurguriev.dentalclinic.models.appointments.AppointmentSlot
import com.velimir_gurguriev.dentalclinic.models.appointments.TimeSlotItem
import com.velimir_gurguriev.dentalclinic.repositories.AppointmentRepository
import com.velimir_gurguriev.dentalclinic.utils.appointments.AppointmentDateUtils
import java.util.Calendar
import com.velimir_gurguriev.dentalclinic.utils.appointments.TimeSlotGenerator

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

        return appointmentRepository.createAppointmentSlotIfAbsent(
            appointmentSlot
        )
    }

    fun publishAppointmentSlotsForMonths(
        dentistId: String,
        monthsAhead: Int
    ): Task<Void> {

        if (dentistId.isBlank()) {
            return createFailedTask(
                "Липсва идентификатор на стоматолога."
            )
        }

        if (monthsAhead !in 1..2) {
            return createFailedTask(
                "Можете да публикувате за 1 или 2 месеца."
            )
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endDate = (today.clone() as Calendar).apply {
            add(Calendar.MONTH, monthsAhead)
        }

        val now = System.currentTimeMillis()

        return appointmentRepository.getDentistSlotsForPeriod(
            dentistId = dentistId,
            startDateTime = today.timeInMillis,
            endDateTime = endDate.timeInMillis
        ).continueWithTask { task ->

            if (!task.isSuccessful) {
                throw task.exception
                    ?: Exception(
                        "Неуспешно зареждане на часовете."
                    )
            }

            val existingSlots = task.result
                .map { it.startDateTime }
                .toHashSet()

            val timeSlots = TimeSlotGenerator.generate(
                startHour = 8,
                endHour = 16
            )

            val creationTasks = mutableListOf<Task<Void>>()

            val currentDate = today.clone() as Calendar

            while (currentDate.before(endDate)) {

                val dayOfWeek = currentDate.get(
                    Calendar.DAY_OF_WEEK
                )

                if (
                    dayOfWeek != Calendar.SATURDAY &&
                    dayOfWeek != Calendar.SUNDAY
                ) {

                    val selectedDate = currentDate.timeInMillis

                    timeSlots.forEach { slot ->

                        val startDateTime =
                            AppointmentDateUtils.createDateTime(
                                date = selectedDate,
                                hour = slot.startHour,
                                minute = slot.startMinute
                            )

                        if (
                            startDateTime > now &&
                            startDateTime !in existingSlots
                        ) {

                            val endDateTime =
                                AppointmentDateUtils.createDateTime(
                                    date = selectedDate,
                                    hour = slot.endHour,
                                    minute = slot.endMinute
                                )

                            val appointmentSlot = AppointmentSlot(
                                dentistId = dentistId,
                                startDateTime = startDateTime,
                                endDateTime = endDateTime
                            )

                            creationTasks.add(
                                appointmentRepository
                                    .createAppointmentSlotIfAbsent(
                                        appointmentSlot
                                    )
                            )
                        }
                    }
                }

                currentDate.add(Calendar.DAY_OF_MONTH, 1)
            }

            if (creationTasks.isEmpty()) {
                Tasks.forResult(null)
            } else {
                Tasks.whenAll(creationTasks)
            }
        }
    }

    fun requestAppointment(
        appointmentId: String,
        dentistId: String,
        patientId: String
    ): Task<Void> {

        if (appointmentId.isBlank()) {
            return createFailedTask(
                "Не е избран час."
            )
        }

        if (dentistId.isBlank() || patientId.isBlank()) {
            return createFailedTask(
                "Невалидни данни за пациента или стоматолога."
            )
        }

        return appointmentRepository.requestAppointment(
            appointmentId = appointmentId,
            dentistId = dentistId,
            patientId = patientId
        )
    }

    fun getPendingAppointmentsForDentist(
        dentistId: String
    ): Task<List<AppointmentSlot>> {

        if (dentistId.isBlank()) {
            return Tasks.forException(
                IllegalArgumentException(
                    "Липсва идентификатор на стоматолога."
                )
            )
        }

        return appointmentRepository.getPendingAppointmentsForDentist(
            dentistId
        )
    }

    fun approveAppointment(
        appointmentId: String,
        dentistId: String
    ): Task<Void> {

        if (appointmentId.isBlank() || dentistId.isBlank()) {
            return createFailedTask(
                "Невалидни данни за заявката."
            )
        }

        return appointmentRepository.approveAppointment(
            appointmentId,
            dentistId
        )
    }

    fun rejectAppointment(
        appointmentId: String,
        dentistId: String
    ): Task<Void> {

        if (appointmentId.isBlank() || dentistId.isBlank()) {
            return createFailedTask(
                "Невалидни данни за заявката."
            )
        }

        return appointmentRepository.rejectAppointment(
            appointmentId,
            dentistId
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