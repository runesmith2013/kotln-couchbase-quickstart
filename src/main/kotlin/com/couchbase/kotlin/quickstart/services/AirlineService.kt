package com.couchbase.kotlin.quickstart.services

import com.couchbase.kotlin.quickstart.models.*
import com.couchbase.kotlin.quickstart.repositories.AirlineRepository

class AirlineService(repo: AirlineRepository) {

    private val repository = repo

    fun createAirline(data: AirlineModel, id: String): Airline {
        return repository.create(data, id)
    }

    fun updateAirline(airline: Airline, id: String) {
        repository.update(airline, id)
    }

    fun deleteAirline(id: String) {
        repository.delete(id)
    }

    fun listAirlines(country: String? = null, offset: Int = 0, limit: Int = 10): List<Airline> {
        return repository.list(country, offset, limit)
    }

    fun getAirlineById(id: String): Airline = repository.getById(id)

    fun toAirport(airport: String, offset: Int = 0, limit: Int = 10): List<Airline> {
        return repository.toAirport(airport, offset, limit)
    }
}