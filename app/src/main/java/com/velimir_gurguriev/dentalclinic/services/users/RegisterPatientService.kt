package com.velimir_gurguriev.dentalclinic.services.users

import com.velimir_gurguriev.dentalclinic.models.User
import com.velimir_gurguriev.dentalclinic.models.forms.PatientRegisterForm
import com.velimir_gurguriev.dentalclinic.models.patient.PatientProfile
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.PatientRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository

class RegisterPatientService(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val patientRepository: PatientRepository
) {

    fun register(
        form: PatientRegisterForm,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        authRepository.registerUser(
            email = form.email,
            password = form.password,
            onSuccess = { uid ->

                val user =
                    User(
                        uid = uid,
                        name = form.name,
                        email = form.email,
                        phone = form.phone,
                        city = form.city,
                        accountType = PATIENT_ACCOUNT_TYPE
                    )

                val patientProfile =
                    PatientProfile(
                        uid = uid,
                        dateOfBirth = form.dateOfBirth
                    )

                userRepository.saveUserProfile(
                    user = user,
                    onSuccess = {
                        savePatientProfile(
                            patientProfile = patientProfile,
                            onSuccess = onSuccess,
                            onFailure = onFailure
                        )
                    },
                    onFailure = onFailure
                )
            },
            onFailure = onFailure
        )
    }

    private fun savePatientProfile(
        patientProfile: PatientProfile,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        patientRepository.savePatientProfile(
            patientProfile = patientProfile,
            onSuccess = {
                authRepository.logout()
                onSuccess()
            },
            onFailure = onFailure
        )
    }

    companion object {
        private const val PATIENT_ACCOUNT_TYPE = "patient"
    }
}