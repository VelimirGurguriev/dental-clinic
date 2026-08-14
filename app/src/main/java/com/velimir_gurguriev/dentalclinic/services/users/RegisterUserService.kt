package com.velimir_gurguriev.dentalclinic.services.users

import com.velimir_gurguriev.dentalclinic.models.User
import com.velimir_gurguriev.dentalclinic.models.dentist.DentistProfile
import com.velimir_gurguriev.dentalclinic.models.forms.DentistRegisterForm
import com.velimir_gurguriev.dentalclinic.models.forms.PatientRegisterForm
import com.velimir_gurguriev.dentalclinic.models.forms.RegisterForm
import com.velimir_gurguriev.dentalclinic.models.patient.PatientProfile
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.DentistRepository
import com.velimir_gurguriev.dentalclinic.repositories.PatientRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository

class RegisterUserService(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val dentistRepository: DentistRepository? = null,
    private val patientRepository: PatientRepository? = null
) {

    fun register(
        form: RegisterForm,
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
                        accountType = form.accountType
                    )

                userRepository.saveUserProfile(
                    user = user,
                    onSuccess = onSuccess,
                    onFailure = onFailure
                )
            },
            onFailure = onFailure
        )
    }

    fun registerPatient(
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
        val repository =
            patientRepository
                ?: return onFailure(
                    IllegalStateException(
                        "Липсва хранилище за пациент."
                    )
                )

        repository.savePatientProfile(
            patientProfile = patientProfile,
            onSuccess = {
                authRepository.logout()
                onSuccess()
            },
            onFailure = onFailure
        )
    }

    fun registerDentist(
        form: DentistRegisterForm,
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
                        accountType = DENTIST_ACCOUNT_TYPE
                    )

                val dentistProfile =
                    DentistProfile(
                        uid = uid,
                        specialization = form.specialization,
                        clinicName = form.clinicName,
                        clinicAddress = form.clinicAddress
                    )

                userRepository.saveUserProfile(
                    user = user,
                    onSuccess = {
                        saveDentistProfile(
                            dentistProfile = dentistProfile,
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

    private fun saveDentistProfile(
        dentistProfile: DentistProfile,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val repository =
            dentistRepository
                ?: return onFailure(
                    IllegalStateException(
                        "Липсва хранилище за стоматолог."
                    )
                )

        repository.saveDentistProfile(
            dentistProfile = dentistProfile,
            onSuccess = {
                authRepository.logout()
                onSuccess()
            },
            onFailure = onFailure
        )
    }

    companion object {
        private const val DENTIST_ACCOUNT_TYPE = "dentist"
        private const val PATIENT_ACCOUNT_TYPE = "patient"
    }
}