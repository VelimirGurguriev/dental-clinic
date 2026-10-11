package com.velimir_gurguriev.dentalclinic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.velimir_gurguriev.dentalclinic.adapters.DentistAppointmentRequestAdapter
import com.velimir_gurguriev.dentalclinic.databinding.FragmentDentistAppointmentRequestsBinding
import com.velimir_gurguriev.dentalclinic.models.appointments.AppointmentSlot
import com.velimir_gurguriev.dentalclinic.repositories.AppointmentRepository
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.services.appointments.AppointmentService
import com.velimir_gurguriev.dentalclinic.utils.ui.SnackbarUtils
import com.google.android.gms.tasks.Tasks
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository

class DentistAppointmentRequestsFragment : Fragment() {

    private var _binding: FragmentDentistAppointmentRequestsBinding? = null
    private val binding get() = _binding!!

    private lateinit var userRepository: UserRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var appointmentService: AppointmentService
    private lateinit var requestAdapter: DentistAppointmentRequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDentistAppointmentRequestsBinding.inflate(
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

        userRepository = UserRepository()
        authRepository = AuthRepository()
        appointmentService = AppointmentService(AppointmentRepository())

        requestAdapter = DentistAppointmentRequestAdapter(
            onApproveClick = { appointment ->
                processRequest(appointment, approve = true)
            },
            onRejectClick = { appointment ->
                processRequest(appointment, approve = false)
            }
        )

        binding.appointmentRequestsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = requestAdapter
        }

        loadRequests()
    }

    private fun loadRequests() {
        val dentistId = authRepository.getCurrentUserId() ?: return

        appointmentService.getPendingAppointmentsForDentist(dentistId)
            .addOnSuccessListener { appointments ->
                if (_binding == null) return@addOnSuccessListener

                val patientIds = appointments
                    .mapNotNull { it.patientId }
                    .distinct()

                if (patientIds.isEmpty()) {
                    displayRequests(appointments, emptyMap())
                    return@addOnSuccessListener
                }

                val patientNames = mutableMapOf<String, String>()
                var completedRequests = 0

                patientIds.forEach { patientId ->
                    userRepository.getUserById(
                        uid = patientId,
                        onSuccess = { user ->
                            patientNames[patientId] = user.name
                            completedRequests++

                            if (completedRequests == patientIds.size) {
                                displayRequests(appointments, patientNames)
                            }
                        },
                        onFailure = {
                            patientNames[patientId] = "Неизвестен пациент"
                            completedRequests++

                            if (completedRequests == patientIds.size) {
                                displayRequests(appointments, patientNames)
                            }
                        }
                    )
                }
            }
            .addOnFailureListener { exception ->
                showMessage(
                    exception.message ?: "Неуспешно зареждане на заявките."
                )
            }
    }

    private fun displayRequests(
        appointments: List<AppointmentSlot>,
        patientNames: Map<String, String>
    ) {
        val currentBinding = _binding ?: return

        requestAdapter.updateAppointments(appointments, patientNames)

        currentBinding.emptyAppointmentRequestsTextView.visibility =
            if (appointments.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun processRequest(
        appointment: AppointmentSlot,
        approve: Boolean
    ) {
        val dentistId = authRepository.getCurrentUserId() ?: return

        val task = if (approve) {
            appointmentService.approveAppointment(
                appointment.id,
                dentistId
            )
        } else {
            appointmentService.rejectAppointment(
                appointment.id,
                dentistId
            )
        }

        task.addOnSuccessListener {
            if (_binding == null) return@addOnSuccessListener

            showMessage(
                if (approve) "Часът е одобрен."
                else "Заявката е отхвърлена."
            )

            loadRequests()
        }.addOnFailureListener { exception ->
            showMessage(
                exception.message ?: "Неуспешна обработка на заявката."
            )
        }
    }

    private fun showMessage(message: String) {
        val currentBinding = _binding ?: return

        SnackbarUtils.show(
            rootView = currentBinding.root,
            message = message
        )
    }

    override fun onDestroyView() {
        binding.appointmentRequestsRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
