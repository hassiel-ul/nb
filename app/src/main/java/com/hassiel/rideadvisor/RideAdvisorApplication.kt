package com.hassiel.rideadvisor

import android.app.Application
import com.hassiel.rideadvisor.repository.PreferencesRepository
import com.hassiel.rideadvisor.repository.RideRepository

/**
 * Clase Application: crea instancias únicas (singletons ligeros) de los
 * repositorios para que todo el proyecto (Activities, Fragments, Services)
 * comparta el mismo acceso a datos.
 */
class RideAdvisorApplication : Application() {

    lateinit var preferencesRepository: PreferencesRepository
        private set

    lateinit var rideRepository: RideRepository
        private set

    override fun onCreate() {
        super.onCreate()
        preferencesRepository = PreferencesRepository(this)
        rideRepository = RideRepository(this)
    }
}
