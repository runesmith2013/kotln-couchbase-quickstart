package com.couchbase.kotlin.quickstart.models

import com.fasterxml.jackson.annotation.JsonProperty

open class DestinationAirport(
    @JsonProperty("destinationairport")
    var destinationAirport: String? = null
)


