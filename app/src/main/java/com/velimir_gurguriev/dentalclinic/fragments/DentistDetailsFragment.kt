package com.velimir_gurguriev.dentalclinic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.velimir_gurguriev.dentalclinic.databinding.FragmentDentistDetailsBinding
import com.velimir_gurguriev.dentalclinic.repositories.DentistPatientConnectionRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.services.DentistsService
import com.velimir_gurguriev.dentalclinic.services.connections.DentistPatientConnectionService

class DentistDetailsFragment : Fragment() {

    private lateinit var binding: FragmentDentistDetailsBinding

    private lateinit var dentistsService: DentistsService

    private lateinit var connectionService: DentistPatientConnectionService

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

        val connectionRepository =
            DentistPatientConnectionRepository()

        connectionService =
            DentistPatientConnectionService(
                connectionRepository
            )
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
            sendPatientRequest()
        }
    }

    private fun sendPatientRequest() {

        val patientId = FirebaseAuth.getInstance()
            .currentUser
            ?.uid

        if (patientId == null) {
            showMessage("Потребителят не е вписан.")
            return
        }

        connectionService
            .sendConnectionRequest(
                patientId = patientId,
                dentistId = dentistUid
            )
            .addOnSuccessListener {
                showMessage("Заявката е изпратена успешно.")

                binding.requestPatientButton.isEnabled = false
                binding.requestPatientButton.text = "Заявката е изпратена"
            }
            .addOnFailureListener { exception ->
                showMessage(
                    exception.message
                        ?: "Възникна грешка при изпращане на заявката."
                )
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