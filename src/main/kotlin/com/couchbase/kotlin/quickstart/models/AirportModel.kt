package com.couchbase.kotlin.quickstart.models

import com.couchbase.client.core.deps.com.fasterxml.jackson.annotation.JsonIgnore
import com.couchbase.client.core.deps.com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.couchbase.client.core.deps.com.fasterxml.jackson.annotation.JsonProperty
import com.couchbase.kotlin.quickstart.repositories.AirportRepository
import com.couchbase.kotlin.quickstart.services.AirportService

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.lang.IllegalArgumentException
import java.util.*

// This class is used to represent
// client requests to create a new
// Airport record
@JsonIgnoreProperties(ignoreUnknown = true)
open class AirportModel(
    var airportname: String? = null,
    var city: String? = null,
    var country: String? = null,
    var faa: String? = null,
    var geo: Geo? = null,
    var icao: String? = null,
    var tz: String? = null,

    @JsonIgnore
    var id: String? = null,

    @JsonIgnore
    var type: String? = null
) {
    fun validate() {
        if (airportname.isNullOrBlank() || city.isNullOrBlank() || country.isNullOrBlank() || faa.isNullOrBlank()) {
            throw IllegalArgumentException()
        }
    }
}

open class Geo (
    var alt: Double = 0.0,
    var lat: Double = 0.0,
    var lon: Double = 0.0
)

// This class is used to represent
// Airport records
class Airport() : AirportModel()

val airportModule = module {
    singleOf(::AirportRepository)
    singleOf(::AirportService)
}

//// Install Ktor StatusPages feature for handling validation errors globally
//fun StatusPages.Configuration.installValidationExceptionHandler() {
//    exception<IllegalArgumentException> { cause ->
//        call.respond(HttpStatusCode.BadRequest, cause.localizedMessage)
//    }
//}