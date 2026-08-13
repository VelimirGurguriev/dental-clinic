package com.velimir_gurguriev.dentalclinic.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.velimir_gurguriev.dentalclinic.R
import com.velimir_gurguriev.dentalclinic.databinding.ActivityMainBinding
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.utils.ui.SnackbarUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var authRepository: AuthRepository
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityMainBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        initializeDependencies()
        initializeNavigation()
        loadBottomNavigationMenu()
    }

    private fun initializeDependencies() {
        authRepository = AuthRepository()

        userRepository = UserRepository()
    }

    private fun initializeNavigation() {
        val navHostFragment =
            supportFragmentManager
                .findFragmentById(
                    R.id.navHostFragment
                ) as NavHostFragment

        navController = navHostFragment.navController
    }

    private fun loadBottomNavigationMenu() {
        val currentUserId =
            getCurrentUserId()
                ?: return

        userRepository.getUserById(
            uid = currentUserId,
            onSuccess = { user ->
                setupBottomNavigation(
                    user.accountType
                )
            },
            onFailure = {
                showMessage(
                    "Неуспешно зареждане на потребителския профил."
                )
            }
        )
    }

    private fun setupBottomNavigation(
        accountType: String
    ) {
        binding.bottomNavigation.menu.clear()

        val menuResource =
            if (isDentist(accountType)) {
                R.menu.bottom_navigation_dentist_menu
            } else {
                R.menu.bottom_navigation_patient_menu
            }

        binding.bottomNavigation.inflateMenu(
            menuResource
        )

        binding.bottomNavigation
            .setupWithNavController(
                navController
            )

        setupDestinationListener()
    }

    private fun isDentist(
        accountType: String
    ): Boolean {
        return accountType.equals(
            "dentist",
            ignoreCase = true
        )
    }

    private fun setupDestinationListener() {
        navController
            .addOnDestinationChangedListener {
                    _,
                    destination,
                    _ ->

                if (
                    destination.id ==
                    R.id.dentistDetailsFragment
                ) {
                    binding.bottomNavigation.menu
                        .findItem(
                            R.id.dentistFragment
                        )
                        ?.isChecked = true
                }
            }
    }

    private fun getCurrentUserId(): String? {
        val userId = authRepository.getCurrentUserId()

        if (userId == null) {
            showMessage(
                "Потребителят не е вписан."
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
}