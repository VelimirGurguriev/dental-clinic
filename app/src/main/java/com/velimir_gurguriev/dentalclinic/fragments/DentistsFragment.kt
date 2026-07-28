package com.velimir_gurguriev.dentalclinic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.velimir_gurguriev.dentalclinic.adapters.DentistAdapter
import com.velimir_gurguriev.dentalclinic.databinding.FragmentDentistsBinding
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.services.DentistsService

class DentistsFragment : Fragment() {

    private lateinit var binding: FragmentDentistsBinding

    private lateinit var dentistsService: DentistsService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDentistsBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        initializeDependencies()
        setupRecyclerView()
        loadDentists()
    }

    private fun initializeDependencies() {
        val userRepository = UserRepository()
        dentistsService = DentistsService(userRepository)
    }

    private fun setupRecyclerView() {
        binding.dentistsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
    }

    private fun loadDentists() {
        dentistsService.loadDentists(
            { dentists ->
                binding.dentistsRecyclerView.adapter =
                    DentistAdapter(dentists)
            },
            {
                showMessage("Failed to load dentists.")
            }
        )
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}