package com.velimir_gurguriev.dentalclinic.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.velimir_gurguriev.dentalclinic.databinding.DisplayDentistPatientBinding
import com.velimir_gurguriev.dentalclinic.models.connections.DentistPatientItem

class DentistPatientAdapter(
    private val patients: MutableList<DentistPatientItem>,
    private val onViewClick: (DentistPatientItem) -> Unit
) : RecyclerView.Adapter<DentistPatientAdapter.DentistPatientViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DentistPatientViewHolder {

        val binding = DisplayDentistPatientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return DentistPatientViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: DentistPatientViewHolder,
        position: Int
    ) {
        holder.bind(patients[position])
    }

    override fun getItemCount(): Int {
        return patients.size
    }

    fun updatePatients(
        newPatients: List<DentistPatientItem>
    ) {
        patients.clear()
        patients.addAll(newPatients)
        notifyDataSetChanged()
    }

    fun addPatient(
        patient: DentistPatientItem
    ) {
        patients.add(patient)
        notifyItemInserted(patients.lastIndex)
    }

    inner class DentistPatientViewHolder(
        private val binding: DisplayDentistPatientBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DentistPatientItem) {
            binding.patientNameTextView.text =
                item.patient.name

            binding.patientEmailTextView.text =
                item.patient.email

            binding.viewPatientButton.setOnClickListener {
                onViewClick(item)
            }
        }
    }
}