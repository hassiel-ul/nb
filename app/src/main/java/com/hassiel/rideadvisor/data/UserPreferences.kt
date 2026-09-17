package com.hassiel.rideadvisor.data

/**
 * Preferencias de filtrado y comportamiento configuradas por el usuario.
 * Persistidas mediante DataStore (ver [com.hassiel.rideadvisor.repository.PreferencesRepository]).
 *
 * El diseño deja espacio para futuros criterios (precio por km, ganancia estimada,
 * horarios, tipo de viaje, etc.) sin romper la API existente.
 */
data class UserPreferences(
    val minimumPrice: Double = 300.0,
    val maximumDistance: Double = 10.0,
    val allowedApps: Set<String> = setOf("Uber", "inDrive", "DiDi"),
    val radarEnabled: Boolean = true,
    val overlayEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val autoModeEnabled: Boolean = false,
    val onboardingCompleted: Boolean = false,

    // Espacio reservado para criterios futuros (sección 5 del documento de requisitos):
    val minimumPricePerKm: Double? = null,
    val minimumEstimatedProfit: Double? = null,
    val minimumDistance: Double? = null,
    val activeHoursStart: Int? = null, // hora del día 0-23
    val activeHoursEnd: Int? = null
)
