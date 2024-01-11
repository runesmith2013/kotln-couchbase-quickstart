package com.couchbase.kotlin.quickstart

import com.couchbase.kotlin.quickstart.models.Airport
import com.couchbase.kotlin.quickstart.models.DestinationAirport
import com.couchbase.kotlin.quickstart.models.Geo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.testing.*
import io.ktor.util.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AirportTests {
    @Test
    fun testListAirportsInCountryWithPagination() = testApplication {
        val country = "France"
        val pageSize = 3
        val iterations = 3
        val airportsList = mutableListOf<String>()

        for (i in 0 until iterations) {
            // Send an HTTP GET request to the /airport/list endpoint with the specified query parameters
            val getResponse = client.get("/api/v1/airport/list") {
                parameter("country", country)
                parameter("limit", pageSize)
                parameter("offset", pageSize * i)
            }
            Assertions.assertEquals(HttpStatusCode.OK, getResponse.status)

            // Read the JSON response content and deserialize it
            val results = jacksonObjectMapper().readValue<List<Airport>>(getResponse.bodyAsText())

            Assertions.assertEquals(pageSize, results.size)
            for (airport in results) {
                airport.airportname?.let { airportsList.add(it) }
                Assertions.assertEquals(country, airport.country)
            }
        }

        Assertions.assertEquals(pageSize * iterations, airportsList.size)
    }

    @Test
    fun testListAirportsInInvalidCountry() = testApplication {
        // Arrange
        val airportAPI = "/api/v1/airport/list?country=invalid"

        // Act
        val response = client.get(airportAPI)

        // Assert
        Assertions.assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getDirectConnectionsTest() = testApplication {
        // Create query parameters
        val airport = "SFO"
        val limit = 5
        val offset = 0

        // Send an HTTP GET request to the /airport/direct-connections endpoint with the specified query parameters
        val getResponse = client.get("/api/v1/airport/direct-connections") {
            parameter("airport", airport)
            parameter("limit", limit)
            parameter("offset", offset)
        }

        // Assert that the HTTP response status code is OK
        Assertions.assertEquals(HttpStatusCode.OK, getResponse.status)

        // Read the JSON response content and deserialize it
        val results = jacksonObjectMapper().readValue<List<DestinationAirport>>(getResponse.bodyAsText())

        // Assert that the number of airports is as expected
        Assertions.assertEquals(limit, results.size)
    }

    @Test
    fun testDirectConnectionsInvalidAirport() = testApplication {
        // Arrange
        val airportAPI = "/api/v1/airport/direct-connections?airport=invalid"

        // Act
        val response = client.get(airportAPI)

        // Assert
        Assertions.assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun getAirportByIdTest() = testApplication {
        // Create airport
        val documentId = "airport_test_get"
        val airport = Airport().apply {
            airportname = "Test Airport"
            city = "Test City"
            country = "Test Country"
            faa = "TAA"
            icao = "TAAS"
            tz = "Europe/Berlin"
            geo = Geo().apply {
                lat = 40.0
                lon = 42.0
                alt = 100.0
            }
        }

        val objectMapper = jacksonObjectMapper()
        val newAirportJson = objectMapper.writeValueAsString(airport)

        // Post the airport
        val postResponse = client.post("/api/v1/airport/$documentId") {
            body = TextContent(newAirportJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.OK, postResponse.status)
        val newAirportResult = objectMapper.readValue<Airport>(postResponse.bodyAsText())

        // Get the airport by ID
        var getResponse = client.get("/api/v1/airport/$documentId")
        Assertions.assertEquals(HttpStatusCode.OK, getResponse.status)
        val resultAirport = objectMapper.readValue<Airport>(getResponse.bodyAsText())

        // Validate the retrieved airport
        Assertions.assertEquals(newAirportResult.airportname, resultAirport.airportname)
        Assertions.assertEquals(newAirportResult.city, resultAirport.city)
        Assertions.assertEquals(newAirportResult.country, resultAirport.country)

        // Remove airport
        val deleteResponse = client.delete("/api/v1/airport/$documentId")
        Assertions.assertEquals(HttpStatusCode.OK, deleteResponse.status)
    }

    @Test
    fun testReadInvalidAirport() = testApplication {
        // Arrange
        val documentId = "airport_test_invalid_id"

        // Act
        val response = client.get("/api/v1/airport/$documentId")

        // Assert
        Assertions.assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun createAirportTest() = testApplication {
        // Create airport
        val documentId = "airport_test_insert"
        val airport = Airport().apply {
            airportname = "Test Airport"
            city = "Test City"
            country = "Test Country"
            faa = "TAA"
            icao = "TAAS"
            tz = "Europe/Berlin"
            geo = Geo().apply {
                lat = 40.0
                lon = 42.0
                alt = 100.0
            }
        }

        val objectMapper = jacksonObjectMapper()
        val newAirportJson = objectMapper.writeValueAsString(airport)

        // Post the airport
        val postResponse = client.post("/api/v1/airport/$documentId") {
            body = TextContent(newAirportJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.OK, postResponse.status)
        val newAirportResult = objectMapper.readValue<Airport>(postResponse.bodyAsText())

        // Validate creation
        Assertions.assertEquals(airport.airportname, newAirportResult.airportname)
        Assertions.assertEquals(airport.city, newAirportResult.city)
        Assertions.assertEquals(airport.country, newAirportResult.country)

        // Remove airport
        val deleteResponse = client.delete("/api/v1/airport/$documentId")
        Assertions.assertEquals(HttpStatusCode.OK, deleteResponse.status)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun testAddDuplicateAirport() = testApplication {
        // Create the airport
        val documentId = "airport_test_duplicate"
        val airport = Airport().apply {
            airportname = "Test Airport"
            city = "Test City"
            country = "Test Country"
            faa = "TAA"
            icao = "TAAS"
            tz = "Europe/Berlin"
            geo = Geo().apply {
                lat = 40.0
                lon = 42.0
                alt = 100.0
            }
        }

        val objectMapper = jacksonObjectMapper()
        val newAirportJson = objectMapper.writeValueAsString(airport)

        // Post the airport
        var postResponse = client.post("/api/v1/airport/$documentId") {
            body = TextContent(newAirportJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.OK, postResponse.status)

        // Try to create the same airport again
        postResponse = client.post("/api/v1/airport/$documentId") {
            body = TextContent(newAirportJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.Conflict, postResponse.status)

        // Delete the airport
        val deleteResponse = client.delete("/api/v1/airport/$documentId")
        Assertions.assertEquals(HttpStatusCode.OK, deleteResponse.status)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun updateAirportTest() = testApplication {
        // Create airport
        val documentId = "airport_test_update"
        val airport = Airport().apply {
            airportname = "Test Airport"
            city = "Test City"
            country = "Test Country"
            faa = "TAA"
            icao = "TAAS"
            tz = "Europe/Berlin"
            geo = Geo().apply {
                lat = 40.0
                lon = 42.0
                alt = 100.0
            }
        }

        val objectMapper = jacksonObjectMapper()
        var airportJson = objectMapper.writeValueAsString(airport)

        // Post the airport
        val postResponse = client.post("/api/v1/airport/$documentId") {
            body = TextContent(airportJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.OK, postResponse.status)
        val airportResult = objectMapper.readValue<Airport>(postResponse.bodyAsText())

        // Update airport
        airportResult.let {
            it.city = "Updated City"
            it.country = "Updated Country"
            airportJson = objectMapper.writeValueAsString(it)

            // Put the updated airport
            val putResponse = client.put("/api/v1/airport/$documentId") {
                body = TextContent(airportJson, ContentType.Application.Json)
            }
            Assertions.assertEquals(HttpStatusCode.OK, putResponse.status)
            val updatedAirportResult = objectMapper.readValue<Airport>(putResponse.bodyAsText())

            // Validate update
            Assertions.assertEquals(it.airportname, updatedAirportResult.airportname)
            Assertions.assertEquals(it.city, updatedAirportResult.city)
            Assertions.assertEquals(it.country, updatedAirportResult.country)
        }

        // Remove airport
        val deleteResponse = client.delete("/api/v1/airport/$documentId")
        Assertions.assertEquals(HttpStatusCode.OK, deleteResponse.status)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun deleteAirportTest() = testApplication {
        // Create airport
        val documentId = "airport_test_delete"
        val airport = Airport().apply {
            airportname = "Test Airport"
            city = "Test City"
            country = "Test Country"
            faa = "TAA"
            icao = "TAAS"
            tz = "Europe/Berlin"
            geo = Geo().apply {
                lat = 40.0
                lon = 42.0
                alt = 100.0
            }
        }

        val objectMapper = jacksonObjectMapper()
        val newAirportJson = objectMapper.writeValueAsString(airport)

        // Post the airport
        val postResponse = client.post("/api/v1/airport/$documentId") {
            body = TextContent(newAirportJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.OK, postResponse.status)

        // Delete airport
        val deleteResponse = client.delete("/api/v1/airport/$documentId")
        Assertions.assertEquals(HttpStatusCode.OK, deleteResponse.status)

        // Check if the airport is no longer accessible
        val getResponse = client.get("/api/v1/airport/$documentId")
        Assertions.assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun testDeleteNonExistingAirport() = testApplication {
        // Arrange
        val documentId = "airport_non_existent_document"

        // Act
        val response = client.delete("/api/v1/airport/$documentId")

        // Assert
        Assertions.assertEquals(HttpStatusCode.NotFound, response.status)
    }
}