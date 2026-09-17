package com.hassiel.rideadvisor.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.hassiel.rideadvisor.R
import com.hassiel.rideadvisor.data.RideRequest
import com.hassiel.rideadvisor.data.UserPreferences
import com.hassiel.rideadvisor.databinding.OverlayRideRequestBinding
import com.hassiel.rideadvisor.ui.MainActivity
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Servicio en primer plano que dibuja el panel flotante sobre otras
 * aplicaciones (sección 7). No bloquea la app de transporte: el panel es
 * pequeño, movible, minimizable y se puede cerrar en cualquier momento.
 *
 * Sección 16: este servicio SOLO muestra información. No pulsa botones ni
 * realiza ninguna acción automática dentro de Uber, inDrive o DiDi.
 */
class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayBinding: OverlayRideRequestBinding? = null
    private val dismissHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var dismissRunnable: Runnable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundIfNeeded()

        val request = intent?.getSerializableExtra(EXTRA_REQUEST) as? RideRequest
        val soundEnabled = intent?.getBooleanExtra(EXTRA_SOUND, true) ?: true
        val vibrationEnabled = intent?.getBooleanExtra(EXTRA_VIBRATION, true) ?: true

        if (request != null) {
            showOverlay(request, soundEnabled, vibrationEnabled)
        }
        return START_NOT_STICKY
    }

    private fun startForegroundIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val contentIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.dashboard_radar_active))
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .build()

        try {
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo iniciar el servicio en primer plano: ${e.message}")
        }
    }

    private fun showOverlay(request: RideRequest, soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (!android.provider.Settings.canDrawOverlays(this)) {
            // Sección 19: si el overlay no puede mostrarse, no se rompe la app.
            Log.w(TAG, "Sin permiso de superposición; overlay omitido")
            stopSelf()
            return
        }

        removeExistingOverlay()

        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager = wm

        val binding = OverlayRideRequestBinding.inflate(LayoutInflater.from(this))
        overlayBinding = binding

        binding.textOverlayApp.text = request.app
        binding.textOverlayPrice.text = request.price?.let {
            String.format(Locale.getDefault(), "$%.0f", it)
        } ?: "--"
        binding.textOverlayDistance.text = request.distance?.let {
            String.format(Locale.getDefault(), "%.1f km", it)
        } ?: "--"
        binding.textOverlayResult.text = getString(R.string.overlay_accepted)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.END
        params.x = 24
        params.y = 160

        binding.btnOverlayClose.setOnClickListener { dismissOverlay() }
        binding.btnOverlayMinimize.setOnClickListener { minimizeOverlay(binding) }

        setupDrag(binding.root, params, wm)

        wm.addView(binding.root, params)

        if (soundEnabled) playAlertSound()
        if (vibrationEnabled) vibrateAlert()

        scheduleAutoDismiss()
    }

    /** Permite mover el panel arrastrándolo (sección 7: "moverlo si es posible"). */
    private fun setupDrag(view: View, params: WindowManager.LayoutParams, wm: WindowManager) {
        var initialX = 0
        var initialY = 0
        var touchX = 0f
        var touchY = 0f

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Gravity END: movimiento horizontal invertido.
                    params.x = (initialX - (event.rawX - touchX)).roundToInt()
                    params.y = (initialY + (event.rawY - touchY)).roundToInt()
                    try {
                        wm.updateViewLayout(v, params)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error moviendo overlay: ${e.message}")
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun minimizeOverlay(binding: OverlayRideRequestBinding) {
        val root = binding.root
        root.visibility = if (root.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun playAlertSound() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(applicationContext, uri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ringtone.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            }
            ringtone.play()
        } catch (e: Exception) {
            Log.e(TAG, "Error reproduciendo sonido: ${e.message}")
        }
    }

    private fun vibrateAlert() {
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(300)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrando: ${e.message}")
        }
    }

    private fun scheduleAutoDismiss() {
        dismissRunnable?.let { dismissHandler.removeCallbacks(it) }
        val runnable = Runnable { dismissOverlay() }
        dismissRunnable = runnable
        dismissHandler.postDelayed(runnable, com.hassiel.rideadvisor.utils.Constants.OVERLAY_AUTO_DISMISS_MS)
    }

    private fun dismissOverlay() {
        removeExistingOverlay()
        stopSelf()
    }

    private fun removeExistingOverlay() {
        dismissRunnable?.let { dismissHandler.removeCallbacks(it) }
        val binding = overlayBinding
        if (binding != null) {
            try {
                windowManager?.removeView(binding.root)
            } catch (e: Exception) {
                Log.e(TAG, "Error removiendo overlay previo: ${e.message}")
            }
            overlayBinding = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeExistingOverlay()
    }

    companion object {
        private const val TAG = "OverlayService"
        private const val CHANNEL_ID = "rideadvisor_overlay_channel"
        private const val NOTIFICATION_ID = 8801
        private const val EXTRA_REQUEST = "extra_request"
        private const val EXTRA_SOUND = "extra_sound"
        private const val EXTRA_VIBRATION = "extra_vibration"

        /** Punto de entrada usado por [RideNotificationService] para mostrar una solicitud. */
        fun showRequest(context: Context, request: RideRequest, preferences: UserPreferences) {
            if (!android.provider.Settings.canDrawOverlays(context)) return

            val intent = Intent(context, OverlayService::class.java).apply {
                putExtra(EXTRA_REQUEST, request)
                putExtra(EXTRA_SOUND, preferences.soundEnabled)
                putExtra(EXTRA_VIBRATION, preferences.vibrationEnabled)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
