package com.hassiel.rideadvisor.utils

object Constants {
    // Paquetes Android de las apps de transporte soportadas.
    // Se pueden agregar más sin modificar el resto del sistema.
    val SUPPORTED_PACKAGES: Map<String, String> = mapOf(
        "com.ubercab.driver" to "Uber",
        "com.ubercab" to "Uber",
        "com.indriver.driver" to "inDrive",
        "sinet.startup.inDriver" to "inDrive",
        "com.didiglobal.driver" to "DiDi",
        "com.didi.driver" to "DiDi"
    )

    const val ACTION_RIDE_ACCEPTED = "com.hassiel.rideadvisor.RIDE_ACCEPTED"
    const val ACTION_RIDE_REJECTED = "com.hassiel.rideadvisor.RIDE_REJECTED"
    const val EXTRA_RIDE_REQUEST = "extra_ride_request"
    const val EXTRA_REJECTION_REASON = "extra_rejection_reason"

    const val OVERLAY_AUTO_DISMISS_MS = 15_000L
    const val NOTIFICATION_DEDUPE_WINDOW_MS = 4_000L

    const val PREFS_DATASTORE_NAME = "rideadvisor_preferences"
}
