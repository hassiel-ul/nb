package com.hassiel.rideadvisor.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hassiel.rideadvisor.R
import com.hassiel.rideadvisor.RideAdvisorApplication
import com.hassiel.rideadvisor.databinding.ActivityOnboardingBinding
import com.hassiel.rideadvisor.utils.PermissionManager
import kotlinx.coroutines.launch

/**
 * Asistente de configuración inicial (sección 20 del documento de
 * requisitos): 6 pasos guiados antes de dejar operar a RideAdvisor.
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private var currentStep = 0
    private val totalSteps = 6

    private val app by lazy { application as RideAdvisorApplication }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAction.setOnClickListener { onActionClicked() }
        renderStep()
    }

    override fun onResume() {
        super.onResume()
        // Al volver de Ajustes del sistema, refrescamos el estado del permiso.
        renderStep()
    }

    private fun onActionClicked() {
        when (currentStep) {
            0 -> {
                if (PermissionManager.isNotificationListenerEnabled(this)) {
                    advance()
                } else {
                    PermissionManager.openNotificationListenerSettings(this)
                }
            }
            1 -> {
                if (PermissionManager.canDrawOverlays(this)) {
                    advance()
                } else {
                    PermissionManager.openOverlaySettings(this)
                }
            }
            2 -> {
                val value = binding.inputStepValue.text?.toString()?.toDoubleOrNull() ?: 300.0
                lifecycleScope.launch {
                    app.preferencesRepository.updateMinimumPrice(value)
                    advance()
                }
            }
            3 -> {
                val value = binding.inputStepValue.text?.toString()?.toDoubleOrNull() ?: 10.0
                lifecycleScope.launch {
                    app.preferencesRepository.updateMaximumDistance(value)
                    advance()
                }
            }
            4 -> {
                val selected = mutableSetOf<String>()
                if (binding.stepChipUber.isChecked) selected.add("Uber")
                if (binding.stepChipIndrive.isChecked) selected.add("inDrive")
                if (binding.stepChipDidi.isChecked) selected.add("DiDi")
                lifecycleScope.launch {
                    app.preferencesRepository.updateAllowedApps(selected)
                    advance()
                }
            }
            5 -> {
                lifecycleScope.launch {
                    app.preferencesRepository.setOnboardingCompleted(true)
                    startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun advance() {
        if (currentStep < totalSteps - 1) {
            currentStep++
            renderStep()
        }
    }

    private fun renderStep() {
        // Ocultar todo por defecto y mostrar solo lo relevante al paso actual.
        binding.inputLayoutValue.visibility = View.GONE
        binding.stepAppsGroup.visibility = View.GONE
        binding.textPermissionStatus.visibility = View.GONE
        binding.textStepExtra.text = ""

        when (currentStep) {
            0 -> {
                binding.textStepLabel.text = getString(R.string.onboarding_step1_title)
                binding.textStepTitle.text = getString(R.string.onboarding_step1_desc)
                binding.textStepExtra.text = getString(R.string.permission_notification_desc)
                showPermissionState(PermissionManager.isNotificationListenerEnabled(this))
                binding.btnAction.text = if (PermissionManager.isNotificationListenerEnabled(this))
                    getString(R.string.onboarding_next) else getString(R.string.permission_enable_button)
            }
            1 -> {
                binding.textStepLabel.text = getString(R.string.onboarding_step2_title)
                binding.textStepTitle.text = getString(R.string.onboarding_step2_desc)
                binding.textStepExtra.text = getString(R.string.permission_overlay_desc)
                showPermissionState(PermissionManager.canDrawOverlays(this))
                binding.btnAction.text = if (PermissionManager.canDrawOverlays(this))
                    getString(R.string.onboarding_next) else getString(R.string.permission_enable_button)
            }
            2 -> {
                binding.textStepLabel.text = getString(R.string.onboarding_step3_title)
                binding.textStepTitle.text = getString(R.string.onboarding_step3_desc)
                binding.inputLayoutValue.visibility = View.VISIBLE
                binding.inputStepValue.setText("300")
                binding.btnAction.text = getString(R.string.onboarding_next)
            }
            3 -> {
                binding.textStepLabel.text = getString(R.string.onboarding_step4_title)
                binding.textStepTitle.text = getString(R.string.onboarding_step4_desc)
                binding.inputLayoutValue.visibility = View.VISIBLE
                binding.inputStepValue.setText("10")
                binding.btnAction.text = getString(R.string.onboarding_next)
            }
            4 -> {
                binding.textStepLabel.text = getString(R.string.onboarding_step5_title)
                binding.textStepTitle.text = getString(R.string.onboarding_step5_desc)
                binding.stepAppsGroup.visibility = View.VISIBLE
                binding.btnAction.text = getString(R.string.onboarding_next)
            }
            5 -> {
                binding.textStepLabel.text = getString(R.string.onboarding_step6_title)
                binding.textStepTitle.text = getString(R.string.onboarding_ready)
                binding.btnAction.text = getString(R.string.onboarding_finish)
            }
        }

        renderDots()
    }

    private fun showPermissionState(granted: Boolean) {
        binding.textPermissionStatus.visibility = if (granted) View.VISIBLE else View.GONE
        binding.textPermissionStatus.text = getString(R.string.permission_granted)
    }

    private fun renderDots() {
        binding.dotsContainer.removeAllViews()
        for (i in 0 until totalSteps) {
            val dot = View(this)
            val size = (8 * resources.displayMetrics.density).toInt()
            val margin = (4 * resources.displayMetrics.density).toInt()
            val params = android.widget.LinearLayout.LayoutParams(size, size)
            params.setMargins(margin, 0, margin, 0)
            dot.layoutParams = params
            dot.setBackgroundResource(
                if (i == currentStep) R.drawable.bg_pill_accepted else R.drawable.bg_card
            )
            binding.dotsContainer.addView(dot)
        }
    }
}
