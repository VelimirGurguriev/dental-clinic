package com.velimir_gurguriev.dentalclinic.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.velimir_gurguriev.dentalclinic.activities.EditProfileActivity
import com.velimir_gurguriev.dentalclinic.activities.WelcomeActivity
import com.velimir_gurguriev.dentalclinic.databinding.FragmentUserProfileBinding
import com.velimir_gurguriev.dentalclinic.models.User
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.services.UserProfileService

class UserProfileFragment : Fragment() {

    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var userProfileService: UserProfileService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentUserProfileBinding.inflate(
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
        super.onViewCreated(view, savedInstanceState)

        initializeDependencies()
        setupClickListeners()
        loadUserProfile()
    }

    override fun onResume() {
        super.onResume()

        if (::userProfileService.isInitialized) {
            loadUserProfile()
        }
    }

    private fun initializeDependencies() {

        val userRepository = UserRepository()
        val authRepository = AuthRepository()

        userProfileService = UserProfileService(
            userRepository,
            authRepository
        )
    }

    private fun setupClickListeners() {

        binding.editProfileButton.setOnClickListener {
            openEditProfile()
        }

        binding.logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun openEditProfile() {

        val intent = Intent(
            requireContext(),
            EditProfileActivity::class.java
        )

        startActivity(intent)
    }

    private fun loadUserProfile() {

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            showMessage("No user logged in.")
            return
        }

        userProfileService.loadCurrentUser(
            currentUser.uid,
            { user ->
                showUser(user)
            },
            {
                showMessage("Profile cannot be loaded.")
            }
        )
    }

    private fun logout() {

        userProfileService.logout()

        val intent = Intent(
            requireContext(),
            WelcomeActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
    }

    private fun showUser(user: User) {

        binding.usernameTextView.text = user.name
        binding.emailTextView.text = user.email
        binding.roleTextView.text = user.accountType
    }

    private fun showMessage(message: String) {

        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}