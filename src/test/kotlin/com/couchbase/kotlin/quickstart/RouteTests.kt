package com.couchbase.kotlin.quickstart

import com.couchbase.kotlin.quickstart.models.Route
import com.couchbase.kotlin.quickstart.models.Schedule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.util.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RouteTests {
    @OptIn(InternalAPI::class)
    @Test
    fun getRouteByIdTest() = testApplication {
        val documentId = "route_test_get"
        val route = Route().apply {
            airline = "SAF"
            airlineid = "airline_sample"
            destinationairport = "JFK"
            distance = 1000.79
            equipment = "CRJ"
            schedule = listOf(Schedule().apply {
                day = 0
                utc = "14:05:00"
                flight = "SAF123"
            })
            sourceairport = "SFO"
            stops = 0
        }

        val objectMapper = jacksonObjectMapper()
        val routeJson = objectMapper.writeValueAsString(route)

        // Create route
        val postResponse = client.post("/api/v1/route/$documentId") {
            body = TextContent(routeJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.OK, postResponse.status)
        val newRouteResult = objectMapper.readValue<Route>(postResponse.bodyAsText())

        // Get the route by ID
        val getResponse = client.get("/api/v1/route/$documentId")
        Assertions.assertEquals(HttpStatusCode.OK, getResponse.status)
        val resultRoute = objectMapper.readValue<Route>(getResponse.bodyAsText())

        // Validate the retrieved route
        Assertions.assertEquals(newRouteResult.airline, resultRoute.airline)
        Assertions.assertEquals(newRouteResult.sourceairport, resultRoute.sourceairport)
        Assertions.assertEquals(newRouteResult.destinationairport, resultRoute.destinationairport)

        // Remove route
        val deleteResponse = client.delete("/api/v1/route/$documentId")
        Assertions.assertEquals(HttpStatusCode.OK, deleteResponse.status)
    }

    @Test
    fun testReadInvalidRoute() = testApplication {
        // Arrange
        val documentId = "route_test_invalid_id"

        // Act
        val response = client.get("/api/v1/route/$documentId")

        // Assert
        Assertions.assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun createRouteTest() = testApplication {
        // Create route
        val documentId = "route_test_insert"
        val route = Route().apply {
            airline = "SAF"
            airlineid = "airline_sample"
            destinationairport = "JFK"
            distance = 1000.79
            equipment = "CRJ"
            schedule = listOf(Schedule().apply {
                day = 0
                utc = "14:05:00"
                flight = "SAF123"
            })
            sourceairport = "SFO"
            stops = 0
        }

        val objectMapper = jacksonObjectMapper()
        val routeJson = objectMapper.writeValueAsString(route)

        // Post the route
        val postResponse = client.post("/api/v1/route/$documentId") {
            body = TextContent(routeJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.OK, postResponse.status)
        val newRouteResult = objectMapper.readValue<Route>(postResponse.bodyAsText())

        // Validate creation
        Assertions.assertEquals(route.airline, newRouteResult.airline)
        Assertions.assertEquals(route.sourceairport, newRouteResult.sourceairport)
        Assertions.assertEquals(route.destinationairport, newRouteResult.destinationairport)

        // Remove route
        val deleteResponse = client.delete("/api/v1/route/$documentId")
        Assertions.assertEquals(HttpStatusCode.OK, deleteResponse.status)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun testAddDuplicateRoute() = testApplication {
        // Create the route
        val documentId = "route_test_duplicate"
        val route = Route().apply {
            airline = "SAF"
            airlineid = "airline_sample"
            destinationairport = "JFK"
            distance = 1000.79
            equipment = "CRJ"
            schedule = listOf(Schedule().apply {
                day = 0
                utc = "14:05:00"
                flight = "SAF123"
            })
            sourceairport = "SFO"
            stops = 0
        }
        val objectMapper = jacksonObjectMapper()
        val routeJson = objectMapper.writeValueAsString(route)

        // Post the route
        var postResponse = client.post("/api/v1/route/$documentId") {
            body = TextContent(routeJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.OK, postResponse.status)

        // Try to create the same route again
        postResponse = client.post("/api/v1/route/$documentId") {
            body = TextContent(routeJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.Conflict, postResponse.status)

        // Delete the route
        val deleteResponse = client.delete("/api/v1/route/$documentId")
        Assertions.assertEquals(HttpStatusCode.OK, deleteResponse.status)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun updateRouteTest() = testApplication {
        // Create route
        val documentId = "route_test_update"
        val route = Route().apply {
            airline = "SAF"
            airlineid = "airline_sample"
            destinationairport = "JFK"
            distance = 1000.79
            equipment = "CRJ"
            schedule = listOf(Schedule().apply {
                day = 0
                utc = "14:05:00"
                flight = "SAF123"
            })
            sourceairport = "SFO"
            stops = 0
        }

        val objectMapper = jacksonObjectMapper()
        var routeJson = objectMapper.writeValueAsString(route)

        // Post the route
        var postResponse = client.post("/api/v1/route/$documentId") {
            body = TextContent(routeJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.OK, postResponse.status)
        val newRouteResult = objectMapper.readValue<Route>(postResponse.bodyAsText())

        // Update route
        newRouteResult.apply {
            airline = "USAF"
            airlineid = "airline_sample_updated"
            destinationairport = "JFK"
            distance = 1000.79
            equipment = "CRJ"
            schedule = listOf(Schedule().apply {
                day = 0
                utc = "14:05:00"
                flight = "SAF123"
            })
            sourceairport = "SFO"
            stops = 0
        }
        routeJson = objectMapper.writeValueAsString(newRouteResult)

        // Put the updated route
        postResponse = client.put("/api/v1/route/$documentId") {
            body = TextContent(routeJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.OK, postResponse.status)
        val updatedRouteResult = objectMapper.readValue<Route>(postResponse.bodyAsText())

        // Validate update
        Assertions.assertEquals(newRouteResult.airline, updatedRouteResult.airline)
        Assertions.assertEquals(newRouteResult.sourceairport, updatedRouteResult.sourceairport)
        Assertions.assertEquals(newRouteResult.destinationairport, updatedRouteResult.destinationairport)

        // Remove route
        val deleteResponse = client.delete("/api/v1/route/$documentId")
        Assertions.assertEquals(HttpStatusCode.OK, deleteResponse.status)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun deleteRouteTest() = testApplication {
        // Create route
        val documentId = "route_test_delete"
        val route = Route().apply {
            airline = "SAF"
            airlineid = "airline_sample"
            destinationairport = "JFK"
            distance = 1000.79
            equipment = "CRJ"
            schedule = listOf(Schedule().apply {
                day = 0
                utc = "14:05:00"
                flight = "SAF123"
            })
            sourceairport = "SFO"
            stops = 0
        }

        val objectMapper = jacksonObjectMapper()
        val routeJson = objectMapper.writeValueAsString(route)

        // Post the route
        val postResponse = client.post("/api/v1/route/$documentId") {
            body = TextContent(routeJson, ContentType.Application.Json)
        }
        Assertions.assertEquals(HttpStatusCode.OK, postResponse.status)

        // Delete route
        val deleteResponse = client.delete("/api/v1/route/$documentId")
        Assertions.assertEquals(HttpStatusCode.OK, deleteResponse.status)

        // Check if the route is no longer accessible
        val getResponse = client.get("/api/v1/route/$documentId")
        Assertions.assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun testDeleteNonExistingRoute() = testApplication {
        // Arrange
        val documentId = "route_non_existent_document"

        // Act
        val response = client.delete("/api/v1/route/$documentId")

        // Assert
        Assertions.assertEquals(HttpStatusCode.NotFound, response.status)
    }
}