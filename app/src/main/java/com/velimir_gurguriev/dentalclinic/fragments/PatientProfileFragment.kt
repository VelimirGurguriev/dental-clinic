package com.velimir_gurguriev.dentalclinic.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.velimir_gurguriev.dentalclinic.activities.PatientEditProfileActivity
import com.velimir_gurguriev.dentalclinic.activities.WelcomeActivity
import com.velimir_gurguriev.dentalclinic.databinding.FragmentPatientProfileBinding
import com.velimir_gurguriev.dentalclinic.models.User
import com.velimir_gurguriev.dentalclinic.models.patient.PatientProfile
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.PatientRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.services.users.UserProfileService
import com.velimir_gurguriev.dentalclinic.utils.ui.SnackbarUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PatientProfileFragment : Fragment() {

    private var _binding: FragmentPatientProfileBinding? = null
    private val binding: FragmentPatientProfileBinding get() = _binding!!
    private lateinit var userProfileService: UserProfileService
    private lateinit var patientRepository: PatientRepository
    private lateinit var authRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentPatientProfileBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        initializeDependencies()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()

        if (
            ::userProfileService.isInitialized && ::patientRepository.isInitialized
        ) {
            loadProfile()
        }
    }

    override fun onDestroyView() {
        _binding = null

        super.onDestroyView()
    }

    private fun initializeDependencies() {
        authRepository = AuthRepository()

        userProfileService =
            UserProfileService(
                repository = UserRepository(),
                authRepository = authRepository
            )

        patientRepository = PatientRepository()
    }

    private fun setupClickListeners() {
        binding.editProfileButton
            .setOnClickListener {
                openEditProfile()
            }

        binding.logoutButton
            .setOnClickListener {
                logout()
            }
    }

    private fun loadProfile() {
        val currentUserId = getCurrentUserId() ?: return

        loadUser(
            currentUserId
        )

        loadPatientProfile(
            currentUserId
        )
    }

    private fun loadUser(
        userId: String
    ) {
        userProfileService.loadCurrentUser(
            userId,
            onSuccess = { user ->
                showUser(
                    user
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
                showPatientProfile(
                    patientProfile
                )
            },
            onFailure = {
                showMessage(
                    "Информацията за пациента не може да бъде заредена."
                )
            }
        )
    }

    private fun showUser(
        user: User
    ) {
        val currentBinding = _binding ?: return

        currentBinding.nameTextView.text = user.name

        currentBinding.emailTextView.text = user.email

        currentBinding.phoneTextView.text = user.phone

        currentBinding.cityTextView.text = user.city
    }

    private fun showPatientProfile(
        patientProfile: PatientProfile
    ) {
        val currentBinding = _binding ?: return

        currentBinding.dateOfBirthTextView.text =
            formatDateOfBirth(
                patientProfile.dateOfBirth
            )
    }

    private fun formatDateOfBirth(
        dateOfBirth: Long
    ): String {
        val formatter =
            SimpleDateFormat(
                DATE_FORMAT,
                Locale.getDefault()
            )

        return formatter.format(
            Date(dateOfBirth)
        )
    }

    private fun openEditProfile() {
        val intent =
            Intent(
                requireContext(),
                PatientEditProfileActivity::class.java
            )

        startActivity(
            intent
        )
    }

    private fun logout() {
        userProfileService.logout()

        val intent =
            Intent(
                requireContext(),
                WelcomeActivity::class.java
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(
            intent
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
        val currentBinding = _binding ?: return

        SnackbarUtils.show(
            rootView = currentBinding.root,
            message = message
        )
    }

    companion object {
        private const val DATE_FORMAT = "dd.MM.yyyy"
    }
}