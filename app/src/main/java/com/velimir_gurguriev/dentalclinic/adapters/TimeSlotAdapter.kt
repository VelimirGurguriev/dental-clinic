package com.velimir_gurguriev.dentalclinic.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.velimir_gurguriev.dentalclinic.R
import com.velimir_gurguriev.dentalclinic.databinding.DisplayTimeSlotBinding
import com.velimir_gurguriev.dentalclinic.models.appointments.AppointmentSlot
import com.velimir_gurguriev.dentalclinic.models.appointments.AppointmentStatus
import com.velimir_gurguriev.dentalclinic.models.appointments.TimeSlotItem
import java.util.Calendar
import java.util.Locale

class TimeSlotAdapter(
    private val timeSlots: List<TimeSlotItem>
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    inner class TimeSlotViewHolder(
        private val binding: DisplayTimeSlotBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(timeSlot: TimeSlotItem) {
            binding.timeSlotButton.text = formatTime(
                timeSlot.startHour,
                timeSlot.startMinute
            )

            when {
                timeSlot.appointmentStatus ==
                        AppointmentStatus.BOOKED -> {

                    binding.timeSlotButton.isEnabled = false
                    binding.timeSlotButton.isChecked = false

                    setButtonColor(
                        R.color.slot_booked
                    )
                }

                timeSlot.appointmentStatus ==
                        AppointmentStatus.AVAILABLE &&
                        timeSlot.isSelectedForCancellation -> {

                    binding.timeSlotButton.isEnabled = true
                    binding.timeSlotButton.isChecked = true

                    setButtonColor(
                        R.color.slot_remove_selected
                    )
                }

                timeSlot.appointmentStatus ==
                        AppointmentStatus.AVAILABLE -> {

                    binding.timeSlotButton.isEnabled = true
                    binding.timeSlotButton.isChecked = false

                    setButtonColor(
                        R.color.slot_published
                    )
                }

                timeSlot.isSelected -> {
                    binding.timeSlotButton.isEnabled = true
                    binding.timeSlotButton.isChecked = true

                    setButtonColor(
                        R.color.slot_selected
                    )
                }

                else -> {
                    binding.timeSlotButton.isEnabled = true
                    binding.timeSlotButton.isChecked = false

                    setButtonColor(
                        R.color.slot_normal
                    )
                }
            }

            binding.timeSlotButton.setOnClickListener {
                val position = bindingAdapterPosition

                if (position == RecyclerView.NO_POSITION) {
                    return@setOnClickListener
                }

                when (timeSlot.appointmentStatus) {
                    AppointmentStatus.AVAILABLE -> {
                        timeSlot.isSelectedForCancellation =
                            !timeSlot.isSelectedForCancellation

                        timeSlot.isSelected = false
                    }

                    AppointmentStatus.BOOKED -> {
                        return@setOnClickListener
                    }

                    else -> {
                        timeSlot.isSelected =
                            !timeSlot.isSelected

                        timeSlot.isSelectedForCancellation = false
                    }
                }

                notifyItemChanged(position)
            }
        }

        private fun setButtonColor(
            colorResource: Int
        ) {
            binding.timeSlotButton.backgroundTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        colorResource
                    )
                )
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TimeSlotViewHolder {

        val binding = DisplayTimeSlotBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return TimeSlotViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: TimeSlotViewHolder,
        position: Int
    ) {
        holder.bind(timeSlots[position])
    }

    override fun getItemCount(): Int {
        return timeSlots.size
    }

    fun getSelectedSlots(): List<TimeSlotItem> {
        return timeSlots.filter { timeSlot ->
            timeSlot.isSelected &&
                    timeSlot.appointmentStatus == null
        }
    }

    fun getSelectedSlotsForCancellation(): List<TimeSlotItem> {
        return timeSlots.filter { timeSlot ->
            timeSlot.isSelectedForCancellation &&
                    timeSlot.appointmentStatus ==
                    AppointmentStatus.AVAILABLE &&
                    !timeSlot.appointmentId.isNullOrBlank()
        }
    }

    fun clearSelection() {
        timeSlots.forEach { timeSlot ->
            timeSlot.isSelected = false
            timeSlot.isSelectedForCancellation = false
        }

        notifyDataSetChanged()
    }

    fun updatePublishedSlots(
        appointmentSlots: List<AppointmentSlot>
    ) {
        timeSlots.forEach { timeSlot ->

            val matchingAppointment =
                appointmentSlots.find { appointmentSlot ->

                    val calendar = Calendar.getInstance().apply {
                        timeInMillis =
                            appointmentSlot.startDateTime
                    }

                    calendar.get(Calendar.HOUR_OF_DAY) ==
                            timeSlot.startHour &&
                            calendar.get(Calendar.MINUTE) ==
                            timeSlot.startMinute &&
                            appointmentSlot.status !=
                            AppointmentStatus.CANCELLED.name
                }

            timeSlot.appointmentId =
                matchingAppointment?.id

            timeSlot.appointmentStatus =
                matchingAppointment
                    ?.status
                    ?.let { status ->
                        runCatching {
                            AppointmentStatus.valueOf(status)
                        }.getOrNull()
                    }

            timeSlot.isPublished =
                timeSlot.appointmentStatus ==
                        AppointmentStatus.AVAILABLE

            timeSlot.isSelected = false
            timeSlot.isSelectedForCancellation = false
        }

        notifyDataSetChanged()
    }

    fun selectAllAvailableSlots() {
        timeSlots.forEach { timeSlot ->
            if (timeSlot.appointmentStatus == null) {
                timeSlot.isSelected = true
                timeSlot.isSelectedForCancellation = false
            }
        }

        notifyDataSetChanged()
    }

    fun deselectAllSlots() {
        clearSelection()
    }

    private fun formatTime(
        hour: Int,
        minute: Int
    ): String {
        return String.format(
            Locale.getDefault(),
            "%02d:%02d",
            hour,
            minute
        )
    }
}