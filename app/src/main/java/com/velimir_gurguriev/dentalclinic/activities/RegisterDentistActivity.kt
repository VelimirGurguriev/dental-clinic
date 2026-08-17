package com.velimir_gurguriev.dentalclinic.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.velimir_gurguriev.dentalclinic.databinding.ActivityRegisterDentistBinding
import com.velimir_gurguriev.dentalclinic.models.forms.DentistRegisterForm
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.DentistRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.services.users.RegisterDentistService
import com.velimir_gurguriev.dentalclinic.utils.ui.SnackbarUtils
import com.velimir_gurguriev.dentalclinic.validators.register.DentistRegisterFormValidator

class RegisterDentistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterDentistBinding
    private lateinit var registerFormValidator: DentistRegisterFormValidator
    private lateinit var registerDentistService: RegisterDentistService

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityRegisterDentistBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        initializeDependencies()
        setupClickListeners()
    }

    private fun initializeDependencies() {
        registerFormValidator = DentistRegisterFormValidator()

        registerDentistService =
            RegisterDentistService(
                authRepository = AuthRepository(),
                userRepository = UserRepository(),
                dentistRepository = DentistRepository()
            )
    }

    private fun setupClickListeners() {
        binding.registerButton
            .setOnClickListener {
                handleRegistration()
            }

        binding.loginButton
            .setOnClickListener {
                openLogin()
            }
    }

    private fun handleRegistration() {
        val form =
            readRegistrationForm()

        val validationResult =
            registerFormValidator.validate(
                form
            )

        if (!validationResult.isValid) {
            showMessage(
                validationResult.message
            )
            return
        }

        registerDentistService.register(
            form = form,
            onSuccess = {
                openLogin()
            },
            onFailure = { exception ->
                showMessage(
                    exception.message
                        ?: "Регистрацията не беше успешна."
                )
            }
        )
    }

    private fun readRegistrationForm(): DentistRegisterForm {
        return DentistRegisterForm(
            name =
                binding.nameInputField
                    .text
                    .toString()
                    .trim(),
            email =
                binding.emailInputField
                    .text
                    .toString()
                    .trim(),
            password =
                binding.passwordInputField
                    .text
                    .toString(),
            phone =
                binding.phoneInputField
                    .text
                    .toString()
                    .trim(),
            city =
                binding.cityInputField
                    .text
                    .toString()
                    .trim(),
            specialization =
                binding.specializationInputField
                    .text
                    .toString()
                    .trim(),
            clinicName =
                binding.clinicNameInputField
                    .text
                    .toString()
                    .trim(),
            clinicAddress =
                binding.clinicAddressInputField
                    .text
                    .toString()
                    .trim()
        )
    }

    private fun openLogin() {
        startActivity(
            Intent(
                this,
                LoginActivity::class.java
            )
        )

        finish()
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