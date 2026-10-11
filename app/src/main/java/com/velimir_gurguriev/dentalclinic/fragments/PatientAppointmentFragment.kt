package com.velimir_gurguriev.dentalclinic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.velimir_gurguriev.dentalclinic.databinding.FragmentPatientAppointmentBinding
import android.widget.ArrayAdapter
import com.velimir_gurguriev.dentalclinic.models.User
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.repositories.DentistPatientConnectionRepository
import com.velimir_gurguriev.dentalclinic.services.connections.DentistPatientConnectionService
import com.velimir_gurguriev.dentalclinic.utils.ui.SnackbarUtils
import androidx.recyclerview.widget.GridLayoutManager
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.velimir_gurguriev.dentalclinic.adapters.PatientTimeSlotAdapter
import com.velimir_gurguriev.dentalclinic.models.appointments.AppointmentSlot
import com.velimir_gurguriev.dentalclinic.repositories.AppointmentRepository
import com.velimir_gurguriev.dentalclinic.services.appointments.AppointmentService
import com.velimir_gurguriev.dentalclinic.utils.appointments.AppointmentDateUtils
import java.util.Calendar
import com.velimir_gurguriev.dentalclinic.R
import com.velimir_gurguriev.dentalclinic.utils.appointments.AppointmentCalendarDecorator
import androidx.appcompat.app.AlertDialog



class PatientAppointmentFragment : Fragment() {

    private var _binding: FragmentPatientAppointmentBinding? = null
    private val binding: FragmentPatientAppointmentBinding
        get() = _binding!!

    private lateinit var appointmentService: AppointmentService
    private lateinit var patientTimeSlotAdapter: PatientTimeSlotAdapter

    private var selectedDate: Long = 0L
    private var selectedAppointment: AppointmentSlot? = null
    private var loadRequestId = 0

    private lateinit var authRepository: AuthRepository
    private lateinit var connectionService: DentistPatientConnectionService

    private var selectedDentist: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientAppointmentBinding.inflate(
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

        authRepository = AuthRepository()

        connectionService = DentistPatientConnectionService(
            connectionRepository = DentistPatientConnectionRepository(),
            userRepository = UserRepository()
        )

        appointmentService = AppointmentService(
            AppointmentRepository()
        )

        setupTimeSlots()
        setupCalendar()

        binding.requestAppointmentButton.isEnabled = false

        binding.requestAppointmentButton.setOnClickListener {
            confirmAppointmentRequest()
        }

        loadApprovedDentists()
    }


    private fun confirmAppointmentRequest() {
        val appointment = selectedAppointment ?: return
        val dentist = selectedDentist ?: return

        val appointmentTime = java.text.SimpleDateFormat(
            "HH:mm",
            java.util.Locale("bg", "BG")
        ).format(java.util.Date(appointment.startDateTime))

        val formattedDate = AppointmentDateUtils.formatDate(
            appointment.startDateTime
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Заявяване на час")
            .setMessage(
                "Искате ли да заявите час при ${dentist.name} " +
                        "на $formattedDate от $appointmentTime?"
            )
            .setPositiveButton("Заяви") { _, _ ->
                submitAppointmentRequest(
                    appointmentId = appointment.id,
                    dentistId = dentist.uid
                )
            }
            .setNegativeButton("Отказ", null)
            .show()
    }


    private fun submitAppointmentRequest(
        appointmentId: String,
        dentistId: String
    ) {
        val patientId = authRepository.getCurrentUserId()

        if (patientId.isNullOrBlank()) {
            showMessage("Не е намерен влязъл потребител.")
            return
        }

        binding.requestAppointmentButton.isEnabled = false
        binding.requestAppointmentButton.text = "Изпращане..."

        appointmentService.requestAppointment(
            appointmentId = appointmentId,
            dentistId = dentistId,
            patientId = patientId
        )
            .addOnSuccessListener {
                if (_binding == null) {
                    return@addOnSuccessListener
                }

                binding.requestAppointmentButton.text = "Заяви час"

                selectedAppointment = null
                loadDentistAppointments()

                showMessage(
                    "Заявката за час е изпратена успешно."
                )
            }
            .addOnFailureListener { exception ->
                if (_binding == null) {
                    return@addOnFailureListener
                }

                binding.requestAppointmentButton.text = "Заяви час"

                loadDentistAppointments()

                showMessage(
                    exception.message
                        ?: "Неуспешно изпращане на заявката."
                )
            }
    }


    private fun setupTimeSlots() {
        patientTimeSlotAdapter = PatientTimeSlotAdapter { appointment ->
            selectedAppointment = appointment

            binding.requestAppointmentButton.isEnabled =
                appointment != null
        }

        binding.patientSlotsRecyclerView.layoutManager =
            GridLayoutManager(requireContext(), 3)

        binding.patientSlotsRecyclerView.adapter =
            patientTimeSlotAdapter
    }

    private fun setupCalendar() {
        val today = AppointmentDateUtils.getToday()

        selectedDate = today.timeInMillis

        binding.patientAppointmentCalendarView.apply {
            setMinimumDate(today)
            setDate(today)

            setSelectionBackground(
                R.drawable.calendar_selected_day
            )

            AppointmentCalendarDecorator.applyWeekendColors(this)
        }

        updateSelectedDateText()
        updateWeekendState()

        binding.patientAppointmentCalendarView.setOnDayClickListener(
            object : OnDayClickListener {

                override fun onDayClick(eventDay: EventDay) {
                    val date = eventDay.calendar.clone() as Calendar

                    AppointmentDateUtils.clearTime(date)

                    if (AppointmentDateUtils.isPastDate(date)) {
                        return
                    }

                    binding.patientAppointmentCalendarView.setDate(date)

                    selectedDate = date.timeInMillis
                    updateSelectedDateText()
                    updateWeekendState()
                    loadDentistAppointments()
                }
            }
        )
    }

    private fun updateWeekendState() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDate
        }

        val isWeekend = AppointmentDateUtils.isWeekend(calendar)

        binding.patientWeekendMessageTextView.visibility =
            if (isWeekend) View.VISIBLE else View.GONE

        binding.patientSlotsTitleTextView.visibility =
            if (isWeekend) View.GONE else View.VISIBLE

        binding.patientSlotsRecyclerView.visibility =
            if (isWeekend) View.GONE else View.VISIBLE

        binding.requestAppointmentButton.visibility =
            if (isWeekend) View.GONE else View.VISIBLE
    }

    private fun updateSelectedDateText() {
        binding.patientSelectedDateTextView.text =
            "Избрана дата: ${AppointmentDateUtils.formatDate(selectedDate)}"
    }


    private fun loadDentistAppointments() {
        val requestId = ++loadRequestId
        patientTimeSlotAdapter.clearAppointments()

        val dentist = selectedDentist ?: return
        val date = selectedDate

        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
        }

        if (AppointmentDateUtils.isWeekend(calendar)) {
            return
        }

        appointmentService.getDentistSlotsForDate(
            dentistId = dentist.uid,
            selectedDate = date
        )
            .addOnSuccessListener { slots ->
                if (_binding == null || requestId != loadRequestId) {
                    return@addOnSuccessListener
                }

                patientTimeSlotAdapter.updateAppointments(
                    date = date,
                    slots = slots
                )
            }
            .addOnFailureListener { exception ->
                if (_binding == null || requestId != loadRequestId) {
                    return@addOnFailureListener
                }

                SnackbarUtils.show(
                    rootView = binding.root,
                    message = exception.message
                        ?: "Неуспешно зареждане на часовете."
                )
            }
    }

    private fun loadApprovedDentists() {
        val patientId = authRepository.getCurrentUserId()
            ?: return

        connectionService.getApprovedDentistsForPatient(patientId)
            .addOnSuccessListener { dentists ->

                if (_binding == null) {
                    return@addOnSuccessListener
                }

                setupDentistDropdown(dentists)
            }
            .addOnFailureListener { exception ->

                val currentBinding = _binding
                    ?: return@addOnFailureListener

                SnackbarUtils.show(
                    rootView = currentBinding.root,
                    message = exception.message
                        ?: "Неуспешно зареждане на стоматолозите."
                )
            }
    }

    private fun setupDentistDropdown(dentists: List<User>) {
        val currentBinding = _binding ?: return

        selectedDentist = null

        val dentistNames = dentists.map { dentist ->
            dentist.name
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            dentistNames
        )

        currentBinding.dentistDropdown.setAdapter(adapter)

        currentBinding.dentistDropdown.setText("", false)

        currentBinding.dentistDropdown.setOnItemClickListener {
                _, _, position, _ ->

            selectedDentist = dentists[position]
            loadDentistAppointments()
        }

        if (dentists.isEmpty()) {
            currentBinding.dentistDropdownLayout.helperText =
                "Нямате одобрени стоматолози."
        } else {
            currentBinding.dentistDropdownLayout.helperText = null
        }
    }

    override fun onDestroyView() {
        binding.patientSlotsRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private fun showMessage(message: String) {
        val currentBinding = _binding ?: return

        SnackbarUtils.show(
            rootView = currentBinding.root,
            message = message
        )
    }
}
