package com.velimir_gurguriev.dentalclinic.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.velimir_gurguriev.dentalclinic.databinding.DisplayDentistBinding
import com.velimir_gurguriev.dentalclinic.models.User

class DentistAdapter(
    private val dentists: List<User>
) : RecyclerView.Adapter<DentistAdapter.ViewHolder>() {

    class ViewHolder(
        val binding: DisplayDentistBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding = DisplayDentistBinding.inflate(
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
        val dentist = dentists[position]

        holder.binding.dentistNameTextView.text = dentist.name
        holder.binding.dentistEmailTextView.text = dentist.email
    }

    override fun getItemCount(): Int {
        return dentists.size
    }
}