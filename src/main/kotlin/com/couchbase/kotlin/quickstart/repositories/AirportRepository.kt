package com.couchbase.kotlin.quickstart.repositories

import com.couchbase.client.kotlin.Scope
import com.couchbase.client.kotlin.query.execute
import com.couchbase.kotlin.quickstart.models.Airport
import com.couchbase.kotlin.quickstart.models.AirportModel
import com.couchbase.kotlin.quickstart.models.DestinationAirport
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

class AirportRepository (scope: Scope) {
    private val collection = scope.collection("airport")
    @OptIn(DelicateCoroutinesApi::class)
    private val databaseContext = newSingleThreadContext("CouchbaseThread")

    fun update(airport: Airport, id: String): Airport {
        runBlocking(databaseContext) {
            collection.replace(id, airport)
        }
        return airport
    }

    fun delete(id: String) {
        runBlocking(databaseContext) {
            collection.remove(id);
        }
    }

    fun create(data: AirportModel, id: String): Airport {
        val airport = Airport().apply {
            airportname = data.airportname
            city = data.city
            country = data.country
            faa = data.faa
            geo = data.geo
            icao = data.icao
            tz = data.tz
        }

        runBlocking(databaseContext) {
            collection.insert(id, airport)
        }
        return airport
    }

    fun getById(id: String): Airport {
        var result: Airport
        runBlocking(databaseContext) {
            result = collection.get(id).contentAs()
        }
        return result
    }

    fun list(country: String? = null, offset: Int = 0, limit: Int = 10): List<Airport> {
        val lowerCountry = country?.lowercase()
        val query = if (!lowerCountry.isNullOrEmpty()) {
            """
        SELECT airport.airportname,
               airport.city,
               airport.country,
               airport.faa,
               airport.geo,
               airport.icao,
               airport.tz
        FROM airport AS airport
        WHERE lower(airport.country) = '$lowerCountry'
        ORDER BY airport.airportname
        LIMIT $limit
        OFFSET $offset
        """
        } else {
            """
        SELECT airport.airportname,
               airport.city,
               airport.country,
               airport.faa,
               airport.geo,
               airport.icao,
               airport.tz
        FROM airport AS airport
        ORDER BY airport.airportname
        LIMIT $limit
        OFFSET $offset
        """
        }

        return runBlocking(databaseContext) {
            val result = collection.scope.query(query, readonly = true).execute()
            result.rows.map {
                it.contentAs<Airport>()
            }.toList()
        }
    }

    fun directDestinations(airport: String, offset: Int = 0, limit: Int = 10): List<DestinationAirport> {
        val lowerAirport = airport.lowercase()
        val query =
            """
                SELECT DISTINCT route.destinationairport
                FROM airport AS airport
                JOIN route AS route
                ON route.sourceairport = airport.faa
                WHERE lower(airport.faa) = '$lowerAirport' AND route.stops = 0
                ORDER BY route.destinationairport
                LIMIT $limit
                OFFSET $offset
            """

        return runBlocking(databaseContext) {
            val result = collection.scope.query(query, readonly = true).execute()
            result.rows.map {
                it.contentAs<DestinationAirport>()
            }.toList()
        }
    }

}