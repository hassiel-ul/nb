package com.hassiel.rideadvisor.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hassiel.rideadvisor.RideAdvisorApplication
import com.hassiel.rideadvisor.databinding.ActivitySplashBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Pantalla de bienvenida (sección 12, pantalla 1). */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as RideAdvisorApplication

        lifecycleScope.launch {
            val prefs = app.preferencesRepository.preferencesFlow.first()

            Handler(Looper.getMainLooper()).postDelayed({
                val destination = if (prefs.onboardingCompleted) {
                    Intent(this@SplashActivity, MainActivity::class.java)
                } else {
                    Intent(this@SplashActivity, OnboardingActivity::class.java)
                }
                startActivity(destination)
                finish()
            }, SPLASH_DELAY_MS)
        }
    }

    companion object {
        private const val SPLASH_DELAY_MS = 1200L
    }
}
