package com.hassiel.rideadvisor.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RideHistoryEntity::class], version = 1, exportSchema = false)
abstract class RideDatabase : RoomDatabase() {

    abstract fun rideHistoryDao(): RideHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: RideDatabase? = null

        fun getInstance(context: Context): RideDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RideDatabase::class.java,
                    "rideadvisor.db"
                ).fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
