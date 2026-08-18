package com.velimir_gurguriev.dentalclinic.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.velimir_gurguriev.dentalclinic.models.dentist.DentistProfile

class DentistRepository {

    private val database = FirebaseFirestore.getInstance()

    private val dentistsCollection =
        database.collection(
            DENTISTS_COLLECTION
        )

    fun saveDentistProfile(
        dentistProfile: DentistProfile,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        dentistsCollection
            .document(
                dentistProfile.uid
            )
            .set(
                dentistProfile
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

    fun getDentistProfileById(
        uid: String,
        onSuccess: (DentistProfile) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        dentistsCollection
            .document(
                uid
            )
            .get()
            .addOnSuccessListener { document ->

                val dentistProfile =
                    document.toObject(
                        DentistProfile::class.java
                    )

                if (dentistProfile != null) {
                    onSuccess(
                        dentistProfile
                    )
                } else {
                    onFailure(
                        IllegalStateException(
                            "Профилът на стоматолога не е намерен."
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

    fun updateDentistProfile(
        uid: String,
        specialization: String,
        clinicName: String,
        clinicAddress: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val updates =
            mapOf(
                SPECIALIZATION_FIELD to specialization,
                CLINIC_NAME_FIELD to clinicName,
                CLINIC_ADDRESS_FIELD to clinicAddress
            )

        dentistsCollection
            .document(uid)
            .update(updates)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    companion object {
        private const val DENTISTS_COLLECTION = "dentists"
        private const val SPECIALIZATION_FIELD = "specialization"
        private const val CLINIC_NAME_FIELD = "clinicName"
        private const val CLINIC_ADDRESS_FIELD = "clinicAddress"
    }
}