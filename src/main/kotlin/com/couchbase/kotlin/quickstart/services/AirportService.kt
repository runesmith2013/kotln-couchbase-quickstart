package com.couchbase.kotlin.quickstart.services

import com.couchbase.kotlin.quickstart.models.Airport
import com.couchbase.kotlin.quickstart.models.AirportModel
import com.couchbase.kotlin.quickstart.models.DestinationAirport
import com.couchbase.kotlin.quickstart.repositories.AirportRepository

class AirportService(repo: AirportRepository) {
    private val repository = repo

    fun createAirport(data: AirportModel, id: String): Airport {
        return repository.create(data, id)
    }

    fun updateAirport(airport: Airport, id: String) {
        repository.update(airport, id)
    }

    fun deleteAirport(id: String) {
        repository.delete(id)
    }

    fun listAirports(country: String? = null, offset: Int = 0, limit: Int = 10): List<Airport> {
        return repository.list(country, offset, limit)
    }

    fun getAirportById(id: String): Airport = repository.getById(id)

    fun directDestinations(airport: String, offset: Int = 0, limit: Int = 10): List<DestinationAirport> {
        return repository.directDestinations(airport, offset, limit)
    }
}