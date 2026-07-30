package com.velimir_gurguriev.dentalclinic

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.velimir_gurguriev.dentalclinic.databinding.ActivityMainBinding
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeDependencies()
        initializeNavigation()
        loadBottomNavigationMenu()
    }

    private fun initializeDependencies() {
        userRepository = UserRepository()
    }

    private fun initializeNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment

        navController = navHostFragment.navController
    }

    private fun loadBottomNavigationMenu() {
        val currentUserId = FirebaseAuth.getInstance()
            .currentUser
            ?.uid

        if (currentUserId == null) {
            showMessage("Потребителят не е вписан.")
            return
        }

        userRepository.getUserById(
            uid = currentUserId,
            onSuccess = { user ->
                setupBottomNavigation(user.accountType)
            },
            onFailure = {
                showMessage("Неуспешно зареждане на потребителския профил.")
            }
        )
    }

    private fun setupBottomNavigation(accountType: String) {
        binding.bottomNavigation.menu.clear()

        if (isDentist(accountType)) {
            binding.bottomNavigation.inflateMenu(
                R.menu.bottom_navigation_dentist_menu
            )
        } else {
            binding.bottomNavigation.inflateMenu(
                R.menu.bottom_navigation_patient_menu
            )
        }

        binding.bottomNavigation.setupWithNavController(navController)

        setupDestinationListener()
    }

    private fun isDentist(accountType: String): Boolean {
        return accountType.equals("dentist", ignoreCase = true) ||
                accountType.equals("стоматолог", ignoreCase = true)
    }

    private fun setupDestinationListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->

            if (destination.id == R.id.dentistDetailsFragment) {
                binding.bottomNavigation.menu
                    .findItem(R.id.dentistFragment)
                    ?.isChecked = true
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}