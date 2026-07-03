package com.velimir_gurguriev.dentalclinic.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.velimir_gurguriev.dentalclinic.databinding.ActivityLoginBinding
import com.velimir_gurguriev.dentalclinic.models.forms.LoginForm
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.services.users.LoginUserService
import com.velimir_gurguriev.dentalclinic.validators.login.LoginFormValidator

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginFormValidator: LoginFormValidator
    private lateinit var loginUserService: LoginUserService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeDependencies()
        setupClickListeners()
    }

    private fun initializeDependencies() {
        loginFormValidator = LoginFormValidator()
        loginUserService = LoginUserService(AuthRepository())
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            handleLogin()
        }
    }

    private fun handleLogin() {
        val form = LoginForm(
            email = binding.emailInputField.text.toString().trim(),
            password = binding.passwordInputField.text.toString()
        )

        val validationResult = loginFormValidator.validate(form)
        if (!validationResult.isValid) {
            showMessage(validationResult.message)
            return
        }

        loginUserService.login(
            form = form,
            onSuccess = {
                showMessage("Успешен вход.")
                startActivity(Intent(this, HomeScreen::class.java))
                finish()
            },
            onFailure = { exception ->
                showMessage("Грешка: ${exception.message}")
            }
        )
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
