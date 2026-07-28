package com.velimir_gurguriev.dentalclinic.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.velimir_gurguriev.dentalclinic.databinding.ActivityEditProfileBinding
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository()

        loadCurrentUser()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.saveButton.setOnClickListener {
            updateProfile()
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun loadCurrentUser() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            showMessage("No user logged in.")
            finish()
            return
        }

        userRepository.getCurrentUser(
            currentUser.uid,
            { user ->
                binding.nameInputField.setText(user.name)
                binding.emailInputField.setText(user.email)
            },
            {
                showMessage("Profile cannot be loaded.")
            }
        )
    }

    private fun updateProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            showMessage("No user logged in.")
            return
        }

        val name = binding.nameInputField.text.toString().trim()

        if (name.isEmpty()) {
            binding.nameInputField.error = "Въведете име."
            return
        }

        userRepository.updateUserName(
            currentUser.uid,
            name,
            {
                showMessage("Профилът е обновен.")
                finish()
            },
            {
                showMessage("Профилът не може да бъде обновен.")
            }
        )
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}