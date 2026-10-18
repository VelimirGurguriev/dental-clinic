
package com.velimir_gurguriev.dentalclinic.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.velimir_gurguriev.dentalclinic.databinding.DisplayTimeSlotBinding
import com.velimir_gurguriev.dentalclinic.models.appointments.AppointmentSlot
import com.velimir_gurguriev.dentalclinic.models.appointments.AppointmentStatus
import com.velimir_gurguriev.dentalclinic.utils.appointments.AppointmentDateUtils
import com.velimir_gurguriev.dentalclinic.utils.appointments.TimeSlotGenerator
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import com.velimir_gurguriev.dentalclinic.R
import java.util.Locale


class PatientTimeSlotAdapter(
    private val currentPatientId: String,
    private val onAppointmentClick: (String) -> Unit,
    private val onSelectionChanged: (AppointmentSlot?) -> Unit
) : RecyclerView.Adapter<PatientTimeSlotAdapter.ViewHolder>() {

    private val timeSlots = TimeSlotGenerator.generate(8, 16)
    private var appointments = emptyMap<Long, AppointmentSlot>()
    private var selectedDate = 0L
    private var selectedAppointment: AppointmentSlot? = null

    inner class ViewHolder(
        private val binding: DisplayTimeSlotBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(hour: Int, minute: Int) {
            val startTime = AppointmentDateUtils.createDateTime(
                selectedDate, hour, minute
            )

            val appointment = appointments[startTime]

            val isOwnAppointment =
                appointment?.patientId == currentPatientId

            val canViewAppointment =
                isOwnAppointment &&
                        (
                            appointment?.status == AppointmentStatus.PENDING.name ||
                            appointment?.status == AppointmentStatus.BOOKED.name
                        )

            val isAvailable =
                appointment?.status == AppointmentStatus.AVAILABLE.name &&
                        startTime > System.currentTimeMillis()

            binding.timeSlotButton.text = String.format(
                Locale.getDefault(), "%02d:%02d", hour, minute
            )

            binding.timeSlotButton.isEnabled =
                isAvailable || canViewAppointment
            binding.timeSlotButton.isChecked =
                appointment?.id == selectedAppointment?.id && isAvailable

            val slotColor = when {
                isAvailable &&
                        appointment?.id == selectedAppointment?.id ->
                    R.color.patient_slot_selected

                isAvailable ->
                    R.color.patient_slot_available

                canViewAppointment &&
                        appointment?.status == AppointmentStatus.PENDING.name ->
                    R.color.patient_slot_pending

                canViewAppointment &&
                        appointment?.status == AppointmentStatus.BOOKED.name ->
                    R.color.patient_slot_booked

                else ->
                    R.color.patient_slot_unavailable
            }


            val textColor =
                if (slotColor == R.color.patient_slot_unavailable) {
                    R.color.patient_slot_disabled_text
                } else {
                    R.color.patient_slot_text
                }

            binding.timeSlotButton.backgroundTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        slotColor
                    )
                )

            binding.timeSlotButton.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    textColor
                )
            )


            binding.timeSlotButton.setOnClickListener {
                val currentAppointment = appointment
                    ?: return@setOnClickListener

                if (canViewAppointment) {
                    onAppointmentClick(currentAppointment.id)
                    return@setOnClickListener
                }

                if (!isAvailable) {
                    return@setOnClickListener
                }

                selectedAppointment =
                    if (selectedAppointment?.id == currentAppointment.id) {
                        null
                    } else {
                        currentAppointment
                    }

                notifyDataSetChanged()
                onSelectionChanged(selectedAppointment)
            }

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = DisplayTimeSlotBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val slot = timeSlots[position]
        holder.bind(slot.startHour, slot.startMinute)
    }

    override fun getItemCount(): Int = timeSlots.size

    fun updateAppointments(
        date: Long,
        slots: List<AppointmentSlot>
    ) {
        selectedDate = date

        appointments = slots.associateBy { it.startDateTime }

        selectedAppointment = null
        notifyDataSetChanged()
        onSelectionChanged(null)
    }

    fun clearAppointments() {
        appointments = emptyMap()
        selectedAppointment = null
        notifyDataSetChanged()
        onSelectionChanged(null)
    }
}
