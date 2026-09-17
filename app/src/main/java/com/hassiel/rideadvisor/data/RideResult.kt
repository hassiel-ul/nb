package com.hassiel.rideadvisor.data

/**
 * Resultado de evaluar una [RideRequest] contra los filtros configurados.
 */
sealed class RideResult {
    data class Accepted(val request: RideRequest) : RideResult()
    data class Rejected(val request: RideRequest, val reason: RejectionReason) : RideResult()
}

/**
 * Motivos de rechazo. Diseñado para poder añadir nuevos motivos sin romper el
 * código existente (ver RideFilterEngine).
 */
enum class RejectionReason {
    PRICE_TOO_LOW,
    DISTANCE_TOO_HIGH,
    APP_NOT_ALLOWED,
    INCOMPLETE_DATA
}
