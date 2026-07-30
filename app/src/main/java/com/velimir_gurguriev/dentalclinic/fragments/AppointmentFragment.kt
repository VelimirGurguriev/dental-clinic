package com.velimir_gurguriev.dentalclinic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.velimir_gurguriev.dentalclinic.adapters.TimeSlotAdapter
import com.velimir_gurguriev.dentalclinic.databinding.FragmentAppointmentBinding
import com.velimir_gurguriev.dentalclinic.models.appointments.AppointmentStatus
import com.velimir_gurguriev.dentalclinic.models.appointments.TimeSlotItem
import com.velimir_gurguriev.dentalclinic.repositories.AppointmentRepository
import com.velimir_gurguriev.dentalclinic.services.appointments.AppointmentService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AppointmentFragment : Fragment() {

    private lateinit var binding: FragmentAppointmentBinding
    private lateinit var appointmentService: AppointmentService
    private lateinit var timeSlotAdapter: TimeSlotAdapter

    private var selectedDate: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAppointmentBinding.inflate(
            inflater,
            container,
            false
        )

        initializeDependencies()
        setupRecyclerView()
        setupCalendar()
        setupClickListeners()

        return binding.root
    }

    private fun initializeDependencies() {
        val appointmentRepository = AppointmentRepository()

        appointmentService = AppointmentService(
            appointmentRepository
        )
    }

    private fun setupRecyclerView() {
        val timeSlots = generateTimeSlots(
            startHour = START_WORKING_HOUR,
            endHour = END_WORKING_HOUR
        )

        timeSlotAdapter = TimeSlotAdapter(timeSlots)

        binding.appointmentSlotsRecyclerView.layoutManager =
            GridLayoutManager(
                requireContext(),
                GRID_COLUMN_COUNT
            )

        binding.appointmentSlotsRecyclerView.adapter =
            timeSlotAdapter
    }

    private fun setupCalendar() {
        val today = Calendar.getInstance().apply {
            clearTime(this)
        }

        selectedDate = today.timeInMillis

        binding.appointmentCalendarView.setMinimumDate(today)
        binding.appointmentCalendarView.setDate(today)

        updateSelectedDateText()
        loadPublishedSlots()

        binding.appointmentCalendarView.setOnDayClickListener(
            object : OnDayClickListener {

                override fun onDayClick(eventDay: EventDay) {
                    updateSelectedDate(eventDay)

                    timeSlotAdapter.clearSelection()
                    loadPublishedSlots()
                }
            }
        )
    }

    private fun updateSelectedDate(eventDay: EventDay) {
        val calendar = eventDay.calendar.apply {
            clearTime(this)
        }

        selectedDate = calendar.timeInMillis

        updateSelectedDateText()
    }

    private fun setupClickListeners() {
        binding.createAppointmentSlotsButton.setOnClickListener {
            createSelectedAppointmentSlots()
        }
    }

    private fun loadPublishedSlots() {
        val dentistId = FirebaseAuth.getInstance()
            .currentUser
            ?.uid

        if (dentistId == null) {
            showMessage("Не е намерен влязъл потребител.")
            return
        }

        appointmentService.getDentistSlotsForDate(
            dentistId = dentistId,
            selectedDate = selectedDate
        )
            .addOnSuccessListener { appointmentSlots ->

                val publishedStartTimes = appointmentSlots
                    .filter { appointmentSlot ->
                        appointmentSlot.status !=
                                AppointmentStatus.CANCELLED.name
                    }
                    .map { appointmentSlot ->

                        val calendar = Calendar.getInstance().apply {
                            timeInMillis =
                                appointmentSlot.startDateTime
                        }

                        Pair(
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE)
                        )
                    }
                    .toSet()

                timeSlotAdapter.markSlotsAsPublished(
                    publishedStartTimes
                )
            }
            .addOnFailureListener { exception ->
                showMessage(
                    exception.message
                        ?: "Неуспешно зареждане на часовете."
                )
            }
    }

    private fun createSelectedAppointmentSlots() {
        val dentistId = FirebaseAuth.getInstance()
            .currentUser
            ?.uid

        if (dentistId == null) {
            showMessage("Не е намерен влязъл потребител.")
            return
        }

        val selectedSlots =
            timeSlotAdapter.getSelectedSlots()

        if (selectedSlots.isEmpty()) {
            showMessage(
                "Моля, изберете поне един свободен час."
            )
            return
        }

        setLoadingState(true)

        val creationTasks = selectedSlots.map { timeSlot ->

            val startDateTime = createDateTime(
                selectedDate = selectedDate,
                hour = timeSlot.startHour,
                minute = timeSlot.startMinute
            )

            val endDateTime = createDateTime(
                selectedDate = selectedDate,
                hour = timeSlot.endHour,
                minute = timeSlot.endMinute
            )

            appointmentService.createAppointmentSlot(
                dentistId = dentistId,
                startDateTime = startDateTime,
                endDateTime = endDateTime
            )
        }

        waitForAllTasks(creationTasks)
    }

    private fun waitForAllTasks(
        tasks: List<Task<Void>>
    ) {
        Tasks.whenAll(tasks)
            .addOnSuccessListener {
                setLoadingState(false)

                timeSlotAdapter.clearSelection()
                loadPublishedSlots()

                showMessage(
                    "Свободните часове са добавени успешно."
                )
            }
            .addOnFailureListener { exception ->
                setLoadingState(false)

                showMessage(
                    exception.message
                        ?: "Възникна грешка при добавянето на часовете."
                )
            }
    }

    private fun generateTimeSlots(
        startHour: Int,
        endHour: Int
    ): List<TimeSlotItem> {

        val timeSlots = mutableListOf<TimeSlotItem>()

        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (true) {
            val startSlotHour =
                currentTime.get(Calendar.HOUR_OF_DAY)

            val startSlotMinute =
                currentTime.get(Calendar.MINUTE)

            currentTime.add(
                Calendar.MINUTE,
                SLOT_DURATION_MINUTES
            )

            val endSlotHour =
                currentTime.get(Calendar.HOUR_OF_DAY)

            val endSlotMinute =
                currentTime.get(Calendar.MINUTE)

            if (
                endSlotHour > endHour ||
                (
                        endSlotHour == endHour &&
                                endSlotMinute > 0
                        )
            ) {
                break
            }

            timeSlots.add(
                TimeSlotItem(
                    startHour = startSlotHour,
                    startMinute = startSlotMinute,
                    endHour = endSlotHour,
                    endMinute = endSlotMinute
                )
            )
        }

        return timeSlots
    }

    private fun createDateTime(
        selectedDate: Long,
        hour: Int,
        minute: Int
    ): Long {

        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDate

            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return calendar.timeInMillis
    }

    private fun updateSelectedDateText() {
        val dateFormat = SimpleDateFormat(
            "dd MMMM yyyy",
            Locale("bg", "BG")
        )

        val formattedDate = dateFormat.format(
            Date(selectedDate)
        )

        binding.selectedDateTextView.text =
            "Избрана дата: $formattedDate"
    }

    private fun clearTime(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.createAppointmentSlotsButton.isEnabled =
            !isLoading

        binding.createAppointmentSlotsButton.text =
            if (isLoading) {
                "Добавяне..."
            } else {
                "Добави свободни часове"
            }
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        private const val START_WORKING_HOUR = 8
        private const val END_WORKING_HOUR = 16

        private const val SLOT_DURATION_MINUTES = 30
        private const val GRID_COLUMN_COUNT = 3
    }
}