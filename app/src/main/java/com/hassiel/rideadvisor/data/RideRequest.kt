package com.hassiel.rideadvisor.data

import java.io.Serializable

/**
 * Representa una solicitud de viaje extraída de una notificación.
 *
 * @property app Nombre normalizado de la app de origen (Uber, inDrive, DiDi, etc.).
 * @property price Precio detectado, o null si no se pudo extraer.
 * @property distance Distancia en km detectada, o null si no se pudo extraer.
 * @property rawText Texto original de la notificación (para depuración/histórico).
 * @property packageName Paquete Android de la app que generó la notificación.
 * @property timestamp Momento en el que se detectó la notificación.
 * @property notificationKey Clave única de la notificación, usada para evitar duplicados.
 */
data class RideRequest(
    val app: String,
    val price: Double?,
    val distance: Double?,
    val rawText: String,
    val packageName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val notificationKey: String
) : Serializable
