package com.velimir_gurguriev.dentalclinic.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.velimir_gurguriev.dentalclinic.databinding.ActivityEditProfileBinding
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.utils.ui.SnackbarUtils

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var userRepository: UserRepository

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityEditProfileBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        initializeDependencies()
        setupClickListeners()
        loadCurrentUser()
    }

    private fun initializeDependencies() {
        authRepository = AuthRepository()

        userRepository = UserRepository()
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

    private fun loadCurrentUser() {
        val userId = getCurrentUserId() ?: return

        userRepository.getUserById(
            uid = userId,
            onSuccess = { user ->

                binding.nameInputField.setText(
                    user.name
                )

                binding.emailInputField.setText(
                    user.email
                )
            },
            onFailure = {
                showMessage(
                    "Профилът не може да бъде зареден."
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

        if (name.isEmpty()) {
            binding.nameInputField.error = "Въведете име."

            return
        }

        userRepository.updateUserName(
            uid = userId,
            name = name,
            onSuccess = {
                showMessage(
                    "Профилът е обновен."
                )

                finish()
            },
            onFailure = {
                showMessage(
                    "Профилът не може да бъде обновен."
                )
            }
        )
    }

    private fun getCurrentUserId(): String? {
        val userId = authRepository.getCurrentUserId()

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