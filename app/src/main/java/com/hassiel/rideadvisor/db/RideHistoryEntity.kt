package com.hassiel.rideadvisor.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que persiste cada solicitud procesada, sea aceptada o rechazada.
 */
@Entity(tableName = "ride_history")
data class RideHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val app: String,
    val price: Double?,
    val distance: Double?,
    val timestamp: Long,
    val accepted: Boolean,
    val rejectionReason: String?,
    val rawText: String
)
