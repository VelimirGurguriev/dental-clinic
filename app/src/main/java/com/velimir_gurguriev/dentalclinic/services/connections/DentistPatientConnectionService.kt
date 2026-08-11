package com.velimir_gurguriev.dentalclinic.services.connections

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.velimir_gurguriev.dentalclinic.models.connections.DentistPatientItem
import com.velimir_gurguriev.dentalclinic.models.connections.DentistPatientStatus
import com.velimir_gurguriev.dentalclinic.models.connections.PatientRequestItem
import com.velimir_gurguriev.dentalclinic.repositories.DentistPatientConnectionRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository

class DentistPatientConnectionService(
    private val connectionRepository: DentistPatientConnectionRepository,
    private val userRepository: UserRepository = UserRepository()
) {

    fun sendConnectionRequest(
        patientId: String,
        dentistId: String
    ): Task<Void> {

        if (patientId.isBlank()) {
            return failedTask(
                "Липсва ID на пациента."
            )
        }

        if (dentistId.isBlank()) {
            return failedTask(
                "Липсва ID на стоматолога."
            )
        }

        if (patientId == dentistId) {
            return failedTask(
                "Не можете да изпратите заявка към себе си."
            )
        }

        return connectionRepository
            .getConnection(
                patientId = patientId,
                dentistId = dentistId
            )
            .continueWithTask { task ->

                if (!task.isSuccessful) {
                    throw task.exception
                        ?: IllegalStateException(
                            "Неуспешна проверка за съществуваща връзка."
                        )
                }

                val querySnapshot =
                    task.result

                if (querySnapshot.isEmpty) {
                    return@continueWithTask connectionRepository
                        .sendRequest(
                            patientId = patientId,
                            dentistId = dentistId
                        )
                        .continueWith { sendTask ->

                            if (!sendTask.isSuccessful) {
                                throw sendTask.exception
                                    ?: IllegalStateException(
                                        "Заявката не можа да бъде изпратена."
                                    )
                            }

                            null
                        }
                }

                val existingDocument =
                    querySnapshot.documents.first()

                val existingStatus =
                    existingDocument.getString(
                        STATUS_FIELD
                    )

                when (existingStatus) {
                    DentistPatientStatus.PENDING.name -> {
                        throw IllegalStateException(
                            "Вече има чакаща заявка към този стоматолог."
                        )
                    }

                    DentistPatientStatus.APPROVED.name -> {
                        throw IllegalStateException(
                            "Вече сте пациент на този стоматолог."
                        )
                    }

                    DentistPatientStatus.REJECTED.name -> {
                        connectionRepository
                            .resendRejectedRequest(
                                existingDocument.id
                            )
                    }

                    else -> {
                        throw IllegalStateException(
                            "Връзката е с невалиден статус."
                        )
                    }
                }
            }
    }

    fun getPendingRequestsForDentist(
        dentistId: String
    ): Task<List<PatientRequestItem>> {

        if (dentistId.isBlank()) {
            return Tasks.forException(
                IllegalArgumentException(
                    "Липсва ID на стоматолога."
                )
            )
        }

        return connectionRepository
            .getPendingRequestsForDentist(
                dentistId
            )
            .continueWithTask { task ->

                if (!task.isSuccessful) {
                    throw task.exception
                        ?: IllegalStateException(
                            "Заявките не можаха да бъдат заредени."
                        )
                }

                val itemTasks =
                    task.result.documents.mapNotNull { document ->

                        val patientId =
                            document.getString(
                                PATIENT_ID_FIELD
                            )

                        if (patientId.isNullOrBlank()) {
                            null
                        } else {
                            loadPatientRequestItem(
                                connectionId = document.id,
                                patientId = patientId
                            )
                        }
                    }

                if (itemTasks.isEmpty()) {
                    return@continueWithTask Tasks.forResult(
                        emptyList()
                    )
                }

                Tasks.whenAllSuccess<PatientRequestItem?>(
                    itemTasks
                )
                    .continueWith { itemsTask ->
                        itemsTask.result.filterNotNull()
                    }
            }
    }

    fun getApprovedPatientsForDentist(
        dentistId: String
    ): Task<List<DentistPatientItem>> {

        if (dentistId.isBlank()) {
            return Tasks.forException(
                IllegalArgumentException(
                    "Липсва ID на стоматолога."
                )
            )
        }

        return connectionRepository
            .getApprovedPatientsForDentist(
                dentistId
            )
            .continueWithTask { task ->

                if (!task.isSuccessful) {
                    throw task.exception
                        ?: IllegalStateException(
                            "Пациентите не можаха да бъдат заредени."
                        )
                }

                val itemTasks =
                    task.result.documents.mapNotNull { document ->

                        val patientId =
                            document.getString(
                                PATIENT_ID_FIELD
                            )

                        if (patientId.isNullOrBlank()) {
                            null
                        } else {
                            loadDentistPatientItem(
                                connectionId = document.id,
                                patientId = patientId
                            )
                        }
                    }

                if (itemTasks.isEmpty()) {
                    return@continueWithTask Tasks.forResult(
                        emptyList()
                    )
                }

                Tasks.whenAllSuccess<DentistPatientItem?>(
                    itemTasks
                )
                    .continueWith { itemsTask ->
                        itemsTask.result.filterNotNull()
                    }
            }
    }

    fun approveRequest(
        connectionId: String
    ): Task<Void> {

        if (connectionId.isBlank()) {
            return failedTask(
                "Липсва ID на заявката."
            )
        }

        return connectionRepository
            .approveRequest(
                connectionId
            )
    }

    fun rejectRequest(
        connectionId: String
    ): Task<Void> {

        if (connectionId.isBlank()) {
            return failedTask(
                "Липсва ID на заявката."
            )
        }

        return connectionRepository
            .rejectRequest(
                connectionId
            )
    }

    private fun loadPatientRequestItem(
        connectionId: String,
        patientId: String
    ): Task<PatientRequestItem?> {

        val taskCompletionSource =
            TaskCompletionSource<PatientRequestItem?>()

        userRepository.getUserById(
            uid = patientId,
            onSuccess = { patient ->

                taskCompletionSource.setResult(
                    PatientRequestItem(
                        connectionId = connectionId,
                        patient = patient
                    )
                )
            },
            onFailure = {
                taskCompletionSource.setResult(null)
            }
        )

        return taskCompletionSource.task
    }

    private fun loadDentistPatientItem(
        connectionId: String,
        patientId: String
    ): Task<DentistPatientItem?> {

        val taskCompletionSource =
            TaskCompletionSource<DentistPatientItem?>()

        userRepository.getUserById(
            uid = patientId,
            onSuccess = { patient ->

                taskCompletionSource.setResult(
                    DentistPatientItem(
                        connectionId = connectionId,
                        patient = patient
                    )
                )
            },
            onFailure = {
                taskCompletionSource.setResult(null)
            }
        )

        return taskCompletionSource.task
    }

    private fun failedTask(
        message: String
    ): Task<Void> {
        return Tasks.forException(
            IllegalArgumentException(message)
        )
    }

    companion object {
        private const val PATIENT_ID_FIELD =
            "patientId"

        private const val STATUS_FIELD =
            "status"
    }
}