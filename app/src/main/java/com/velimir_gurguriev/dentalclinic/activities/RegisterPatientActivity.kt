package com.velimir_gurguriev.dentalclinic.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.velimir_gurguriev.dentalclinic.databinding.ActivityRegisterPatientBinding
import com.velimir_gurguriev.dentalclinic.models.forms.PatientRegisterForm
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.PatientRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.services.users.RegisterPatientService
import com.velimir_gurguriev.dentalclinic.utils.ui.SnackbarUtils
import com.velimir_gurguriev.dentalclinic.validators.register.PatientRegisterFormValidator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterPatientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterPatientBinding
    private lateinit var registerFormValidator: PatientRegisterFormValidator
    private lateinit var registerPatientService: RegisterPatientService

    private var selectedDateOfBirth: Long = 0L

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityRegisterPatientBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        initializeDependencies()
        setupClickListeners()
    }

    private fun initializeDependencies() {
        registerFormValidator = PatientRegisterFormValidator()

        registerPatientService =
            RegisterPatientService(
                authRepository = AuthRepository(),
                userRepository = UserRepository(),
                patientRepository = PatientRepository()
            )
    }

    private fun setupClickListeners() {
        binding.dateOfBirthInputField
            .setOnClickListener {
                showDateOfBirthPicker()
            }

        binding.registerButton
            .setOnClickListener {
                handleRegistration()
            }

        binding.loginButton
            .setOnClickListener {
                openLogin()
            }
    }

    private fun showDateOfBirthPicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(
                    year,
                    month,
                    day
                )

                selectedDateOfBirth = calendar.timeInMillis

                binding.dateOfBirthInputField.setText(
                    SimpleDateFormat(
                        DATE_FORMAT,
                        Locale.getDefault()
                    ).format(
                        calendar.time
                    )
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()

            show()
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

        registerPatientService.register(
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

    private fun readRegistrationForm(): PatientRegisterForm {
        return PatientRegisterForm(
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
            dateOfBirth = selectedDateOfBirth
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

    companion object {
        private const val DATE_FORMAT = "dd.MM.yyyy"
    }
}