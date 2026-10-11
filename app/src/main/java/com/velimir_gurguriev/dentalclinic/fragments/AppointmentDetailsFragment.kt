package com.velimir_gurguriev.dentalclinic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.velimir_gurguriev.dentalclinic.databinding.FragmentAppointmentDetailsBinding
import com.velimir_gurguriev.dentalclinic.models.appointments.AppointmentSlot
import com.velimir_gurguriev.dentalclinic.repositories.AppointmentRepository
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.services.appointments.AppointmentService
import com.velimir_gurguriev.dentalclinic.utils.ui.SnackbarUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AppointmentDetailsFragment : Fragment() {

    private var _binding: FragmentAppointmentDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var appointmentService: AppointmentService
    private lateinit var userRepository: UserRepository
    private lateinit var authRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentDetailsBinding.inflate(
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

        binding.appointmentDetailsContent.visibility = View.GONE

        val appointmentId = arguments?.getString(ARG_APPOINTMENT_ID)

        if (appointmentId.isNullOrBlank()) {
            showMessage("Липсва идентификатор на часа.")
            return
        }

        loadAppointment(appointmentId)
    }

    private fun initializeDependencies() {
        appointmentService = AppointmentService(
            AppointmentRepository()
        )

        userRepository = UserRepository()
        authRepository = AuthRepository()
    }

    private fun loadAppointment(appointmentId: String) {
        binding.appointmentDetailsProgressBar.visibility = View.VISIBLE

        appointmentService.getAppointmentById(appointmentId)
            .addOnSuccessListener { appointment ->
                if (_binding == null) return@addOnSuccessListener

                val currentUserId = authRepository.getCurrentUserId()

                if (
                    currentUserId != appointment.dentistId &&
                    currentUserId != appointment.patientId
                ) {
                    binding.appointmentDetailsProgressBar.visibility = View.GONE
                    showMessage("Нямате достъп до този час.")
                    return@addOnSuccessListener
                }

                displayAppointment(appointment)
                loadUserNames(appointment)
            }
            .addOnFailureListener { exception ->
                if (_binding == null) return@addOnFailureListener

                binding.appointmentDetailsProgressBar.visibility = View.GONE

                showMessage(
                    exception.message
                        ?: "Неуспешно зареждане на часа."
                )
            }
    }

    private fun displayAppointment(appointment: AppointmentSlot) {
        val dateFormatter = SimpleDateFormat(
            "dd MMMM yyyy, HH:mm",
            Locale.forLanguageTag("bg-BG")
        )

        binding.appointmentDetailsDateTextView.text =
            "Дата и час: ${
                dateFormatter.format(Date(appointment.startDateTime))
            }"

        val statusText = when (appointment.status) {
            "AVAILABLE" -> "Свободен"
            "PENDING" -> "Чака потвърждение"
            "BOOKED" -> "Потвърден"
            "CANCELLED" -> "Отменен"
            else -> appointment.status
        }

        binding.appointmentDetailsStatusTextView.text =
            "Статус: $statusText"

        binding.appointmentDetailsProgressBar.visibility = View.GONE
        binding.appointmentDetailsContent.visibility = View.VISIBLE
    }

    private fun loadUserNames(appointment: AppointmentSlot) {
        val currentBinding = _binding ?: return

        currentBinding.appointmentDetailsPatientTextView.text =
            "Пациент: Няма избран пациент"

        currentBinding.appointmentDetailsDentistTextView.text =
            "Стоматолог: Зареждане..."

        appointment.patientId?.let { patientId ->
            userRepository.getUserById(
                uid = patientId,
                onSuccess = { user ->
                    _binding?.appointmentDetailsPatientTextView?.text =
                        "Пациент: ${user.name}"
                },
                onFailure = {
                    _binding?.appointmentDetailsPatientTextView?.text =
                        "Пациент: Неизвестен"
                }
            )
        }

        userRepository.getUserById(
            uid = appointment.dentistId,
            onSuccess = { user ->
                _binding?.appointmentDetailsDentistTextView?.text =
                    "Стоматолог: ${user.name}"
            },
            onFailure = {
                _binding?.appointmentDetailsDentistTextView?.text =
                    "Стоматолог: Неизвестен"
            }
        )
    }

    private fun showMessage(message: String) {
        val currentBinding = _binding ?: return

        SnackbarUtils.show(
            rootView = currentBinding.root,
            message = message
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_APPOINTMENT_ID = "appointmentId"
    }
}
