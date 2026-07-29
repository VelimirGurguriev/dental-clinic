package com.velimir_gurguriev.dentalclinic.repositories

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.velimir_gurguriev.dentalclinic.models.connections.DentistPatientStatus

class DentistPatientConnectionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val connectionsCollection =
        firestore.collection(CONNECTIONS_COLLECTION)

    fun getConnection(
        patientId: String,
        dentistId: String
    ): Task<QuerySnapshot> {
        return connectionsCollection
            .whereEqualTo(PATIENT_ID_FIELD, patientId)
            .whereEqualTo(DENTIST_ID_FIELD, dentistId)
            .limit(1)
            .get()
    }

    fun sendRequest(
        patientId: String,
        dentistId: String
    ): Task<DocumentReference> {
        val connectionData = hashMapOf(
            PATIENT_ID_FIELD to patientId,
            DENTIST_ID_FIELD to dentistId,
            STATUS_FIELD to DentistPatientStatus.PENDING.name,
            REQUESTED_AT_FIELD to FieldValue.serverTimestamp(),
            RESPONDED_AT_FIELD to null
        )

        return connectionsCollection.add(connectionData)
    }

    fun getPendingRequestsForDentist(
        dentistId: String
    ): Task<QuerySnapshot> {
        return connectionsCollection
            .whereEqualTo(DENTIST_ID_FIELD, dentistId)
            .whereEqualTo(
                STATUS_FIELD,
                DentistPatientStatus.PENDING.name
            )
            .orderBy(REQUESTED_AT_FIELD, Query.Direction.DESCENDING)
            .get()
    }

    fun getApprovedDentistsForPatient(
        patientId: String
    ): Task<QuerySnapshot> {
        return connectionsCollection
            .whereEqualTo(PATIENT_ID_FIELD, patientId)
            .whereEqualTo(
                STATUS_FIELD,
                DentistPatientStatus.APPROVED.name
            )
            .get()
    }

    fun approveRequest(
        connectionId: String
    ): Task<Void> {
        return updateRequestStatus(
            connectionId = connectionId,
            status = DentistPatientStatus.APPROVED
        )
    }

    fun rejectRequest(
        connectionId: String
    ): Task<Void> {
        return updateRequestStatus(
            connectionId = connectionId,
            status = DentistPatientStatus.REJECTED
        )
    }

    private fun updateRequestStatus(
        connectionId: String,
        status: DentistPatientStatus
    ): Task<Void> {
        val updates = mapOf(
            STATUS_FIELD to status.name,
            RESPONDED_AT_FIELD to FieldValue.serverTimestamp()
        )

        return connectionsCollection
            .document(connectionId)
            .update(updates)
    }

    companion object {
        private const val CONNECTIONS_COLLECTION =
            "dentistPatientConnections"

        private const val PATIENT_ID_FIELD = "patientId"
        private const val DENTIST_ID_FIELD = "dentistId"
        private const val STATUS_FIELD = "status"
        private const val REQUESTED_AT_FIELD = "requestedAt"
        private const val RESPONDED_AT_FIELD = "respondedAt"
    }

    fun resendRejectedRequest(
        connectionId: String
    ): Task<Void> {
        val updates = mapOf(
            STATUS_FIELD to DentistPatientStatus.PENDING.name,
            REQUESTED_AT_FIELD to FieldValue.serverTimestamp(),
            RESPONDED_AT_FIELD to null
        )

        return connectionsCollection
            .document(connectionId)
            .update(updates)
    }
}