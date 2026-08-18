package com.velimir_gurguriev.dentalclinic.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.velimir_gurguriev.dentalclinic.activities.DentistEditProfileActivity
import com.velimir_gurguriev.dentalclinic.activities.WelcomeActivity
import com.velimir_gurguriev.dentalclinic.databinding.FragmentDentistProfileBinding
import com.velimir_gurguriev.dentalclinic.models.User
import com.velimir_gurguriev.dentalclinic.models.dentist.DentistProfile
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.DentistRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.services.users.UserProfileService
import com.velimir_gurguriev.dentalclinic.utils.ui.SnackbarUtils

class DentistProfileFragment : Fragment() {

    private var _binding: FragmentDentistProfileBinding? = null
    private val binding: FragmentDentistProfileBinding get() = _binding!!
    private lateinit var userProfileService: UserProfileService
    private lateinit var dentistRepository: DentistRepository
    private lateinit var authRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentDentistProfileBinding.inflate(
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
            ::userProfileService.isInitialized && ::dentistRepository.isInitialized
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

        dentistRepository = DentistRepository()
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

        loadDentistProfile(
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

    private fun loadDentistProfile(
        userId: String
    ) {
        dentistRepository.getDentistProfileById(
            uid = userId,
            onSuccess = { dentistProfile ->
                showDentistProfile(
                    dentistProfile
                )
            },
            onFailure = {
                showMessage(
                    "Професионалната информация не може да бъде заредена."
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

    private fun showDentistProfile(
        dentistProfile: DentistProfile
    ) {
        val currentBinding = _binding ?: return

        currentBinding.specializationTextView.text = dentistProfile.specialization

        currentBinding.clinicNameTextView.text = dentistProfile.clinicName

        currentBinding.clinicAddressTextView.text = dentistProfile.clinicAddress
    }

    private fun openEditProfile() {
        val intent =
            Intent(
                requireContext(),
                DentistEditProfileActivity::class.java
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
}