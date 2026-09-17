package com.hassiel.rideadvisor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hassiel.rideadvisor.RideAdvisorApplication
import com.hassiel.rideadvisor.databinding.FragmentHistoryBinding
import kotlinx.coroutines.launch

/** Pantalla "Historial" (sección 13): lista de solicitudes procesadas. */
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as RideAdvisorApplication }
    private val adapter = HistoryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHistory.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            app.rideRepository.observeHistory().collect { entries ->
                adapter.submitList(entries)
                binding.textEmptyHistory.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
