package com.velimir_gurguriev.dentalclinic.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.velimir_gurguriev.dentalclinic.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityWelcomeBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.toLoginButton
            .setOnClickListener {
                openLogin()
            }

        binding.toPatientRegisterButton
            .setOnClickListener {
                openPatientRegistration()
            }

        binding.toDentistRegisterButton
            .setOnClickListener {
                openDentistRegistration()
            }
    }

    private fun openLogin() {
        startActivity(
            Intent(
                this,
                LoginActivity::class.java
            )
        )
    }

    private fun openPatientRegistration() {
        startActivity(
            Intent(
                this,
                RegisterPatientActivity::class.java
            )
        )
    }

    private fun openDentistRegistration() {
        startActivity(
            Intent(
                this,
                RegisterDentistActivity::class.java
            )
        )
    }
}