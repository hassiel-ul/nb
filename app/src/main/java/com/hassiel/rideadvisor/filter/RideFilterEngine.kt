package com.hassiel.rideadvisor.filter

import com.hassiel.rideadvisor.data.RideRequest
import com.hassiel.rideadvisor.data.RideResult
import com.hassiel.rideadvisor.data.UserPreferences

/**
 * Motor de filtrado independiente (sección 6 del documento de requisitos).
 *
 * Recibe una [RideRequest] y las [UserPreferences] vigentes, y devuelve
 * [RideResult.Accepted] o [RideResult.Rejected].
 *
 * Ejemplo:
 *   val engine = RideFilterEngine()
 *   val result = engine.evaluate(request, preferences)
 *
 * El motor construye su lista de criterios dinámicamente a partir de las
 * preferencias, de modo que agregar un nuevo criterio (ver [FilterCriteria])
 * no requiere modificar el resto del proyecto: basta con añadirlo a
 * [buildCriteria].
 */
class RideFilterEngine {

    fun evaluate(request: RideRequest, preferences: UserPreferences): RideResult {
        val criteria = buildCriteria(preferences)

        for (criterion in criteria) {
            val rejection = criterion.evaluate(request)
            if (rejection != null) {
                return RideResult.Rejected(request, rejection)
            }
        }

        return RideResult.Accepted(request)
    }

    private fun buildCriteria(preferences: UserPreferences): List<FilterCriterion> {
        val criteria = mutableListOf<FilterCriterion>(
            AllowedAppCriterion(preferences.allowedApps),
            MinimumPriceCriterion(preferences.minimumPrice),
            MaximumDistanceCriterion(preferences.maximumDistance)
        )

        // Espacio para criterios futuros, activados solo si el usuario los configura:
        // preferences.minimumPricePerKm?.let { criteria.add(MinimumPricePerKmCriterion(it)) }
        // preferences.minimumEstimatedProfit?.let { criteria.add(MinimumProfitCriterion(it)) }
        // preferences.minimumDistance?.let { criteria.add(MinimumDistanceCriterion(it)) }

        return criteria
    }
}
