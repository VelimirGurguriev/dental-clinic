package com.velimir_gurguriev.dentalclinic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.velimir_gurguriev.dentalclinic.databinding.FragmentDentistDetailsBinding
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.services.DentistsService

class DentistDetailsFragment : Fragment() {

    private lateinit var binding: FragmentDentistDetailsBinding

    private lateinit var dentistsService: DentistsService

    private lateinit var dentistUid: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDentistDetailsBinding.inflate(
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
        readArguments()
        setupClickListeners()
        loadDentist()
    }

    private fun initializeDependencies() {
        val userRepository = UserRepository()
        dentistsService = DentistsService(userRepository)
    }

    private fun readArguments() {
        dentistUid = requireArguments().getString("dentistUid")
            ?: throw IllegalArgumentException("Dentist UID is missing.")
    }

    private fun loadDentist() {
        dentistsService.getDentistById(
            dentistUid,
            { dentist ->
                binding.dentistNameTextView.text = dentist.name
                binding.dentistEmailTextView.text = dentist.email
                binding.dentistRoleTextView.text = dentist.accountType
            },
            {
                showMessage("Неуспешно зареждане на стоматолога.")
            }
        )
    }

    private fun setupClickListeners() {
        binding.requestPatientButton.setOnClickListener {
            showMessage("work in progress")
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}