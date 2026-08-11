package com.velimir_gurguriev.dentalclinic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.velimir_gurguriev.dentalclinic.R
import com.velimir_gurguriev.dentalclinic.adapters.TimeSlotAdapter
import com.velimir_gurguriev.dentalclinic.databinding.FragmentAppointmentBinding
import com.velimir_gurguriev.dentalclinic.models.appointments.TimeSlotItem
import com.velimir_gurguriev.dentalclinic.repositories.AppointmentRepository
import com.velimir_gurguriev.dentalclinic.repositories.AuthRepository
import com.velimir_gurguriev.dentalclinic.services.appointments.AppointmentService
import com.velimir_gurguriev.dentalclinic.utils.appointments.AppointmentCalendarDecorator
import com.velimir_gurguriev.dentalclinic.utils.appointments.AppointmentDateUtils
import com.velimir_gurguriev.dentalclinic.utils.appointments.TimeSlotGenerator
import com.velimir_gurguriev.dentalclinic.utils.ui.SnackbarUtils
import java.util.Calendar

class AppointmentFragment : Fragment() {

    private lateinit var binding: FragmentAppointmentBinding
    private lateinit var appointmentService: AppointmentService
    private lateinit var timeSlotAdapter: TimeSlotAdapter

    private lateinit var authRepository: AuthRepository

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
        authRepository = AuthRepository()

        val appointmentRepository =
            AppointmentRepository()

        appointmentService =
            AppointmentService(
                appointmentRepository
            )
    }

    private fun setupRecyclerView() {
        val timeSlots = TimeSlotGenerator.generate(
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
        val today = AppointmentDateUtils.getToday()

        selectedDate = today.timeInMillis

        binding.appointmentCalendarView.setMinimumDate(today)
        binding.appointmentCalendarView.setDate(today)

        binding.appointmentCalendarView.setSelectionBackground(
            R.drawable.calendar_selected_day
        )

        AppointmentCalendarDecorator.applyWeekendColors(
            binding.appointmentCalendarView
        )
        updateSelectedDateText()
        updateWeekendState(today)
        loadPublishedSlots()

        binding.appointmentCalendarView.setOnDayClickListener(
            object : OnDayClickListener {

                override fun onDayClick(eventDay: EventDay) {
                    val clickedDate =
                        eventDay.calendar.clone() as Calendar

                    AppointmentDateUtils.clearTime(
                        clickedDate
                    )

                    if (
                        AppointmentDateUtils.isPastDate(
                            clickedDate
                        )
                    ) {
                        showMessage(
                            "Не можете да изберете изминала дата."
                        )
                        return
                    }

                    binding.appointmentCalendarView.setDate(
                        clickedDate
                    )

                    updateSelectedDate(eventDay)
                    timeSlotAdapter.clearSelection()

                    if (
                        !AppointmentDateUtils.isWeekend(
                            clickedDate
                        )
                    ) {
                        loadPublishedSlots()
                    }
                }
            }
        )
    }

    private fun updateSelectedDate(
        eventDay: EventDay
    ) {
        val calendar =
            eventDay.calendar.clone() as Calendar

        AppointmentDateUtils.clearTime(
            calendar
        )

        selectedDate = calendar.timeInMillis

        updateSelectedDateText()
        updateWeekendState(calendar)
    }

    private fun updateWeekendState(
        calendar: Calendar
    ) {
        val isWeekend =
            AppointmentDateUtils.isWeekend(
                calendar
            )

        val weekendVisibility =
            if (isWeekend) View.VISIBLE else View.GONE

        val workingDayVisibility =
            if (isWeekend) View.GONE else View.VISIBLE

        binding.weekendMessageTextView.visibility =
            weekendVisibility

        listOf(
            binding.selectTimeTitleTextView,
            binding.appointmentSlotsRecyclerView,
            binding.selectAllSlotsButton,
            binding.deselectAllSlotsButton,
            binding.createAppointmentSlotsButton,
            binding.removeAppointmentSlotButton
        ).forEach { view ->
            view.visibility = workingDayVisibility
        }
    }

    private fun setupClickListeners() {
        binding.createAppointmentSlotsButton
            .setOnClickListener {
                createSelectedAppointmentSlots()
            }

        binding.removeAppointmentSlotButton
            .setOnClickListener {
                confirmAppointmentSlotCancellation()
            }

        binding.selectAllSlotsButton
            .setOnClickListener {
                timeSlotAdapter.selectAllUnpublishedSlots()
            }

        binding.deselectAllSlotsButton
            .setOnClickListener {
                timeSlotAdapter.clearSelection()
            }
    }

    private fun loadPublishedSlots() {
        val dentistId =
            getCurrentDentistId()
                ?: return

        appointmentService.getDentistSlotsForDate(
            dentistId = dentistId,
            selectedDate = selectedDate
        )
            .addOnSuccessListener { appointmentSlots ->
                timeSlotAdapter.updatePublishedSlots(
                    appointmentSlots
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
        val dentistId =
            getCurrentDentistId()
                ?: return

        val selectedSlots =
            timeSlotAdapter.getSelectedSlots()

        if (selectedSlots.isEmpty()) {
            showMessage(
                "Моля, изберете поне един час за добавяне."
            )
            return
        }

        setCreationLoadingState(true)

        appointmentService.createAppointmentSlots(
            dentistId = dentistId,
            selectedDate = selectedDate,
            timeSlots = selectedSlots
        )
            .addOnSuccessListener {
                setCreationLoadingState(false)

                timeSlotAdapter.clearSelection()
                loadPublishedSlots()

                showMessage(
                    "Избраните часове са добавени успешно."
                )
            }
            .addOnFailureListener { exception ->
                setCreationLoadingState(false)

                showMessage(
                    exception.message
                        ?: "Възникна грешка при добавянето на часовете."
                )
            }
    }

    private fun confirmAppointmentSlotCancellation() {
        val selectedSlots =
            timeSlotAdapter
                .getSelectedSlotsForCancellation()

        if (selectedSlots.isEmpty()) {
            showMessage(
                "Моля, изберете поне един час за премахване."
            )
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Премахване на час")
            .setMessage(
                "Сигурни ли сте, че искате да премахнете избраните часове?"
            )
            .setPositiveButton("Премахни") { _, _ ->
                cancelSelectedAppointmentSlots(
                    selectedSlots
                )
            }
            .setNegativeButton("Отказ", null)
            .show()
    }

    private fun cancelSelectedAppointmentSlots(
        selectedSlots: List<TimeSlotItem>
    ) {
        setCancellationLoadingState(true)

        appointmentService.cancelAppointmentSlots(
            timeSlots = selectedSlots
        )
            .addOnSuccessListener {
                setCancellationLoadingState(false)

                timeSlotAdapter.clearSelection()
                loadPublishedSlots()

                showMessage(
                    "Избраните часове са премахнати успешно."
                )
            }
            .addOnFailureListener { exception ->
                setCancellationLoadingState(false)

                showMessage(
                    exception.message
                        ?: "Възникна грешка при премахването на часовете."
                )
            }
    }

    private fun updateSelectedDateText() {
        val formattedDate =
            AppointmentDateUtils.formatDate(
                selectedDate
            )

        binding.selectedDateTextView.text =
            "Избрана дата: $formattedDate"
    }

    private fun setAppointmentControlsEnabled(
        isEnabled: Boolean
    ) {
        binding.createAppointmentSlotsButton.isEnabled =
            isEnabled

        binding.removeAppointmentSlotButton.isEnabled =
            isEnabled

        binding.selectAllSlotsButton.isEnabled =
            isEnabled

        binding.deselectAllSlotsButton.isEnabled =
            isEnabled
    }

    private fun setCreationLoadingState(
        isLoading: Boolean
    ) {
        setAppointmentControlsEnabled(
            !isLoading
        )

        binding.createAppointmentSlotsButton.text =
            if (isLoading) {
                "Добавяне..."
            } else {
                "Добави час"
            }
    }

    private fun setCancellationLoadingState(
        isLoading: Boolean
    ) {
        setAppointmentControlsEnabled(
            !isLoading
        )

        binding.removeAppointmentSlotButton.text =
            if (isLoading) {
                "Премахване..."
            } else {
                "Премахни час"
            }
    }

    private fun getCurrentDentistId(): String? {
        val dentistId =
            authRepository.getCurrentUserId()

        if (dentistId == null) {
            showMessage(
                "Не е намерен влязъл потребител."
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

    companion object {
        private const val START_WORKING_HOUR = 8
        private const val END_WORKING_HOUR = 16
        private const val GRID_COLUMN_COUNT = 3
    }
}