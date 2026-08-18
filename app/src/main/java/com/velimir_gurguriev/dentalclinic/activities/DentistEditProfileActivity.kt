package com.velimir_gurguriev.dentalclinic.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.velimir_gurguriev.dentalclinic.databinding.ActivityDentistEditProfileBinding
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.DentistRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.utils.ui.SnackbarUtils

class DentistEditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDentistEditProfileBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var userRepository: UserRepository
    private lateinit var dentistRepository: DentistRepository

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityDentistEditProfileBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        initializeDependencies()
        setupClickListeners()
        loadProfile()
    }

    private fun initializeDependencies() {
        authRepository = AuthRepository()

        userRepository = UserRepository()

        dentistRepository = DentistRepository()
    }

    private fun setupClickListeners() {
        binding.saveButton
            .setOnClickListener {
                updateProfile()
            }

        binding.cancelButton
            .setOnClickListener {
                finish()
            }
    }

    private fun loadProfile() {
        val userId = getCurrentUserId() ?: return

        loadUser(
            userId
        )

        loadDentistProfile(
            userId
        )
    }

    private fun loadUser(
        userId: String
    ) {
        userRepository.getUserById(
            uid = userId,
            onSuccess = { user ->

                binding.nameInputField.setText(
                    user.name
                )

                binding.emailInputField.setText(
                    user.email
                )

                binding.phoneInputField.setText(
                    user.phone
                )

                binding.cityInputField.setText(
                    user.city
                )
            },
            onFailure = {
                showMessage(
                    "Профилът не може да бъде зареден."
                )
            }
        )
    }

    private fun loadDentistProfile(
        userId: String
    ) {
        dentistRepository.getDentistProfileById(
            uid = userId,
            onSuccess = { dentistProfile ->

                binding.specializationInputField.setText(
                    dentistProfile.specialization
                )

                binding.clinicNameInputField.setText(
                    dentistProfile.clinicName
                )

                binding.clinicAddressInputField.setText(
                    dentistProfile.clinicAddress
                )
            },
            onFailure = {
                showMessage(
                    "Професионалната информация не може да бъде заредена."
                )
            }
        )
    }

    private fun updateProfile() {
        val userId = getCurrentUserId() ?: return

        val name =
            binding.nameInputField
                .text
                .toString()
                .trim()

        val phone =
            binding.phoneInputField
                .text
                .toString()
                .trim()

        val city =
            binding.cityInputField
                .text
                .toString()
                .trim()

        val specialization =
            binding.specializationInputField
                .text
                .toString()
                .trim()

        val clinicName =
            binding.clinicNameInputField
                .text
                .toString()
                .trim()

        val clinicAddress =
            binding.clinicAddressInputField
                .text
                .toString()
                .trim()

        if (name.isEmpty()) {
            binding.nameInputField.error =
                "Въведете име."

            return
        }

        userRepository.updateUserProfile(
            uid = userId,
            name = name,
            phone = phone,
            city = city,
            onSuccess = {
                updateDentistProfile(
                    userId = userId,
                    specialization = specialization,
                    clinicName = clinicName,
                    clinicAddress = clinicAddress
                )
            },
            onFailure = {
                showMessage(
                    "Профилът не може да бъде обновен."
                )
            }
        )
    }

    private fun updateDentistProfile(
        userId: String,
        specialization: String,
        clinicName: String,
        clinicAddress: String
    ) {
        dentistRepository.updateDentistProfile(
            uid = userId,
            specialization = specialization,
            clinicName = clinicName,
            clinicAddress = clinicAddress,
            onSuccess = {
                showMessage(
                    "Профилът е обновен."
                )

                finish()
            },
            onFailure = {
                showMessage(
                    "Професионалната информация не може да бъде обновена."
                )
            }
        )
    }

    private fun getCurrentUserId(): String? {
        val userId =
            authRepository.getCurrentUserId()

        if (userId == null) {
            showMessage(
                "Няма влязъл потребител."
            )
        }

        return userId
    }

    private fun showMessage(
        message: String
    ) {
        SnackbarUtils.show(
            rootView = binding.root,
            message = message
        )
    }
}