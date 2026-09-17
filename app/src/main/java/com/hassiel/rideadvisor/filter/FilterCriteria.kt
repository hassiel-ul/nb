package com.hassiel.rideadvisor.filter

import com.hassiel.rideadvisor.data.RideRequest
import com.hassiel.rideadvisor.data.RejectionReason

/**
 * Contrato para un criterio de filtrado individual. Cada criterio evalúa una
 * [RideRequest] de forma independiente, lo que permite agregar nuevos
 * criterios (precio por km, ganancia estimada, horarios, etc. — sección 5)
 * sin modificar los criterios existentes ni el motor que los orquesta.
 */
interface FilterCriterion {
    /** Devuelve null si el criterio se cumple, o el motivo de rechazo si no. */
    fun evaluate(request: RideRequest): RejectionReason?
}

/** Rechaza solicitudes cuyo precio esté por debajo del mínimo configurado. */
class MinimumPriceCriterion(private val minimumPrice: Double) : FilterCriterion {
    override fun evaluate(request: RideRequest): RejectionReason? {
        val price = request.price ?: return RejectionReason.INCOMPLETE_DATA
        return if (price < minimumPrice) RejectionReason.PRICE_TOO_LOW else null
    }
}

/** Rechaza solicitudes cuya distancia supere la máxima configurada. */
class MaximumDistanceCriterion(private val maximumDistance: Double) : FilterCriterion {
    override fun evaluate(request: RideRequest): RejectionReason? {
        val distance = request.distance ?: return RejectionReason.INCOMPLETE_DATA
        return if (distance > maximumDistance) RejectionReason.DISTANCE_TOO_HIGH else null
    }
}

/** Rechaza solicitudes provenientes de apps no habilitadas por el usuario. */
class AllowedAppCriterion(private val allowedApps: Set<String>) : FilterCriterion {
    override fun evaluate(request: RideRequest): RejectionReason? {
        return if (request.app !in allowedApps) RejectionReason.APP_NOT_ALLOWED else null
    }
}
