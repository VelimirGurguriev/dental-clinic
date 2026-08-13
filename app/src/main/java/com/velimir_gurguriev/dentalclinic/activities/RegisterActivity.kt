package com.velimir_gurguriev.dentalclinic.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.velimir_gurguriev.dentalclinic.databinding.ActivityRegisterBinding
import com.velimir_gurguriev.dentalclinic.models.forms.RegisterForm
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.services.users.RegisterUserService
import com.velimir_gurguriev.dentalclinic.utils.ui.SnackbarUtils
import com.velimir_gurguriev.dentalclinic.validators.register.RegisterFormValidator

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerFormValidator: RegisterFormValidator
    private lateinit var registerUserService: RegisterUserService

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityRegisterBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        initializeDependencies()
        setupClickListeners()
    }

    private fun initializeDependencies() {
        registerFormValidator = RegisterFormValidator()

        registerUserService =
            RegisterUserService(
                authRepository = AuthRepository(),
                userRepository = UserRepository()
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
        val form = readRegistrationForm()

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

        registerUserService.register(
            form = form,
            onSuccess = {
                showMessage(
                    "Успешна регистрация."
                )
            },
            onFailure = { exception ->
                showMessage(
                    exception.message
                        ?: "Регистрацията не беше успешна."
                )
            }
        )
    }

    private fun readRegistrationForm(): RegisterForm {
        return RegisterForm(
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
            accountType =
                getSelectedAccountType()
        )
    }

    private fun getSelectedAccountType(): String {
        return when (
            binding.accountTypeRadioGroup.checkedRadioButtonId
        ) {
            binding.patientRadioButton.id -> PATIENT_ACCOUNT_TYPE

            binding.dentistRadioButton.id -> DENTIST_ACCOUNT_TYPE

            else ->
                ""
        }
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

    companion object {
        private const val PATIENT_ACCOUNT_TYPE = "patient"

        private const val DENTIST_ACCOUNT_TYPE = "dentist"
    }
}