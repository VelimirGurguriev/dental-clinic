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
            val context = binding.root.context

            binding.timeSlotButton.text = formatTime(
                timeSlot.startHour,
                timeSlot.startMinute
            )

            when {
                timeSlot.isPublished -> {
                    binding.timeSlotButton.isEnabled = false
                    binding.timeSlotButton.isChecked = false

                    binding.timeSlotButton.backgroundTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                context,
                                R.color.slot_published
                            )
                        )
                }

                timeSlot.isSelected -> {
                    binding.timeSlotButton.isEnabled = true
                    binding.timeSlotButton.isChecked = true

                    binding.timeSlotButton.backgroundTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                context,
                                R.color.slot_selected
                            )
                        )
                }

                else -> {
                    binding.timeSlotButton.isEnabled = true
                    binding.timeSlotButton.isChecked = false

                    binding.timeSlotButton.backgroundTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                context,
                                R.color.slot_normal
                            )
                        )
                }
            }

            binding.timeSlotButton.setOnClickListener {
                if (timeSlot.isPublished) {
                    return@setOnClickListener
                }

                timeSlot.isSelected = !timeSlot.isSelected

                notifyItemChanged(bindingAdapterPosition)
            }
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
            timeSlot.isSelected && !timeSlot.isPublished
        }
    }

    fun clearSelection() {
        timeSlots.forEach { timeSlot ->
            timeSlot.isSelected = false
        }

        notifyDataSetChanged()
    }

    fun updatePublishedSlots(
        appointmentSlots: List<AppointmentSlot>?
    ) {
        timeSlots.forEach { timeSlot ->

            val matchingAppointment =
                appointmentSlots?.find { appointmentSlot ->

                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = appointmentSlot.startDateTime
                    }

                    calendar.get(Calendar.HOUR_OF_DAY) ==
                            timeSlot.startHour &&
                            calendar.get(Calendar.MINUTE) ==
                            timeSlot.startMinute
                }

            timeSlot.appointmentId =
                matchingAppointment?.id

            timeSlot.appointmentStatus =
                matchingAppointment
                    ?.status
                    ?.let { status ->
                        AppointmentStatus.valueOf(status)
                    }

            timeSlot.isPublished =
                matchingAppointment != null &&
                        matchingAppointment.status == AppointmentStatus.AVAILABLE.name

            if (timeSlot.isPublished) {
                timeSlot.isSelected = false
            }
        }

        notifyDataSetChanged()
    }

    fun selectAllAvailableSlots() {
        timeSlots.forEach { timeSlot ->
            if (!timeSlot.isPublished) {
                timeSlot.isSelected = true
            }
        }

        notifyDataSetChanged()
    }

    fun deselectAllSlots() {
        timeSlots.forEach { timeSlot ->
            timeSlot.isSelected = false
        }

        notifyDataSetChanged()
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