package com.couchbase.kotlin.quickstart.models

import com.couchbase.client.core.deps.com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.couchbase.kotlin.quickstart.repositories.RouteRepository
import com.couchbase.kotlin.quickstart.services.RouteService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.lang.IllegalArgumentException

// This class is used to represent
// client requests to create a new
// Route record
@JsonIgnoreProperties(ignoreUnknown = true)
open class RouteModel (
    var airline: String? = null,
    var airlineid: String? = null,
    var destinationairport: String? = null,
    var distance: Double? = 0.0,
    var equipment: String? = null,
    var schedule: List<Schedule>? = null,
    var sourceairport: String? = null,
    var stops: Int? = 0
) {
    fun validate() {
        if (airline.isNullOrBlank() || airlineid.isNullOrBlank() || destinationairport.isNullOrBlank() || sourceairport.isNullOrBlank()) {
            throw IllegalArgumentException()
        }
    }
}

open class Schedule (
    var day: Int = 0,
    var flight: String = "",
    var utc: String = ""
)

// This class is used to represent
// Route records
class Route : RouteModel()

val routeModule = module {
    singleOf(::RouteRepository)
    singleOf(::RouteService)
}