package com.velimir_gurguriev.dentalclinic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.velimir_gurguriev.dentalclinic.R
import com.velimir_gurguriev.dentalclinic.adapters.DentistAdapter
import com.velimir_gurguriev.dentalclinic.databinding.FragmentDentistsBinding
import com.velimir_gurguriev.dentalclinic.models.User
import com.velimir_gurguriev.dentalclinic.repositories.UserRepository
import com.velimir_gurguriev.dentalclinic.services.dentists.DentistService

class DentistsFragment : Fragment() {

    private lateinit var binding: FragmentDentistsBinding

    private lateinit var dentistService: DentistService

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
        dentistService = DentistService(userRepository)
    }

    private fun setupRecyclerView() {
        binding.dentistsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
    }

    private fun loadDentists() {
        dentistService.getAllDentists(
            { dentists ->
                binding.dentistsRecyclerView.adapter =
                    DentistAdapter(dentists) { dentist ->
                        openDentistDetails(dentist)
                    }
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

    private fun openDentistDetails(dentist: User) {

        val bundle = Bundle().apply {
            putString("dentistUid", dentist.uid)
        }

        findNavController().navigate(
            R.id.action_dentistFragment_to_dentistDetailsFragment,
            bundle
        )
    }
}