package com.hassiel.rideadvisor.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hassiel.rideadvisor.R
import com.hassiel.rideadvisor.RideAdvisorApplication
import com.hassiel.rideadvisor.data.RideRequest
import com.hassiel.rideadvisor.databinding.FragmentRadarBinding
import com.hassiel.rideadvisor.utils.Constants
import com.hassiel.rideadvisor.utils.RejectionReasonText
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Pantalla "Radar IA" (sección 8). Muestra el estado de búsqueda en tiempo
 * real y la última solicitud detectada, aceptada o rechazada.
 */
class RadarFragment : Fragment() {

    private var _binding: FragmentRadarBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as RideAdvisorApplication }
    private val mainHandler = Handler(Looper.getMainLooper())

    private val resultReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val request = intent.getSerializableExtra(Constants.EXTRA_RIDE_REQUEST) as? RideRequest
                ?: return

            when (intent.action) {
                Constants.ACTION_RIDE_ACCEPTED -> showResult(request, accepted = true, reasonName = null)
                Constants.ACTION_RIDE_REJECTED -> {
                    val reason = intent.getStringExtra(Constants.EXTRA_REJECTION_REASON)
                    showResult(request, accepted = false, reasonName = reason)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRadarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            app.preferencesRepository.preferencesFlow.collect { prefs ->
                if (prefs.radarEnabled) {
                    binding.radarView.start()
                    binding.textRadarState.text = getString(R.string.radar_scanning)
                    binding.textRadarStatus.text = getString(R.string.radar_status_scanning)
                } else {
                    binding.radarView.stop()
                    binding.textRadarStatus.text = getString(R.string.radar_status_paused)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction(Constants.ACTION_RIDE_ACCEPTED)
            addAction(Constants.ACTION_RIDE_REJECTED)
        }
        ContextCompat.registerReceiver(
            requireContext(), resultReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
        binding.radarView.start()
    }

    override fun onPause() {
        super.onPause()
        try {
            requireContext().unregisterReceiver(resultReceiver)
        } catch (e: IllegalArgumentException) {
            // El receptor ya estaba desregistrado; no es un error crítico.
        }
        // Sección 21: pausar el radar cuando la pantalla no es visible.
        binding.radarView.stop()
    }

    private fun showResult(request: RideRequest, accepted: Boolean, reasonName: String?) {
        binding.textRadarStatus.text = if (accepted) {
            getString(R.string.radar_status_detected)
        } else {
            getString(R.string.radar_status_discarded)
        }

        binding.cardLastRequest.visibility = View.VISIBLE
        binding.textLastApp.text = request.app
        binding.textLastPrice.text = request.price?.let {
            "\uD83D\uDCB0 " + String.format(Locale.getDefault(), "$%.0f", it)
        } ?: "\uD83D\uDCB0 --"
        binding.textLastDistance.text = request.distance?.let {
            "\uD83D\uDCCD " + String.format(Locale.getDefault(), "%.1f km", it)
        } ?: "\uD83D\uDCCD --"

        if (accepted) {
            binding.textLastResult.text = "\u2713 " + getString(R.string.overlay_accepted)
            binding.textLastResult.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.accent_green)
            )
        } else {
            val reasonText = RejectionReasonText.forName(requireContext(), reasonName)
                ?: getString(R.string.radar_status_discarded)
            binding.textLastResult.text = "\u2716 $reasonText"
            binding.textLastResult.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.accent_red)
            )
        }

        // Volver al estado de "buscando" tras unos segundos.
        mainHandler.postDelayed({
            if (_binding != null) {
                binding.textRadarStatus.text = getString(R.string.radar_status_scanning)
            }
        }, 4000L)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainHandler.removeCallbacksAndMessages(null)
        _binding = null
    }
}
