package com.velimir_gurguriev.dentalclinic.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.velimir_gurguriev.dentalclinic.databinding.DisplayPatientRequestBinding
import com.velimir_gurguriev.dentalclinic.models.connections.PatientRequestItem

class PatientRequestAdapter(
    private val requests: MutableList<PatientRequestItem>,
    private val onApproveClick: (PatientRequestItem) -> Unit,
    private val onRejectClick: (PatientRequestItem) -> Unit
) : RecyclerView.Adapter<PatientRequestAdapter.PatientRequestViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PatientRequestViewHolder {

        val binding = DisplayPatientRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return PatientRequestViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: PatientRequestViewHolder,
        position: Int
    ) {
        holder.bind(requests[position])
    }

    override fun getItemCount(): Int {
        return requests.size
    }

    fun updateRequests(newRequests: List<PatientRequestItem>) {
        requests.clear()
        requests.addAll(newRequests)
        notifyDataSetChanged()
    }

    fun removeRequest(item: PatientRequestItem) {
        val position = requests.indexOf(item)

        if (position == -1) {
            return
        }

        requests.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class PatientRequestViewHolder(
        private val binding: DisplayPatientRequestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PatientRequestItem) {
            binding.patientNameTextView.text = item.patient.name
            binding.patientEmailTextView.text = item.patient.email

            binding.approveRequestButton.setOnClickListener {
                onApproveClick(item)
            }

            binding.rejectRequestButton.setOnClickListener {
                onRejectClick(item)
            }
        }
    }
}