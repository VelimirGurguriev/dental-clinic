package com.velimir_gurguriev.dentalclinic.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.velimir_gurguriev.dentalclinic.databinding.DisplayAppointmentRequestBinding
import com.velimir_gurguriev.dentalclinic.models.appointments.AppointmentSlot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DentistAppointmentRequestAdapter(
    private val onApproveClick: (AppointmentSlot) -> Unit,
    private val onRejectClick: (AppointmentSlot) -> Unit
) : RecyclerView.Adapter<DentistAppointmentRequestAdapter.ViewHolder>() {

    private val appointments = mutableListOf<AppointmentSlot>()
    private var patientNames: Map<String, String> = emptyMap()

    private val dateFormatter =
        SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale("bg", "BG"))

    inner class ViewHolder(
        private val binding: DisplayAppointmentRequestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: AppointmentSlot) {
            val patientName = appointment.patientId?.let { patientId ->
                patientNames[patientId]
            } ?: "Неизвестен пациент"

            binding.appointmentRequestPatientTextView.text =
                "Пациент: $patientName"

            binding.appointmentRequestDateTextView.text =
                dateFormatter.format(Date(appointment.startDateTime))

            binding.approveAppointmentButton.setOnClickListener {
                onApproveClick(appointment)
            }

            binding.rejectAppointmentButton.setOnClickListener {
                onRejectClick(appointment)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = DisplayAppointmentRequestBinding.inflate(
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
        holder.bind(appointments[position])
    }

    override fun getItemCount(): Int = appointments.size

    fun updateAppointments(
        items: List<AppointmentSlot>,
        names: Map<String, String>
    ) {
        appointments.clear()
        appointments.addAll(items)
        patientNames = names
        notifyDataSetChanged()
    }
}
