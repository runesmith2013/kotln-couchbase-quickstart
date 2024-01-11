package com.couchbase.kotlin.quickstart.repositories

import com.couchbase.client.kotlin.Scope
import com.couchbase.kotlin.quickstart.models.Airline
import com.couchbase.kotlin.quickstart.models.AirlineModel
import com.couchbase.kotlin.quickstart.models.Route
import com.couchbase.kotlin.quickstart.models.RouteModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

class RouteRepository(scope: Scope) {

    private val collection = scope.collection("route")
    @OptIn(DelicateCoroutinesApi::class)
    private val databaseContext = newSingleThreadContext("CouchbaseThread")

    fun update(route: Route, id: String): Route {
        runBlocking(databaseContext) {
            collection.replace(id, route)
        }
        return route
    }

    fun delete(id: String) {
        runBlocking(databaseContext) {
            collection.remove(id);
        }
    }

    fun create(data: RouteModel, id: String): Route {
        val route = Route().apply {
            airline = data.airline
            airlineid = data.airlineid
            destinationairport = data.destinationairport
            distance = data.distance
            equipment = data.equipment
            schedule = data.schedule
            sourceairport = data.sourceairport
            stops = data.stops
        }

        runBlocking(databaseContext) {
            collection.insert(id, route)
        }
        return route
    }

    fun getById(id: String): Route {
        var result: Route
        runBlocking(databaseContext) {
            result = collection.get(id).contentAs()
        }
        return result
    }
}