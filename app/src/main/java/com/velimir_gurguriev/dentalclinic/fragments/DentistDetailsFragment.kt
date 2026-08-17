package com.velimir_gurguriev.dentalclinic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.velimir_gurguriev.dentalclinic.databinding.FragmentDentistDetailsBinding
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.DentistPatientConnectionRepository
import com.velimir_gurguriev.dentalclinic.repositories.DentistRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.services.dentists.DentistService
import com.velimir_gurguriev.dentalclinic.services.connections.DentistPatientConnectionService
import com.velimir_gurguriev.dentalclinic.utils.ui.SnackbarUtils

class DentistDetailsFragment : Fragment() {

    private var _binding: FragmentDentistDetailsBinding? = null
    private val binding: FragmentDentistDetailsBinding get() = _binding!!
    private lateinit var dentistService: DentistService
    private lateinit var connectionService: DentistPatientConnectionService
    private lateinit var authRepository: AuthRepository
    private lateinit var dentistRepository: DentistRepository
    private lateinit var dentistUid: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
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

    override fun onDestroyView() {
        _binding = null

        super.onDestroyView()
    }

    private fun initializeDependencies() {
        authRepository = AuthRepository()

        val userRepository = UserRepository()

        dentistRepository = DentistRepository()

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
        loadDentistUser()
        loadDentistProfile()
    }

    private fun loadDentistUser() {
        dentistService.getDentistById(
            uid = dentistUid,
            onSuccess = { dentist ->
                val currentBinding =
                    _binding
                        ?: return@getDentistById

                currentBinding.dentistNameTextView.text = dentist.name

                currentBinding.dentistEmailTextView.text = dentist.email

                currentBinding.dentistPhoneTextView.text = dentist.phone

                currentBinding.dentistCityTextView.text = dentist.city
            },
            onFailure = {
                showMessage(
                    "Неуспешно зареждане на стоматолога."
                )
            }
        )
    }

    private fun loadDentistProfile() {
        dentistRepository.getDentistProfileById(
            uid = dentistUid,
            onSuccess = { dentistProfile ->
                val currentBinding =
                    _binding
                        ?: return@getDentistProfileById

                currentBinding.dentistSpecializationTextView.text = dentistProfile.specialization

                currentBinding.dentistClinicNameTextView.text = dentistProfile.clinicName

                currentBinding.dentistClinicAddressTextView.text = dentistProfile.clinicAddress
            },
            onFailure = {
                showMessage(
                    "Професионалната информация не може да бъде заредена."
                )
            }
        )
    }

    private fun sendPatientRequest() {
        val patientId = getCurrentPatientId() ?: return

        connectionService
            .sendConnectionRequest(
                patientId = patientId,
                dentistId = dentistUid
            )
            .addOnSuccessListener {
                val currentBinding = _binding ?: return@addOnSuccessListener

                showMessage(
                    "Заявката е изпратена успешно."
                )

                currentBinding.requestPatientButton.isEnabled = false

                currentBinding.requestPatientButton.text =
                    "Заявката е изпратена"
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
        val currentBinding = _binding ?: return

        SnackbarUtils.show(
            rootView = currentBinding.root,
            message = message
        )
    }

    companion object {
        private const val DENTIST_UID_ARGUMENT = "dentistUid"
    }
}