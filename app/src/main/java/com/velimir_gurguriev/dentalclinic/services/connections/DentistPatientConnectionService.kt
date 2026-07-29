package com.velimir_gurguriev.dentalclinic.services.connections

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.QuerySnapshot
import com.velimir_gurguriev.dentalclinic.models.connections.DentistPatientStatus
import com.velimir_gurguriev.dentalclinic.repositories.DentistPatientConnectionRepository

class DentistPatientConnectionService(
    private val connectionRepository: DentistPatientConnectionRepository
) {

    fun sendConnectionRequest(
        patientId: String,
        dentistId: String
    ): Task<Void> {

        if (patientId.isBlank()) {
            return Tasks.forException(
                IllegalArgumentException("Patient ID cannot be empty.")
            )
        }

        if (dentistId.isBlank()) {
            return Tasks.forException(
                IllegalArgumentException("Dentist ID cannot be empty.")
            )
        }

        if (patientId == dentistId) {
            return Tasks.forException(
                IllegalArgumentException(
                    "A user cannot send a connection request to themselves."
                )
            )
        }

        return connectionRepository
            .getConnection(patientId, dentistId)
            .continueWithTask { task ->

                if (!task.isSuccessful) {
                    throw task.exception
                        ?: IllegalStateException(
                            "Failed to check the existing connection."
                        )
                }

                val querySnapshot = task.result

                if (querySnapshot.isEmpty) {
                    return@continueWithTask connectionRepository
                        .sendRequest(patientId, dentistId)
                        .continueWith { sendTask ->

                            if (!sendTask.isSuccessful) {
                                throw sendTask.exception
                                    ?: IllegalStateException(
                                        "Failed to send the connection request."
                                    )
                            }

                            null
                        }
                }

                val existingDocument = querySnapshot.documents.first()
                val existingStatus = existingDocument.getString("status")

                when (existingStatus) {
                    DentistPatientStatus.PENDING.name -> {
                        throw IllegalStateException(
                            "A connection request is already pending."
                        )
                    }

                    DentistPatientStatus.APPROVED.name -> {
                        throw IllegalStateException(
                            "This dentist has already approved you as a patient."
                        )
                    }

                    DentistPatientStatus.REJECTED.name -> {
                        connectionRepository.resendRejectedRequest(
                            existingDocument.id
                        )
                    }

                    else -> {
                        throw IllegalStateException(
                            "The existing connection has an invalid status."
                        )
                    }
                }
            }
    }

    fun getPendingRequestsForDentist(
        dentistId: String
    ): Task<QuerySnapshot> {

        if (dentistId.isBlank()) {
            return Tasks.forException(
                IllegalArgumentException("Dentist ID cannot be empty.")
            )
        }

        return connectionRepository
            .getPendingRequestsForDentist(dentistId)
    }

    fun approveRequest(
        connectionId: String
    ): Task<Void> {

        if (connectionId.isBlank()) {
            return Tasks.forException(
                IllegalArgumentException("Connection ID cannot be empty.")
            )
        }

        return connectionRepository.approveRequest(connectionId)
    }

    fun rejectRequest(
        connectionId: String
    ): Task<Void> {

        if (connectionId.isBlank()) {
            return Tasks.forException(
                IllegalArgumentException("Connection ID cannot be empty.")
            )
        }

        return connectionRepository.rejectRequest(connectionId)
    }
}