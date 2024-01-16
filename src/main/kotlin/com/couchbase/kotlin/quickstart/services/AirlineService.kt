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

    fun listAirlines(country: String? = null, limit: Int = 10, offset: Int = 0,): List<Airline> {
        return repository.list(country, limit, offset)
    }

    fun getAirlineById(id: String): Airline = repository.getById(id)

    fun toAirport(airport: String, limit: Int = 10, offset: Int = 0): List<Airline> {
        return repository.toAirport(airport, limit, offset)
    }
}