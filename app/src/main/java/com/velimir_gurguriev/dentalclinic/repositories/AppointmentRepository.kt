package com.velimir_gurguriev.dentalclinic.repositories

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.velimir_gurguriev.dentalclinic.models.appointments.AppointmentSlot
import com.velimir_gurguriev.dentalclinic.models.appointments.AppointmentStatus
import com.velimir_gurguriev.dentalclinic.models.connections.DentistPatientStatus

class AppointmentRepository {

    private val firestore =
        FirebaseFirestore.getInstance()

    private val appointmentSlotsCollection =
        firestore.collection(
            APPOINTMENT_SLOTS_COLLECTION
        )

    fun createAppointmentSlotIfAbsent(
        appointmentSlot: AppointmentSlot
    ): Task<Void> {

        val documentId =
            "${appointmentSlot.dentistId}_${appointmentSlot.startDateTime}"

        val documentReference =
            appointmentSlotsCollection.document(documentId)

        return firestore.runTransaction { transaction ->

            val snapshot = transaction.get(documentReference)

            if (!snapshot.exists()) {
                transaction.set(
                    documentReference,
                    appointmentSlot.copy(id = documentId)
                )
            }

            null
        }
    }

    fun getDentistSlotsForPeriod(
        dentistId: String,
        startDateTime: Long,
        endDateTime: Long
    ): Task<List<AppointmentSlot>> {

        return appointmentSlotsCollection
            .whereEqualTo(DENTIST_ID_FIELD, dentistId)
            .whereGreaterThanOrEqualTo(
                START_DATE_TIME_FIELD,
                startDateTime
            )
            .whereLessThan(
                START_DATE_TIME_FIELD,
                endDateTime
            )
            .get()
            .continueWith { task ->

                if (!task.isSuccessful) {
                    throw task.exception
                        ?: Exception(
                            "Неуспешно зареждане на часовете."
                        )
                }

                task.result.documents.mapNotNull { document ->
                    document.toObject(
                        AppointmentSlot::class.java
                    )?.copy(id = document.id)
                }
            }
    }

    fun createAppointmentSlot(
        appointmentSlot: AppointmentSlot
    ): Task<Void> {

        val documentReference =
            if (appointmentSlot.id.isBlank()) {
                appointmentSlotsCollection.document()
            } else {
                appointmentSlotsCollection.document(
                    appointmentSlot.id
                )
            }

        val slotWithId =
            appointmentSlot.copy(
                id = documentReference.id
            )

        return documentReference.set(
            slotWithId
        )
    }

    fun getDentistSlotsForDate(
        dentistId: String,
        startOfDay: Long,
        startOfNextDay: Long
    ): Task<List<AppointmentSlot>> {

        return appointmentSlotsCollection
            .whereEqualTo(
                DENTIST_ID_FIELD,
                dentistId
            )
            .whereGreaterThanOrEqualTo(
                START_DATE_TIME_FIELD,
                startOfDay
            )
            .whereLessThan(
                START_DATE_TIME_FIELD,
                startOfNextDay
            )
            .orderBy(
                START_DATE_TIME_FIELD,
                Query.Direction.ASCENDING
            )
            .get()
            .continueWith { task ->

                if (!task.isSuccessful) {
                    throw task.exception
                        ?: Exception(
                            "Неуспешно зареждане на часовете."
                        )
                }

                task.result.documents
                    .mapNotNull { document ->

                        document
                            .toObject(
                                AppointmentSlot::class.java
                            )
                            ?.copy(
                                id = document.id
                            )
                    }
            }
    }

    fun cancelAppointmentSlot(
        appointmentId: String
    ): Task<Void> {

        return appointmentSlotsCollection
            .document(appointmentId)
            .update(
                STATUS_FIELD,
                AppointmentStatus.CANCELLED.name
            )
    }

    fun requestAppointment(
        appointmentId: String,
        dentistId: String,
        patientId: String
    ): Task<Void> {

        val appointmentReference =
            appointmentSlotsCollection.document(appointmentId)

        val connectionsCollection =
            firestore.collection("dentistPatientConnections")

        return connectionsCollection
            .whereEqualTo("dentistId", dentistId)
            .whereEqualTo("patientId", patientId)
            .whereEqualTo(
                "status",
                DentistPatientStatus.APPROVED.name
            )
            .limit(1)
            .get()
            .continueWithTask { connectionTask ->

                if (!connectionTask.isSuccessful) {
                    throw connectionTask.exception
                        ?: IllegalStateException(
                            "Неуспешна проверка на връзката със стоматолога."
                        )
                }

                if (connectionTask.result.isEmpty) {
                    throw IllegalStateException(
                        "Нямате одобрена връзка с този стоматолог."
                    )
                }

                firestore.runTransaction { transaction ->

                    val snapshot =
                        transaction.get(appointmentReference)

                    if (!snapshot.exists()) {
                        throw IllegalStateException(
                            "Избраният час вече не съществува."
                        )
                    }

                    val appointment =
                        snapshot.toObject(AppointmentSlot::class.java)
                            ?: throw IllegalStateException(
                                "Невалидни данни за часа."
                            )

                    if (appointment.dentistId != dentistId) {
                        throw IllegalStateException(
                            "Часът не принадлежи на избрания стоматолог."
                        )
                    }

                    if (appointment.status != AppointmentStatus.AVAILABLE.name) {
                        throw IllegalStateException(
                            "Този час вече не е свободен."
                        )
                    }

                    if (appointment.startDateTime <= System.currentTimeMillis()) {
                        throw IllegalStateException(
                            "Не можете да заявите изминал час."
                        )
                    }

                    transaction.update(
                        appointmentReference,
                        mapOf(
                            "status" to AppointmentStatus.PENDING.name,
                            "patientId" to patientId
                        )
                    )

                    null
                }
            }
    }

    fun getPendingAppointmentsForDentist(
        dentistId: String
    ): Task<List<AppointmentSlot>> {

        return appointmentSlotsCollection
            .whereEqualTo("dentistId", dentistId)
            .whereEqualTo(
                "status",
                AppointmentStatus.PENDING.name
            )
            .get()
            .continueWith { task ->

                if (!task.isSuccessful) {
                    throw task.exception
                        ?: IllegalStateException(
                            "Неуспешно зареждане на заявките."
                        )
                }

                task.result.documents.mapNotNull { document ->
                    document.toObject(
                        AppointmentSlot::class.java
                    )?.copy(id = document.id)
                }.sortedBy { it.startDateTime }
            }
    }

    fun approveAppointment(
        appointmentId: String,
        dentistId: String
    ): Task<Void> {

        val reference = appointmentSlotsCollection.document(appointmentId)

        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(reference)

            val appointment = snapshot.toObject(
                AppointmentSlot::class.java
            ) ?: throw IllegalStateException(
                "Часът не е намерен."
            )

            if (
                appointment.dentistId != dentistId ||
                appointment.status != AppointmentStatus.PENDING.name ||
                appointment.patientId.isNullOrBlank()
            ) {
                throw IllegalStateException(
                    "Заявката вече не може да бъде одобрена."
                )
            }

            if (appointment.startDateTime <= System.currentTimeMillis()) {
                throw IllegalStateException(
                    "Не може да бъде одобрен изминал час."
                )
            }

            transaction.update(
                reference,
                mapOf(
                    "status" to AppointmentStatus.BOOKED.name,
                    "bookedAt" to System.currentTimeMillis()
                )
            )

            null
        }
    }

    fun rejectAppointment(
        appointmentId: String,
        dentistId: String
    ): Task<Void> {

        val reference = appointmentSlotsCollection.document(appointmentId)

        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(reference)

            val appointment = snapshot.toObject(
                AppointmentSlot::class.java
            ) ?: throw IllegalStateException(
                "Часът не е намерен."
            )

            if (
                appointment.dentistId != dentistId ||
                appointment.status != AppointmentStatus.PENDING.name
            ) {
                throw IllegalStateException(
                    "Заявката вече не може да бъде отхвърлена."
                )
            }

            transaction.update(
                reference,
                mapOf(
                    "status" to AppointmentStatus.AVAILABLE.name,
                    "patientId" to null,
                    "bookedAt" to null
                )
            )

            null
        }
    }

    fun getAppointmentById(
        appointmentId: String
    ): Task<AppointmentSlot> {

        return appointmentSlotsCollection
            .document(appointmentId)
            .get()
            .continueWith { task ->

                if (!task.isSuccessful) {
                    throw task.exception
                        ?: IllegalStateException(
                            "Неуспешно зареждане на часа."
                        )
                }

                val document = task.result

                document.toObject(AppointmentSlot::class.java)
                    ?.copy(id = document.id)
                    ?: throw IllegalStateException(
                        "Часът не е намерен."
                    )
            }
    }

    companion object {

        private const val APPOINTMENT_SLOTS_COLLECTION =
            "appointmentSlots"

        private const val DENTIST_ID_FIELD =
            "dentistId"

        private const val START_DATE_TIME_FIELD =
            "startDateTime"

        private const val STATUS_FIELD =
            "status"
    }
}