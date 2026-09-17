package com.hassiel.rideadvisor.service

import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.hassiel.rideadvisor.data.RideRequest
import com.hassiel.rideadvisor.data.RideResult
import com.hassiel.rideadvisor.filter.RideFilterEngine
import com.hassiel.rideadvisor.repository.PreferencesRepository
import com.hassiel.rideadvisor.repository.RideRepository
import com.hassiel.rideadvisor.utils.Constants
import com.hassiel.rideadvisor.utils.NotificationParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Servicio de escucha de notificaciones (sección 4 del documento de
 * requisitos). Detecta únicamente las apps configuradas en
 * [Constants.SUPPORTED_PACKAGES], extrae la información con
 * [NotificationParser], evalúa la solicitud con [RideFilterEngine] y, si
 * corresponde, dispara el overlay a través de [OverlayService].
 *
 * Este servicio NUNCA interactúa con las apps de terceros (sección 16):
 * solo lee el contenido textual de sus notificaciones.
 */
class RideNotificationService : NotificationListenerService() {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var filterEngine: RideFilterEngine
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var rideRepository: RideRepository

    // Evita procesar la misma notificación más de una vez en una ventana corta.
    private val recentKeys = LinkedHashMap<String, Long>()

    override fun onCreate() {
        super.onCreate()
        filterEngine = RideFilterEngine()
        preferencesRepository = PreferencesRepository(applicationContext)
        rideRepository = RideRepository(applicationContext)
        Log.i(TAG, "RideNotificationService creado")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        try {
            handleNotification(sbn)
        } catch (e: Exception) {
            // Sección 19: la app nunca debe cerrarse por un error de procesamiento.
            Log.e(TAG, "Error procesando notificación: ${e.message}", e)
        }
    }

    private fun handleNotification(sbn: StatusBarNotification) {
        val packageName = sbn.packageName ?: return

        // Solo procesamos apps configuradas; todo lo demás se ignora sin costo.
        if (!Constants.SUPPORTED_PACKAGES.containsKey(packageName)) return

        val text = extractText(sbn.notification) ?: return
        if (text.isBlank()) return

        val dedupeKey = NotificationParser.buildDedupeKey(packageName, text)
        if (isDuplicate(dedupeKey)) return

        val appName = NotificationParser.resolveAppName(packageName, text)
        val price = NotificationParser.extractPrice(text)
        val distance = NotificationParser.extractDistance(text)

        val request = RideRequest(
            app = appName,
            price = price,
            distance = distance,
            rawText = text,
            packageName = packageName,
            notificationKey = dedupeKey
        )

        scope.launch {
            try {
                val preferences = preferencesRepository.preferencesFlow.first()

                if (!preferences.radarEnabled) return@launch

                val result = filterEngine.evaluate(request, preferences)
                rideRepository.recordResult(result)
                broadcastResult(result)

                if (result is RideResult.Accepted && preferences.overlayEnabled) {
                    OverlayService.showRequest(applicationContext, result.request, preferences)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error evaluando solicitud: ${e.message}", e)
            }
        }
    }

    private fun broadcastResult(result: RideResult) {
        val intent = when (result) {
            is RideResult.Accepted -> Intent(Constants.ACTION_RIDE_ACCEPTED).apply {
                setPackage(packageName)
                putExtra(Constants.EXTRA_RIDE_REQUEST, result.request)
            }
            is RideResult.Rejected -> Intent(Constants.ACTION_RIDE_REJECTED).apply {
                setPackage(packageName)
                putExtra(Constants.EXTRA_RIDE_REQUEST, result.request)
                putExtra(Constants.EXTRA_REJECTION_REASON, result.reason.name)
            }
        }
        sendBroadcast(intent)
    }

    /**
     * Extrae el texto combinando título + contenido + textLines, ya que cada
     * app de transporte estructura sus notificaciones de forma distinta.
     */
    private fun extractText(notification: Notification?): String? {
        if (notification == null) return null
        val extras: Bundle = notification.extras ?: return null

        val builder = StringBuilder()
        (extras.getCharSequence(Notification.EXTRA_TITLE))?.let { builder.append(it).append("\n") }
        (extras.getCharSequence(Notification.EXTRA_TEXT))?.let { builder.append(it).append("\n") }
        (extras.getCharSequence(Notification.EXTRA_SUB_TEXT))?.let { builder.append(it).append("\n") }
        (extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES))?.forEach {
            builder.append(it).append("\n")
        }
        (extras.getCharSequence(Notification.EXTRA_BIG_TEXT))?.let { builder.append(it).append("\n") }

        val result = builder.toString().trim()
        return result.ifBlank { null }
    }

    private fun isDuplicate(key: String): Boolean {
        val now = System.currentTimeMillis()
        // Limpieza de entradas viejas.
        recentKeys.entries.removeAll { now - it.value > Constants.NOTIFICATION_DEDUPE_WINDOW_MS }

        val lastSeen = recentKeys[key]
        recentKeys[key] = now
        return lastSeen != null
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "Notification listener conectado")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "Notification listener desconectado")
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {
        private const val TAG = "RideNotificationService"
    }
}
