package com.hassiel.rideadvisor.data

/**
 * Modelo de UI para una entrada del historial. Se construye a partir de
 * [com.hassiel.rideadvisor.db.RideHistoryEntity].
 */
data class HistoryEntry(
    val id: Long,
    val app: String,
    val price: Double?,
    val distance: Double?,
    val timestamp: Long,
    val accepted: Boolean,
    val rejectionReason: String?
)
