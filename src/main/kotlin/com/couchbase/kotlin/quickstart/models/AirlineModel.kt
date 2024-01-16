package com.couchbase.kotlin.quickstart.models

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import com.couchbase.kotlin.quickstart.repositories.AirlineRepository
import com.couchbase.kotlin.quickstart.services.AirlineService
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.lang.IllegalArgumentException

// This class is used to represent
 // client requests to create a new
 // Airline record
@JsonIgnoreProperties(ignoreUnknown = true)
open class AirlineModel (
    var callsign: String? = null,
    var country: String? = null,
    var iata: String? = null,
    var icao: String? = null,
    var name: String? = null,

    @JsonIgnore
    var id: String? = null,

    @JsonIgnore
    var type: String? = null
){
    fun validate() {
        if (callsign.isNullOrBlank() || country.isNullOrBlank() || name.isNullOrBlank()) {
            throw IllegalArgumentException()
        }
    }
}

// This class is used to represent
// Airline records
@JsonIgnoreProperties(ignoreUnknown = true)
class Airline() : AirlineModel()

val airlineModule = module {
    singleOf(::AirlineRepository)
    singleOf(::AirlineService)
}