package com.velimir_gurguriev.dentalclinic.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.velimir_gurguriev.dentalclinic.models.patient.PatientProfile

class PatientRepository {

    private val database = FirebaseFirestore.getInstance()

    private val patientsCollection =
        database.collection(
            PATIENTS_COLLECTION
        )

    fun savePatientProfile(
        patientProfile: PatientProfile,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        patientsCollection
            .document(
                patientProfile.uid
            )
            .set(
                patientProfile
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(
                    exception
                )
            }
    }

    fun getPatientProfileById(
        uid: String,
        onSuccess: (PatientProfile) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        patientsCollection
            .document(
                uid
            )
            .get()
            .addOnSuccessListener { document ->

                val patientProfile =
                    document.toObject(
                        PatientProfile::class.java
                    )

                if (patientProfile != null) {
                    onSuccess(
                        patientProfile
                    )
                } else {
                    onFailure(
                        IllegalStateException(
                            "Профилът на пациента не е намерен."
                        )
                    )
                }
            }
            .addOnFailureListener { exception ->
                onFailure(
                    exception
                )
            }
    }

    companion object {
        private const val PATIENTS_COLLECTION = "patients"
    }
}