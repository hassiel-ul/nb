package com.hassiel.rideadvisor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hassiel.rideadvisor.R
import com.hassiel.rideadvisor.RideAdvisorApplication
import com.hassiel.rideadvisor.databinding.FragmentDashboardBinding
import com.hassiel.rideadvisor.utils.PermissionManager
import kotlinx.coroutines.launch

/** Pantalla principal con estado del sistema y estadísticas (sección 9). */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as RideAdvisorApplication }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnFixPermissions.setOnClickListener {
            if (!PermissionManager.isNotificationListenerEnabled(requireContext())) {
                PermissionManager.openNotificationListenerSettings(requireContext())
            } else if (!PermissionManager.canDrawOverlays(requireContext())) {
                PermissionManager.openOverlaySettings(requireContext())
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            app.rideRepository.observeTotalCount().collect {
                binding.textDetectedCount.text = it.toString()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            app.rideRepository.observeAcceptedCount().collect {
                binding.textAcceptedCount.text = it.toString()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            app.rideRepository.observeRejectedCount().collect {
                binding.textRejectedCount.text = it.toString()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        val listenerOk = PermissionManager.isNotificationListenerEnabled(requireContext())
        val overlayOk = PermissionManager.canDrawOverlays(requireContext())

        val greenDot = R.drawable.bg_pill_accepted
        val redDot = R.drawable.bg_pill_rejected

        binding.dotService.setBackgroundResource(if (listenerOk && overlayOk) greenDot else redDot)
        binding.dotListening.setBackgroundResource(if (listenerOk) greenDot else redDot)
        binding.dotRadar.setBackgroundResource(if (overlayOk) greenDot else redDot)

        binding.cardPermissions.visibility = if (!listenerOk || !overlayOk) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
