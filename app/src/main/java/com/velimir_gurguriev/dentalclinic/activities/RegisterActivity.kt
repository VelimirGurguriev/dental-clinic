package com.velimir_gurguriev.dentalclinic.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.velimir_gurguriev.dentalclinic.databinding.ActivityRegisterBinding
import com.velimir_gurguriev.dentalclinic.models.forms.RegisterForm
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.services.users.RegisterUserService
import com.velimir_gurguriev.dentalclinic.validators.register.RegisterFormValidator

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerFormValidator: RegisterFormValidator
    private lateinit var registerUserService: RegisterUserService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeDependencies()
        setupClickListeners()
    }

    private fun initializeDependencies() {
        registerFormValidator = RegisterFormValidator()
        registerUserService = RegisterUserService(
            authRepository = AuthRepository(),
            userRepository = UserRepository()
        )
    }

    private fun setupClickListeners() {
        binding.registerButton.setOnClickListener {
            handleRegistration()
        }

        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun handleRegistration() {
        val form = readRegistrationForm()
        val validationResult = registerFormValidator.validate(form)

        if (!validationResult.isValid) {
            showMessage(validationResult.message)
            return
        }

        registerUserService.register(
            form = form,
            onSuccess = {
                showMessage("Успешна регистрация.")
            },
            onFailure = { exception ->
                showMessage("Грешка: ${exception.message}")
            }
        )
    }

    private fun readRegistrationForm(): RegisterForm {
        return RegisterForm(
            name = binding.nameInputField.text.toString().trim(),
            email = binding.emailInputField.text.toString().trim(),
            password = binding.passwordInputField.text.toString(),
            accountType = getSelectedAccountType()
        )
    }

    private fun getSelectedAccountType(): String {
        return when (binding.accountTypeRadioGroup.checkedRadioButtonId) {
            binding.patientRadioButton.id -> "patient"
            binding.dentistRadioButton.id -> "dentist"
            else -> ""
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
