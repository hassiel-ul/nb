package com.hassiel.rideadvisor.repository

import android.content.Context
import com.hassiel.rideadvisor.data.HistoryEntry
import com.hassiel.rideadvisor.data.RideResult
import com.hassiel.rideadvisor.db.RideDatabase
import com.hassiel.rideadvisor.db.RideHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio de historial (sección 13). Traduce entre el resultado del
 * motor de filtros ([RideResult]) y la persistencia local en Room.
 */
class RideRepository(context: Context) {

    private val dao = RideDatabase.getInstance(context).rideHistoryDao()

    suspend fun recordResult(result: RideResult) {
        val entity = when (result) {
            is RideResult.Accepted -> RideHistoryEntity(
                app = result.request.app,
                price = result.request.price,
                distance = result.request.distance,
                timestamp = result.request.timestamp,
                accepted = true,
                rejectionReason = null,
                rawText = result.request.rawText
            )
            is RideResult.Rejected -> RideHistoryEntity(
                app = result.request.app,
                price = result.request.price,
                distance = result.request.distance,
                timestamp = result.request.timestamp,
                accepted = false,
                rejectionReason = result.reason.name,
                rawText = result.request.rawText
            )
        }
        dao.insert(entity)
    }

    fun observeHistory(): Flow<List<HistoryEntry>> = dao.observeAll().map { list ->
        list.map {
            HistoryEntry(
                id = it.id,
                app = it.app,
                price = it.price,
                distance = it.distance,
                timestamp = it.timestamp,
                accepted = it.accepted,
                rejectionReason = it.rejectionReason
            )
        }
    }

    fun observeTotalCount(): Flow<Int> = dao.observeTotalCount()
    fun observeAcceptedCount(): Flow<Int> = dao.observeAcceptedCount()
    fun observeRejectedCount(): Flow<Int> = dao.observeRejectedCount()

    suspend fun clearHistory() = dao.clearAll()
}
