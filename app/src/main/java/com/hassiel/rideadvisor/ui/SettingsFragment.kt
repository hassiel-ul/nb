package com.hassiel.rideadvisor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hassiel.rideadvisor.RideAdvisorApplication
import com.hassiel.rideadvisor.databinding.FragmentSettingsBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Pantalla de configuración de filtros y comportamiento (secciones 5 y 10).
 * Los valores se guardan localmente mediante DataStore mientras el usuario
 * los modifica.
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as RideAdvisorApplication }
    private var isRestoringState = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val prefs = app.preferencesRepository.preferencesFlow.first()

            binding.inputMinPrice.setText(prefs.minimumPrice.toInt().toString())
            binding.inputMaxDistance.setText(prefs.maximumDistance.toInt().toString())
            binding.chipUber.isChecked = "Uber" in prefs.allowedApps
            binding.chipIndrive.isChecked = "inDrive" in prefs.allowedApps
            binding.chipDidi.isChecked = "DiDi" in prefs.allowedApps
            binding.switchRadar.isChecked = prefs.radarEnabled
            binding.switchOverlay.isChecked = prefs.overlayEnabled
            binding.switchSound.isChecked = prefs.soundEnabled
            binding.switchVibration.isChecked = prefs.vibrationEnabled
            binding.switchAutoMode.isChecked = prefs.autoModeEnabled

            isRestoringState = false
        }

        binding.inputMinPrice.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) persistMinPrice()
        }
        binding.inputMaxDistance.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) persistMaxDistance()
        }

        val appsListener = { _: View, _: Boolean -> persistAllowedApps() }
        binding.chipUber.setOnCheckedChangeListener(appsListener)
        binding.chipIndrive.setOnCheckedChangeListener(appsListener)
        binding.chipDidi.setOnCheckedChangeListener(appsListener)

        binding.switchRadar.setOnCheckedChangeListener { _, checked ->
            if (!isRestoringState) viewLifecycleOwner.lifecycleScope.launch {
                app.preferencesRepository.updateRadarEnabled(checked)
            }
        }
        binding.switchOverlay.setOnCheckedChangeListener { _, checked ->
            if (!isRestoringState) viewLifecycleOwner.lifecycleScope.launch {
                app.preferencesRepository.updateOverlayEnabled(checked)
            }
        }
        binding.switchSound.setOnCheckedChangeListener { _, checked ->
            if (!isRestoringState) viewLifecycleOwner.lifecycleScope.launch {
                app.preferencesRepository.updateSoundEnabled(checked)
            }
        }
        binding.switchVibration.setOnCheckedChangeListener { _, checked ->
            if (!isRestoringState) viewLifecycleOwner.lifecycleScope.launch {
                app.preferencesRepository.updateVibrationEnabled(checked)
            }
        }
        binding.switchAutoMode.setOnCheckedChangeListener { _, checked ->
            if (!isRestoringState) viewLifecycleOwner.lifecycleScope.launch {
                app.preferencesRepository.updateAutoModeEnabled(checked)
            }
        }
    }

    private fun persistMinPrice() {
        val value = binding.inputMinPrice.text?.toString()?.toDoubleOrNull() ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            app.preferencesRepository.updateMinimumPrice(value)
        }
    }

    private fun persistMaxDistance() {
        val value = binding.inputMaxDistance.text?.toString()?.toDoubleOrNull() ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            app.preferencesRepository.updateMaximumDistance(value)
        }
    }

    private fun persistAllowedApps() {
        if (isRestoringState) return
        val selected = mutableSetOf<String>()
        if (binding.chipUber.isChecked) selected.add("Uber")
        if (binding.chipIndrive.isChecked) selected.add("inDrive")
        if (binding.chipDidi.isChecked) selected.add("DiDi")
        viewLifecycleOwner.lifecycleScope.launch {
            app.preferencesRepository.updateAllowedApps(selected)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
