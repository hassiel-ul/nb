package com.hassiel.rideadvisor.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RideHistoryDao {

    @Insert
    suspend fun insert(entry: RideHistoryEntity): Long

    @Query("SELECT * FROM ride_history ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<RideHistoryEntity>>

    @Query("SELECT COUNT(*) FROM ride_history")
    fun observeTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ride_history WHERE accepted = 1")
    fun observeAcceptedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ride_history WHERE accepted = 0")
    fun observeRejectedCount(): Flow<Int>

    @Query("DELETE FROM ride_history")
    suspend fun clearAll()
}
