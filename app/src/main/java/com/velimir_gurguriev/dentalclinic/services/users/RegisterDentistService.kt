package com.velimir_gurguriev.dentalclinic.services.users

import com.velimir_gurguriev.dentalclinic.models.User
import com.velimir_gurguriev.dentalclinic.models.dentist.DentistProfile
import com.velimir_gurguriev.dentalclinic.models.forms.DentistRegisterForm
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.DentistRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository

class RegisterDentistService(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val dentistRepository: DentistRepository
) {

    fun register(
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
        dentistRepository.saveDentistProfile(
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
    }
}