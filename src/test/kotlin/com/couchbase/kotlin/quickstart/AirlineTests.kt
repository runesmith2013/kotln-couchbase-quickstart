package com.couchbase.kotlin.quickstart

import com.couchbase.kotlin.quickstart.models.Airline
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.testing.*
import io.ktor.util.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AirlineTests {
    @Test
    fun testListAirlinesInCountryWithPagination() = testApplication {
        val country = "United States"
        val pageSize = 3
        val iterations = 3
        val airlinesList = mutableListOf<String>()

        for (i in 0 until iterations) {
            // Send an HTTP GET request to the /airline/list endpoint with the specified query parameters
            val getResponse = client.get("/api/v1/airline/list") {
                parameter("country", country)
                parameter("limit", pageSize)
                parameter("offset", pageSize * i)
            }
            Assertions.assertEquals(HttpStatusCode.OK, getResponse.status)

            // Read the JSON response content and deserialize it
            val results = jacksonObjectMapper().readValue<List<Airline>>(getResponse.bodyAsText())

            Assertions.assertEquals(pageSize, results.size)
            for (airline in results) {
                airline.name?.let { airlinesList.add(it) }
                Assertions.assertEquals(country, airline.country)
            }
        }

        Assertions.assertEquals(pageSize * iterations, airlinesList.size)
    }

    @Test
    fun testListAirlinesInInvalidCountry() = testApplication {
        // Arrange
        val airlineAPI = "/api/v1/airline/list?country=invalid"

        // Act
        val response = client.get(airlineAPI)

        // Assert
        Assertions.assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun getToAirportTest() = testApplication {
        // Create query parameters
        val airport = "SFO"
        val limit = 5
        val offset = 0

        // Send an HTTP GET request to the /airline/to-airport endpoint with the specified query parameters
        val getResponse = client.get("/api/v1/airline/to-airport") {
            parameter("airport", airport)
            parameter("limit", limit)
            parameter("offset", offset)
        }

        // Assert that the HTTP response status code is OK
        Assertions.assertEquals(HttpStatusCode.OK, getResponse.status)

        // Read the JSON response content and deserialize it
        val results = jacksonObjectMapper().readValue<List<Airline>>(getResponse.bodyAsText())

        // Assert that the number of airlines is as expected
        Assertions.assertEquals(limit, results.size)
    }

    @Test
    fun testToAirportInvalidAirport() = testApplication {
        // Arrange
        val airlineAPI = "/api/v1/airline/to-airport?airport=invalid"

        // Act
        val response = client.get(airlineAPI)

        // Assert
        Assertions.assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun getAirlineByIdTest() = testApplication {
        val documentId = "airline_test_get"
        val airline = Airline().apply {
            callsign = "TestAirline"
            country = "TestCountry"
            iata = "TestIATA"
            icao = "TestICAO"
            name = "TestName"
        }

        val objectMapper = jacksonObjectMapper()
        val airlineJson = objectMapper.writeValueAsString(airline)

        // Create airline
        val postResponse = client.post("/api/v1/airline/$documentId") {
            body = TextContent(airlineJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.OK, postResponse.status)
        val newAirlineResult = objectMapper.readValue<Airline>(postResponse.bodyAsText())

        // Get the airline by ID
        val getResponse = client.get("/api/v1/airline/$documentId")
        Assertions.assertEquals(HttpStatusCode.OK, getResponse.status)
        val resultAirline = objectMapper.readValue<Airline>(getResponse.bodyAsText())

        // Validate the retrieved airline
        Assertions.assertEquals(newAirlineResult.name, resultAirline.name)
        Assertions.assertEquals(newAirlineResult.country, resultAirline.country)
        Assertions.assertEquals(newAirlineResult.icao, resultAirline.icao)

        // Remove airline
        val deleteResponse = client.delete("/api/v1/airline/$documentId")
        Assertions.assertEquals(HttpStatusCode.OK, deleteResponse.status)
    }

    @Test
    fun testReadInvalidAirline() = testApplication {
        // Arrange
        val documentId = "airline_test_invalid_id"

        // Act
        val response = client.get("/api/v1/airline/$documentId")

        // Assert
        Assertions.assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun createAirlineTest() = testApplication {
        // Create airline
        val documentId = "airline_test_insert"
        val airline = Airline().apply {
            callsign = "TestAirline"
            country = "TestCountry"
            iata = "TestIATA"
            icao = "TestICAO"
            name = "TestName"
        }

        val objectMapper = jacksonObjectMapper()
        val airlineJson = objectMapper.writeValueAsString(airline)

        // Post the airline
        val postResponse = client.post("/api/v1/airline/$documentId") {
            body = TextContent(airlineJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.OK, postResponse.status)
        val newAirlineResult = objectMapper.readValue<Airline>(postResponse.bodyAsText())

        // Validate creation
        Assertions.assertEquals(airline.name, newAirlineResult.name)
        Assertions.assertEquals(airline.country, newAirlineResult.country)

        // Remove airline
        val deleteResponse = client.delete("/api/v1/airline/$documentId")
        Assertions.assertEquals(HttpStatusCode.OK, deleteResponse.status)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun testAddDuplicateAirline() = testApplication {
        // Create the airline
        val documentId = "airline_test_duplicate"
        val airline = Airline().apply {
            callsign = "TestAirline"
            country = "TestCountry"
            iata = "TestIATA"
            icao = "TestICAO"
            name = "TestName"
        }

        val objectMapper = jacksonObjectMapper()
        val airlineJson = objectMapper.writeValueAsString(airline)

        // Post the airline
        var postResponse = client.post("/api/v1/airline/$documentId") {
            body = TextContent(airlineJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.OK, postResponse.status)

        // Try to create the same airline again
        postResponse = client.post("/api/v1/airline/$documentId") {
            body = TextContent(airlineJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.Conflict, postResponse.status)

        // Delete the airline
        val deleteResponse = client.delete("/api/v1/airline/$documentId")
        Assertions.assertEquals(HttpStatusCode.OK, deleteResponse.status)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun testAddAirlineWithoutRequiredFields() = testApplication {
        // Arrange
        val documentId = "airline_test_invalid_payload"
        val airline = Airline().apply {
            iata = "SAL"
            icao = "SALL"
            country = "Sample Country"
        }

        val objectMapper = jacksonObjectMapper()
        val airlineJson = objectMapper.writeValueAsString(airline)

        // Act
        val postResponse = client.post("/api/v1/airline/$documentId") {
            body = TextContent(airlineJson, ContentType.Application.Json)
        }

        // Assert
        Assertions.assertEquals(HttpStatusCode.BadRequest, postResponse.status)

        // Check if the document was not created
        val getResponse = client.get("/api/v1/airline/$documentId")
        Assertions.assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }


    @OptIn(InternalAPI::class)
    @Test
    fun updateAirlineTest() = testApplication {
        // Create airline
        val documentId = "airline_test_update"
        val airline = Airline().apply {
            callsign = "TestAirline"
            country = "TestCountry"
            iata = "TestIATA"
            icao = "TestICAO"
            name = "TestName"
        }

        val objectMapper = jacksonObjectMapper()
        var airlineJson = objectMapper.writeValueAsString(airline)

        // Post the airline
        var postResponse = client.post("/api/v1/airline/$documentId") {
            body = TextContent(airlineJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.OK, postResponse.status)
        val newAirlineResult = objectMapper.readValue<Airline>(postResponse.bodyAsText())

        // Update airline
        newAirlineResult.name = "UpdatedName"
        newAirlineResult.country = "UpdatedCountry"
        newAirlineResult.icao = "UpdatedICAO"
        airlineJson = objectMapper.writeValueAsString(newAirlineResult)

        // Put the updated airline
        postResponse = client.put("/api/v1/airline/$documentId") {
            body = TextContent(airlineJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.OK, postResponse.status)
        val updatedAirlineResult = objectMapper.readValue<Airline>(postResponse.bodyAsText())

        // Validate update
        Assertions.assertEquals(newAirlineResult.name, updatedAirlineResult.name)
        Assertions.assertEquals(newAirlineResult.country, updatedAirlineResult.country)
        Assertions.assertEquals(newAirlineResult.icao, updatedAirlineResult.icao)

        // Remove airline
        val deleteResponse = client.delete("/api/v1/airline/$documentId")
        Assertions.assertEquals(HttpStatusCode.OK, deleteResponse.status)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun testUpdateWithInvalidDocument() = testApplication {
        // Arrange
        val documentId = "airline_test_update_invalid_doc"
        val updatedAirline = Airline().apply {
            iata = "SAL"
            icao = "SALL"
            callsign = "SAM"
            country = "Updated Country"
        }

        val objectMapper = jacksonObjectMapper()
        val updatedAirlineJson = objectMapper.writeValueAsString(updatedAirline)

        // Act
        val putResponse = client.put("/api/v1/airline/$documentId") {
            body = TextContent(updatedAirlineJson, ContentType.Application.Json)
        }

        // Assert
        Assertions.assertEquals(HttpStatusCode.BadRequest, putResponse.status)

        // Check if the document was not created
        val getResponse = client.get("/api/v1/airline/$documentId")
        Assertions.assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }


    @OptIn(InternalAPI::class)
    @Test
    fun deleteAirlineTest() = testApplication {
        // Create airline
        val documentId = "airline_test_delete"
        val airline = Airline().apply {
            callsign = "TestAirline"
            country = "TestCountry"
            iata = "TestIATA"
            icao = "TestICAO"
            name = "TestName"
        }

        val objectMapper = jacksonObjectMapper()
        val airlineJson = objectMapper.writeValueAsString(airline)

        // Post the airline
        var postResponse = client.post("/api/v1/airline/$documentId") {
            body = TextContent(airlineJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.OK, postResponse.status)

        // Delete airline
        val deleteResponse = client.delete("/api/v1/airline/$documentId")
        Assertions.assertEquals(HttpStatusCode.OK, deleteResponse.status)

        // Check if the airline is no longer accessible
        val getResponse = client.get("/api/v1/airline/$documentId")
        Assertions.assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun testDeleteNonExistingAirline() = testApplication {
        // Arrange
        val documentId = "airline_non_existent_document"

        // Act
        val response = client.delete("/api/v1/airline/$documentId")

        // Assert
        Assertions.assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
