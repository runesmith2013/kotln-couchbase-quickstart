package com.couchbase.kotlin.quickstart.routes

import com.couchbase.client.core.error.DocumentExistsException
import com.couchbase.client.core.error.DocumentNotFoundException
import com.couchbase.kotlin.quickstart.models.*
import com.couchbase.kotlin.quickstart.services.AirlineService
import com.papsign.ktor.openapigen.annotations.Path
import com.papsign.ktor.openapigen.annotations.Response
import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam
import com.papsign.ktor.openapigen.route.path.normal.*
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import com.papsign.ktor.openapigen.route.throws
import io.ktor.http.*
import org.koin.java.KoinJavaComponent

fun NormalOpenAPIRoute.airlineRoutes() {
    val service = KoinJavaComponent.getKoin().get<AirlineService>()

    route("/api/v1/airline") {
        throws(HttpStatusCode.InternalServerError, Exception::class) {
            throws(HttpStatusCode.Conflict, DocumentExistsException::class) {
                post<AirlineRequest, Airline, AirlineModel> { params, data ->
                    respond(service.createAirline(data, params.id))
                }
            }
            throws(HttpStatusCode.NotFound, DocumentNotFoundException::class) {
                get<AirlineRequest, Airline> { params ->
                    try {
                        respond(service.getAirlineById(params.id))
                    }catch (e: Exception)
                    {
                        println(e.message)
                    }
                }
                put<AirlineRequest, Airline, Airline> { params, airport ->
                    service.updateAirline(airport, params.id)
                    respond(airport)
                }
                delete<AirlineRequest, AirlineDeleteResponse> { params ->
                    service.deleteAirline(params.id)
                    respond(AirlineDeleteResponse(params.id))
                }
            }
            route("list") {
                get<AirlinePaginationRequest, List<Airline>> { params ->

                    try {
                        respond(service.listAirlines(params.country, params.limit ?: 10, params.offset ?: 0))
                    }catch (e: Exception)
                    {
                        println(e.message)
                    }
                }
            }
            route("to-airport") {
                get<ToAirportRequest, List<Airline>> { params ->
                    try {
                        respond(service.toAirport(params.airport, params.limit ?: 10, params.offset ?: 0))
                    }catch (e: Exception)
                    {
                        println(e.message)
                    }
                }
            }
        }
    }
}

data class AirlinePaginationRequest (
    @QueryParam("Country (Example: France, United Kingdom, United States)", allowEmptyValues = true)
    val country: String?,
    @QueryParam("Number of airlines to return (page size). Default value: 10.", allowEmptyValues = true)
    val limit: Int?,
    @QueryParam("Number of airlines to skip (for pagination). Default value: 0.", allowEmptyValues = true)
    val offset: Int?
)

@Path("{id}")
data class AirlineRequest(
    @PathParam("Airline ID like airline_10")
    val id: String
)

data class ToAirportRequest(
    @QueryParam("Destination airport (Example: SFO, JFK, LAX)")
    val airport: String,
    @QueryParam("Number of airlines to return (page size). Default value: 10.", allowEmptyValues = true)
    val limit: Int?,
    @QueryParam("Number of airlines to skip (for pagination). Default value: 0.", allowEmptyValues = true)
    val offset: Int?
)

@Response("Airline Deleted", statusCode = 200)
data class AirlineDeleteResponse (
    val airline: String
)
