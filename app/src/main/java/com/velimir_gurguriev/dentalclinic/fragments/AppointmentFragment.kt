package com.velimir_gurguriev.dentalclinic.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.velimir_gurguriev.dentalclinic.R
import com.velimir_gurguriev.dentalclinic.adapters.TimeSlotAdapter
import com.velimir_gurguriev.dentalclinic.databinding.FragmentAppointmentBinding
import com.velimir_gurguriev.dentalclinic.models.appointments.TimeSlotItem
import com.velimir_gurguriev.dentalclinic.repositories.AppointmentRepository
import com.velimir_gurguriev.dentalclinic.services.appointments.AppointmentService
import com.velimir_gurguriev.dentalclinic.utils.appointments.TimeSlotGenerator
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
        val today = Calendar.getInstance().apply {
            clearTime(this)
        }

        selectedDate = today.timeInMillis

        binding.appointmentCalendarView.setMinimumDate(today)
        binding.appointmentCalendarView.setDate(today)

        binding.appointmentCalendarView.setSelectionBackground(
            R.drawable.calendar_selected_day
        )

        applyWeekendColors()
        updateSelectedDateText()
        updateWeekendState(today)
        loadPublishedSlots()

        binding.appointmentCalendarView.setOnDayClickListener(
            object : OnDayClickListener {

                override fun onDayClick(eventDay: EventDay) {
                    val clickedDate =
                        eventDay.calendar.clone() as Calendar

                    clearTime(clickedDate)

                    val today = Calendar.getInstance().apply {
                        clearTime(this)
                    }

                    if (clickedDate.before(today)) {
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

                    if (!isWeekend(clickedDate)) {
                        loadPublishedSlots()
                    }
                }
            }
        )
    }

    private fun isWeekend(
        calendar: Calendar
    ): Boolean {
        val dayOfWeek =
            calendar.get(Calendar.DAY_OF_WEEK)

        return dayOfWeek == Calendar.SATURDAY ||
                dayOfWeek == Calendar.SUNDAY
    }

    private fun applyWeekendColors() {
        val weekendDays = mutableListOf<CalendarDay>()

        val startDate = Calendar.getInstance().apply {
            add(Calendar.YEAR, -1)
            clearTime(this)
        }

        val endDate = Calendar.getInstance().apply {
            add(Calendar.YEAR, 5)
            clearTime(this)
        }

        val currentDate = startDate.clone() as Calendar

        while (!currentDate.after(endDate)) {
            val dayOfWeek =
                currentDate.get(Calendar.DAY_OF_WEEK)

            if (
                dayOfWeek == Calendar.SATURDAY ||
                dayOfWeek == Calendar.SUNDAY
            ) {
                weekendDays.add(
                    CalendarDay(
                        currentDate.clone() as Calendar
                    ).apply {
                        labelColor =
                            R.color.calendar_weekend
                    }
                )
            }

            currentDate.add(
                Calendar.DAY_OF_MONTH,
                1
            )
        }

        binding.appointmentCalendarView.setCalendarDays(
            weekendDays
        )
    }

    private fun updateSelectedDate(
        eventDay: EventDay
    ) {
        val calendar =
            eventDay.calendar.clone() as Calendar

        clearTime(calendar)

        selectedDate = calendar.timeInMillis

        updateSelectedDateText()
        updateWeekendState(calendar)
    }

    private fun updateWeekendState(
        calendar: Calendar
    ) {
        val dayOfWeek =
            calendar.get(Calendar.DAY_OF_WEEK)

        val isWeekend =
            dayOfWeek == Calendar.SATURDAY ||
                    dayOfWeek == Calendar.SUNDAY

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
                timeSlotAdapter.selectAllAvailableSlots()
            }

        binding.deselectAllSlotsButton
            .setOnClickListener {
                timeSlotAdapter.deselectAllSlots()
            }
    }

    private fun loadPublishedSlots() {
        val dentistId = FirebaseAuth.getInstance()
            .currentUser
            ?.uid

        if (dentistId == null) {
            showMessage(
                "Не е намерен влязъл потребител."
            )
            return
        }

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
        val dentistId = FirebaseAuth.getInstance()
            .currentUser
            ?.uid

        if (dentistId == null) {
            showMessage(
                "Не е намерен влязъл потребител."
            )
            return
        }

        val selectedSlots =
            timeSlotAdapter.getSelectedSlots()

        if (selectedSlots.isEmpty()) {
            showMessage(
                "Моля, изберете поне един час за добавяне."
            )
            return
        }

        setCreationLoadingState(true)

        val creationTasks =
            selectedSlots.map { timeSlot ->

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

        waitForCreationTasks(creationTasks)
    }

    private fun waitForCreationTasks(
        tasks: List<Task<Void>>
    ) {
        Tasks.whenAll(tasks)
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

        val cancellationTasks =
            selectedSlots.mapNotNull { timeSlot ->

                val appointmentId =
                    timeSlot.appointmentId

                if (appointmentId.isNullOrBlank()) {
                    null
                } else {
                    appointmentService
                        .cancelAppointmentSlot(
                            appointmentId
                        )
                }
            }

        if (cancellationTasks.isEmpty()) {
            setCancellationLoadingState(false)

            showMessage(
                "Не бяха намерени часове за премахване."
            )
            return
        }

        Tasks.whenAll(cancellationTasks)
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

    private fun createDateTime(
        selectedDate: Long,
        hour: Int,
        minute: Int
    ): Long {

        val calendar =
            Calendar.getInstance().apply {
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

        val formattedDate =
            dateFormat.format(
                Date(selectedDate)
            )

        binding.selectedDateTextView.text =
            "Избрана дата: $formattedDate"
    }

    private fun clearTime(
        calendar: Calendar
    ) {
        calendar.set(
            Calendar.HOUR_OF_DAY,
            0
        )
        calendar.set(
            Calendar.MINUTE,
            0
        )
        calendar.set(
            Calendar.SECOND,
            0
        )
        calendar.set(
            Calendar.MILLISECOND,
            0
        )
    }

    private fun setCreationLoadingState(
        isLoading: Boolean
    ) {
        binding.createAppointmentSlotsButton.isEnabled =
            !isLoading

        binding.removeAppointmentSlotButton.isEnabled =
            !isLoading

        binding.selectAllSlotsButton.isEnabled =
            !isLoading

        binding.deselectAllSlotsButton.isEnabled =
            !isLoading

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
        binding.createAppointmentSlotsButton.isEnabled =
            !isLoading

        binding.removeAppointmentSlotButton.isEnabled =
            !isLoading

        binding.selectAllSlotsButton.isEnabled =
            !isLoading

        binding.deselectAllSlotsButton.isEnabled =
            !isLoading

        binding.removeAppointmentSlotButton.text =
            if (isLoading) {
                "Премахване..."
            } else {
                "Премахни час"
            }
    }

    private fun showMessage(
        message: String
    ) {
        val snackbar = Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_SHORT
        )

        val snackbarView = snackbar.view

        ViewCompat.setBackgroundTintList(
            snackbarView,
            null
        )

        snackbarView.background =
            AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.snackbar_background
            )

        val layoutParams =
            snackbarView.layoutParams as FrameLayout.LayoutParams

        layoutParams.gravity =
            Gravity.TOP or Gravity.CENTER_HORIZONTAL

        val horizontalMargin =
            resources.getDimensionPixelSize(
                R.dimen.snackbar_horizontal_margin
            )

        layoutParams.setMargins(
            horizontalMargin,
            resources.getDimensionPixelSize(
                R.dimen.snackbar_top_margin
            ),
            horizontalMargin,
            0
        )

        snackbarView.layoutParams = layoutParams

        snackbarView.elevation =
            resources.getDimension(
                R.dimen.snackbar_elevation
            )

        snackbar.show()
    }

    companion object {
        private const val START_WORKING_HOUR = 8
        private const val END_WORKING_HOUR = 16
        private const val GRID_COLUMN_COUNT = 3
    }
}