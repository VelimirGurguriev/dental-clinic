package com.velimir_gurguriev.dentalclinic.repositories

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.velimir_gurguriev.dentalclinic.models.appointments.AppointmentSlot
import com.velimir_gurguriev.dentalclinic.models.appointments.AppointmentStatus

class AppointmentRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private val appointmentSlotsCollection =
        firestore.collection(APPOINTMENT_SLOTS_COLLECTION)

    fun createAppointmentSlot(
        appointmentSlot: AppointmentSlot
    ): Task<Void> {

        val documentReference =
            if (appointmentSlot.id.isBlank()) {
                appointmentSlotsCollection.document()
            } else {
                appointmentSlotsCollection.document(appointmentSlot.id)
            }

        val slotWithId = appointmentSlot.copy(
            id = documentReference.id
        )

        return documentReference.set(slotWithId)
    }

    fun getAvailableSlots(
        dentistId: String,
        startOfDay: Long,
        endOfDay: Long,
        onSuccess: (List<AppointmentSlot>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        appointmentSlotsCollection
            .whereEqualTo(
                DENTIST_ID_FIELD,
                dentistId
            )
            .whereEqualTo(
                STATUS_FIELD,
                AppointmentStatus.AVAILABLE.name
            )
            .whereGreaterThanOrEqualTo(
                START_DATE_TIME_FIELD,
                startOfDay
            )
            .whereLessThan(
                START_DATE_TIME_FIELD,
                endOfDay
            )
            .orderBy(
                START_DATE_TIME_FIELD,
                Query.Direction.ASCENDING
            )
            .get()
            .addOnSuccessListener { querySnapshot ->

                val slots =
                    querySnapshot.documents.mapNotNull { document ->
                        document
                            .toObject(AppointmentSlot::class.java)
                            ?.copy(id = document.id)
                    }

                onSuccess(slots)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getDentistAppointments(
        dentistId: String,
        fromDateTime: Long,
        onSuccess: (List<AppointmentSlot>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        appointmentSlotsCollection
            .whereEqualTo(
                DENTIST_ID_FIELD,
                dentistId
            )
            .whereGreaterThanOrEqualTo(
                START_DATE_TIME_FIELD,
                fromDateTime
            )
            .orderBy(
                START_DATE_TIME_FIELD,
                Query.Direction.ASCENDING
            )
            .get()
            .addOnSuccessListener { querySnapshot ->

                val appointments =
                    querySnapshot.documents.mapNotNull { document ->
                        document
                            .toObject(AppointmentSlot::class.java)
                            ?.copy(id = document.id)
                    }

                onSuccess(appointments)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
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

                task.result.documents.mapNotNull { document ->
                    document
                        .toObject(AppointmentSlot::class.java)
                        ?.copy(id = document.id)
                }
            }
    }

    fun getPatientAppointments(
        patientId: String,
        fromDateTime: Long,
        onSuccess: (List<AppointmentSlot>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        appointmentSlotsCollection
            .whereEqualTo(
                PATIENT_ID_FIELD,
                patientId
            )
            .whereEqualTo(
                STATUS_FIELD,
                AppointmentStatus.BOOKED.name
            )
            .whereGreaterThanOrEqualTo(
                START_DATE_TIME_FIELD,
                fromDateTime
            )
            .orderBy(
                START_DATE_TIME_FIELD,
                Query.Direction.ASCENDING
            )
            .get()
            .addOnSuccessListener { querySnapshot ->

                val appointments =
                    querySnapshot.documents.mapNotNull { document ->
                        document
                            .toObject(AppointmentSlot::class.java)
                            ?.copy(id = document.id)
                    }

                onSuccess(appointments)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
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

    companion object {
        private const val APPOINTMENT_SLOTS_COLLECTION =
            "appointmentSlots"

        private const val DENTIST_ID_FIELD =
            "dentistId"

        private const val PATIENT_ID_FIELD =
            "patientId"

        private const val START_DATE_TIME_FIELD =
            "startDateTime"

        private const val STATUS_FIELD =
            "status"
    }
}