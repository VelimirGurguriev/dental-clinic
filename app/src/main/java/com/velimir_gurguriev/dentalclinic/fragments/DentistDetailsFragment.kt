package com.velimir_gurguriev.dentalclinic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.velimir_gurguriev.dentalclinic.databinding.FragmentDentistDetailsBinding
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.DentistPatientConnectionRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.services.dentists.DentistService
import com.velimir_gurguriev.dentalclinic.services.connections.DentistPatientConnectionService
import com.velimir_gurguriev.dentalclinic.utils.ui.SnackbarUtils

class DentistDetailsFragment : Fragment() {

    private lateinit var binding: FragmentDentistDetailsBinding

    private lateinit var dentistService: DentistService

    private lateinit var connectionService: DentistPatientConnectionService

    private lateinit var authRepository: AuthRepository

    private lateinit var dentistUid: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentDentistDetailsBinding.inflate(
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
        readArguments()
        setupClickListeners()
        loadDentist()
    }

    private fun initializeDependencies() {
        authRepository = AuthRepository()

        val userRepository = UserRepository()

        dentistService =
            DentistService(
                userRepository
            )

        connectionService =
            DentistPatientConnectionService(
                connectionRepository = DentistPatientConnectionRepository(),
                userRepository = userRepository
            )
    }

    private fun readArguments() {
        dentistUid =
            requireArguments()
                .getString(
                    DENTIST_UID_ARGUMENT
                )
                ?: throw IllegalArgumentException(
                    "Липсва ID на стоматолога."
                )
    }

    private fun setupClickListeners() {
        binding.requestPatientButton
            .setOnClickListener {
                sendPatientRequest()
            }
    }

    private fun loadDentist() {
        dentistService.getDentistById(
            uid = dentistUid,
            onSuccess = { dentist ->

                binding.dentistNameTextView.text = dentist.name

                binding.dentistEmailTextView.text = dentist.email

                binding.dentistRoleTextView.text = dentist.accountType
            },
            onFailure = {
                showMessage(
                    "Неуспешно зареждане на стоматолога."
                )
            }
        )
    }

    private fun sendPatientRequest() {
        val patientId =
            getCurrentPatientId()
                ?: return

        connectionService
            .sendConnectionRequest(
                patientId = patientId,
                dentistId = dentistUid
            )
            .addOnSuccessListener {

                showMessage(
                    "Заявката е изпратена успешно."
                )

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

    private fun getCurrentPatientId(): String? {
        val patientId =
            authRepository.getCurrentUserId()

        if (patientId == null) {
            showMessage(
                "Потребителят не е вписан."
            )
        }

        return patientId
    }

    private fun showMessage(
        message: String
    ) {
        SnackbarUtils.show(
            rootView = binding.root,
            message = message
        )
    }

    companion object {
        private const val DENTIST_UID_ARGUMENT =
            "dentistUid"
    }
}