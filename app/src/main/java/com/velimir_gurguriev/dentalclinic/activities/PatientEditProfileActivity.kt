package com.velimir_gurguriev.dentalclinic.activities

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.velimir_gurguriev.dentalclinic.databinding.ActivityPatientEditProfileBinding
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.PatientRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.utils.ui.SnackbarUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PatientEditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPatientEditProfileBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var userRepository: UserRepository
    private lateinit var patientRepository: PatientRepository

    private var selectedDateOfBirth: Long = 0L

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityPatientEditProfileBinding.inflate(
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

        patientRepository = PatientRepository()
    }

    private fun setupClickListeners() {
        binding.dateOfBirthInputField
            .setOnClickListener {
                showDateOfBirthPicker()
            }

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

        loadPatientProfile(
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

    private fun loadPatientProfile(
        userId: String
    ) {
        patientRepository.getPatientProfileById(
            uid = userId,
            onSuccess = { patientProfile ->

                selectedDateOfBirth =
                    patientProfile.dateOfBirth

                binding.dateOfBirthInputField.setText(
                    formatDateOfBirth(
                        selectedDateOfBirth
                    )
                )
            },
            onFailure = {
                showMessage(
                    "Информацията за пациента не може да бъде заредена."
                )
            }
        )
    }

    private fun showDateOfBirthPicker() {
        val calendar =
            Calendar.getInstance()

        if (selectedDateOfBirth > 0L) {
            calendar.timeInMillis =
                selectedDateOfBirth
        }

        DatePickerDialog(
            this,
            { _, year, month, day ->

                calendar.set(
                    year,
                    month,
                    day
                )

                selectedDateOfBirth =
                    calendar.timeInMillis

                binding.dateOfBirthInputField.setText(
                    formatDateOfBirth(
                        selectedDateOfBirth
                    )
                )
            },
            calendar.get(
                Calendar.YEAR
            ),
            calendar.get(
                Calendar.MONTH
            ),
            calendar.get(
                Calendar.DAY_OF_MONTH
            )
        ).apply {
            datePicker.maxDate =
                System.currentTimeMillis()

            show()
        }
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

        if (name.isEmpty()) {
            binding.nameInputField.error = "Въведете име."

            return
        }

        if (selectedDateOfBirth == 0L) {
            showMessage(
                "Изберете дата на раждане."
            )

            return
        }

        userRepository.updateUserProfile(
            uid = userId,
            name = name,
            phone = phone,
            city = city,
            onSuccess = {
                updatePatientProfile(
                    userId
                )
            },
            onFailure = {
                showMessage(
                    "Профилът не може да бъде обновен."
                )
            }
        )
    }

    private fun updatePatientProfile(
        userId: String
    ) {
        patientRepository.updatePatientProfile(
            uid = userId,
            dateOfBirth = selectedDateOfBirth,
            onSuccess = {
                showMessage(
                    "Профилът е обновен."
                )

                finish()
            },
            onFailure = {
                showMessage(
                    "Информацията за пациента не може да бъде обновена."
                )
            }
        )
    }

    private fun formatDateOfBirth(
        dateOfBirth: Long
    ): String {
        return SimpleDateFormat(
            DATE_FORMAT,
            Locale.getDefault()
        ).format(
            Date(dateOfBirth)
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

    companion object {
        private const val DATE_FORMAT = "dd.MM.yyyy"
    }
}