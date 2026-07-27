package com.velimir_gurguriev.dentalclinic.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.velimir_gurguriev.dentalclinic.models.User
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.services.UserProfileService
import com.velimir_gurguriev.dentalclinic.databinding.FragmentUserProfileBinding

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
        loadUserProfile()
    }

    private fun initializeDependencies() {
        val userRepository = UserRepository()
        userProfileService = UserProfileService(userRepository)
    }

    private fun loadUserProfile() {

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            showMessage("No logged-in user.")
            return
        }

        userProfileService.loadCurrentUser(
            currentUser.uid,
            { user ->
                showUser(user)
            },
            {
                showMessage("Failed to load profile.")
            }
        )
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