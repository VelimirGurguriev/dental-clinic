package com.velimir_gurguriev.dentalclinic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.velimir_gurguriev.dentalclinic.adapters.DentistPatientAdapter
import com.velimir_gurguriev.dentalclinic.adapters.PatientRequestAdapter
import com.velimir_gurguriev.dentalclinic.databinding.FragmentPatientRequestsBinding
import com.velimir_gurguriev.dentalclinic.models.connections.DentistPatientItem
import com.velimir_gurguriev.dentalclinic.models.connections.PatientRequestItem
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.DentistPatientConnectionRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.services.connections.DentistPatientConnectionService
import com.velimir_gurguriev.dentalclinic.utils.ui.SnackbarUtils

class DentistPatientsFragment : Fragment() {

    private lateinit var binding: FragmentPatientRequestsBinding

    private lateinit var connectionService: DentistPatientConnectionService

    private lateinit var authRepository: AuthRepository

    private lateinit var patientRequestAdapter: PatientRequestAdapter

    private lateinit var dentistPatientAdapter: DentistPatientAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            FragmentPatientRequestsBinding.inflate(
                inflater,
                container,
                false
            )

        initializeDependencies()
        setupRecyclerViews()
        loadPendingRequests()
        loadApprovedPatients()

        return binding.root
    }

    private fun initializeDependencies() {
        authRepository = AuthRepository()

        connectionService =
            DentistPatientConnectionService(
                connectionRepository =
                    DentistPatientConnectionRepository(),
                userRepository =
                    UserRepository()
            )
    }

    private fun setupRecyclerViews() {
        setupPendingRequestsRecyclerView()
        setupApprovedPatientsRecyclerView()
    }

    private fun setupPendingRequestsRecyclerView() {
        patientRequestAdapter =
            PatientRequestAdapter(
                requests = mutableListOf(),
                onApproveClick = { request ->
                    approveRequest(request)
                },
                onRejectClick = { request ->
                    rejectRequest(request)
                }
            )

        binding.patientRequestsRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                patientRequestAdapter
        }
    }

    private fun setupApprovedPatientsRecyclerView() {
        dentistPatientAdapter =
            DentistPatientAdapter(
                patients = mutableListOf(),
                onViewClick = { patient ->
                    showMessage(
                        "Преглед на ${patient.patient.name}"
                    )
                }
            )

        binding.approvedPatientsRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                dentistPatientAdapter
        }
    }

    private fun loadPendingRequests() {
        val dentistId =
            getCurrentDentistId()
                ?: return

        connectionService
            .getPendingRequestsForDentist(
                dentistId
            )
            .addOnSuccessListener { requests ->

                patientRequestAdapter.updateRequests(
                    requests
                )

                if (requests.isEmpty()) {
                    showMessage(
                        "Няма чакащи заявки."
                    )
                }
            }
            .addOnFailureListener { exception ->
                showMessage(
                    exception.message
                        ?: "Заявките не можаха да бъдат заредени."
                )
            }
    }

    private fun loadApprovedPatients() {
        val dentistId =
            getCurrentDentistId()
                ?: return

        connectionService
            .getApprovedPatientsForDentist(
                dentistId
            )
            .addOnSuccessListener { patients ->

                dentistPatientAdapter.updatePatients(
                    patients
                )
            }
            .addOnFailureListener { exception ->
                showMessage(
                    exception.message
                        ?: "Пациентите не можаха да бъдат заредени."
                )
            }
    }

    private fun approveRequest(
        request: PatientRequestItem
    ) {
        connectionService
            .approveRequest(
                request.connectionId
            )
            .addOnSuccessListener {

                patientRequestAdapter.removeRequest(
                    request
                )

                dentistPatientAdapter.addPatient(
                    DentistPatientItem(
                        connectionId =
                            request.connectionId,
                        patient =
                            request.patient
                    )
                )

                showMessage(
                    "Заявката е одобрена."
                )
            }
            .addOnFailureListener { exception ->
                showMessage(
                    exception.message
                        ?: "Заявката не можа да бъде одобрена."
                )
            }
    }

    private fun rejectRequest(
        request: PatientRequestItem
    ) {
        connectionService
            .rejectRequest(
                request.connectionId
            )
            .addOnSuccessListener {

                patientRequestAdapter.removeRequest(
                    request
                )

                showMessage(
                    "Заявката е отхвърлена."
                )
            }
            .addOnFailureListener { exception ->
                showMessage(
                    exception.message
                        ?: "Заявката не можа да бъде отхвърлена."
                )
            }
    }

    private fun getCurrentDentistId(): String? {
        val dentistId =
            authRepository.getCurrentUserId()

        if (dentistId == null) {
            showMessage(
                "Няма влязъл потребител."
            )
        }

        return dentistId
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