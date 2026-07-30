package com.velimir_gurguriev.dentalclinic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.velimir_gurguriev.dentalclinic.adapters.DentistPatientAdapter
import com.velimir_gurguriev.dentalclinic.adapters.PatientRequestAdapter
import com.velimir_gurguriev.dentalclinic.databinding.FragmentPatientRequestsBinding
import com.velimir_gurguriev.dentalclinic.models.connections.DentistPatientItem
import com.velimir_gurguriev.dentalclinic.models.connections.PatientRequestItem
import com.velimir_gurguriev.dentalclinic.repositories.DentistPatientConnectionRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.services.connections.DentistPatientConnectionService

class PatientRequestsFragment : Fragment() {

    private lateinit var binding: FragmentPatientRequestsBinding

    private lateinit var connectionService:
            DentistPatientConnectionService

    private lateinit var userRepository: UserRepository

    private lateinit var patientRequestAdapter:
            PatientRequestAdapter

    private lateinit var dentistPatientAdapter:
            DentistPatientAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPatientRequestsBinding.inflate(
            inflater,
            container,
            false
        )

        initializeDependencies()
        setupRecyclerView()
        loadPendingRequests()
        loadApprovedPatients()

        return binding.root
    }

    private fun initializeDependencies() {
        val connectionRepository =
            DentistPatientConnectionRepository()

        connectionService =
            DentistPatientConnectionService(
                connectionRepository
            )

        userRepository = UserRepository()
    }

    private fun setupRecyclerView() {
        patientRequestAdapter = PatientRequestAdapter(
            requests = mutableListOf(),
            onApproveClick = { request ->
                approveRequest(request)
            },
            onRejectClick = { request ->
                rejectRequest(request)
            }
        )

        binding.patientRequestsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = patientRequestAdapter
        }

        dentistPatientAdapter = DentistPatientAdapter(
            patients = mutableListOf(),
            onViewClick = { patient ->
                showMessage(
                    "Преглед на ${patient.patient.name}"
                )
            }
        )

        binding.approvedPatientsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dentistPatientAdapter
        }
    }

    private fun loadApprovedPatients() {
        val dentistId =
            FirebaseAuth.getInstance().currentUser?.uid

        if (dentistId == null) {
            showMessage("Няма влязъл потребител.")
            return
        }

        connectionService
            .getApprovedPatientsForDentist(dentistId)
            .addOnSuccessListener { querySnapshot ->

                if (querySnapshot.isEmpty) {
                    dentistPatientAdapter.updatePatients(
                        emptyList()
                    )
                    return@addOnSuccessListener
                }

                val patientItems =
                    mutableListOf<DentistPatientItem>()

                var completedPatients = 0
                val totalPatients =
                    querySnapshot.documents.size

                querySnapshot.documents.forEach { document ->

                    val patientId =
                        document.getString("patientId")

                    if (patientId.isNullOrBlank()) {
                        completedPatients++

                        updateApprovedPatientsWhenFinished(
                            completedPatients,
                            totalPatients,
                            patientItems
                        )

                        return@forEach
                    }

                    userRepository.getUserById(
                        uid = patientId,
                        onSuccess = { patient ->

                            if (patient != null) {
                                patientItems.add(
                                    DentistPatientItem(
                                        connectionId = document.id,
                                        patient = patient
                                    )
                                )
                            }

                            completedPatients++

                            updateApprovedPatientsWhenFinished(
                                completedPatients,
                                totalPatients,
                                patientItems
                            )
                        },
                        onFailure = {
                            completedPatients++

                            updateApprovedPatientsWhenFinished(
                                completedPatients,
                                totalPatients,
                                patientItems
                            )
                        }
                    )
                }
            }
            .addOnFailureListener { exception ->
                showMessage(
                    exception.message
                        ?: "Пациентите не можаха да бъдат заредени."
                )
            }
    }

    private fun updateApprovedPatientsWhenFinished(
        completedPatients: Int,
        totalPatients: Int,
        patientItems: List<DentistPatientItem>
    ) {
        if (completedPatients != totalPatients) {
            return
        }

        dentistPatientAdapter.updatePatients(
            patientItems
        )
    }

    private fun loadPendingRequests() {
        val dentistId =
            FirebaseAuth.getInstance().currentUser?.uid

        if (dentistId == null) {
            showMessage("Няма влязъл потребител.")
            return
        }

        connectionService
            .getPendingRequestsForDentist(dentistId)
            .addOnSuccessListener { querySnapshot ->

                if (querySnapshot.isEmpty) {
                    patientRequestAdapter.updateRequests(
                        emptyList()
                    )

                    showMessage("Няма чакащи заявки.")
                    return@addOnSuccessListener
                }

                val requestItems =
                    mutableListOf<PatientRequestItem>()

                var completedRequests = 0
                val totalRequests =
                    querySnapshot.documents.size

                querySnapshot.documents.forEach { document ->

                    val patientId =
                        document.getString("patientId")

                    if (patientId.isNullOrBlank()) {
                        completedRequests++

                        updateAdapterWhenFinished(
                            completedRequests,
                            totalRequests,
                            requestItems
                        )

                        return@forEach
                    }

                    userRepository.getUserById(
                        uid = patientId,
                        onSuccess = { patient ->

                            if (patient != null) {
                                requestItems.add(
                                    PatientRequestItem(
                                        connectionId = document.id,
                                        patient = patient
                                    )
                                )
                            }

                            completedRequests++

                            updateAdapterWhenFinished(
                                completedRequests,
                                totalRequests,
                                requestItems
                            )
                        },
                        onFailure = {

                            completedRequests++

                            updateAdapterWhenFinished(
                                completedRequests,
                                totalRequests,
                                requestItems
                            )
                        }
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

    private fun updateAdapterWhenFinished(
        completedRequests: Int,
        totalRequests: Int,
        requestItems: List<PatientRequestItem>
    ) {
        if (completedRequests != totalRequests) {
            return
        }

        patientRequestAdapter.updateRequests(
            requestItems
        )
    }

    private fun approveRequest(
        request: PatientRequestItem
    ) {
        connectionService
            .approveRequest(request.connectionId)
            .addOnSuccessListener {

                patientRequestAdapter.removeRequest(
                    request
                )

                dentistPatientAdapter.addPatient(
                    DentistPatientItem(
                        connectionId = request.connectionId,
                        patient = request.patient
                    )
                )

                showMessage("Заявката е одобрена.")
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
            .rejectRequest(request.connectionId)
            .addOnSuccessListener {
                patientRequestAdapter.removeRequest(
                    request
                )

                showMessage("Заявката е отхвърлена.")
            }
            .addOnFailureListener { exception ->
                showMessage(
                    exception.message
                        ?: "Заявката не можа да бъде отхвърлена."
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