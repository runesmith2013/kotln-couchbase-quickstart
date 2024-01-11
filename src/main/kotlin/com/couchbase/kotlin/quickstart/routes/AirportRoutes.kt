package com.couchbase.kotlin.quickstart.routes

import com.couchbase.client.core.error.DocumentExistsException
import com.couchbase.client.core.error.DocumentNotFoundException
import com.couchbase.kotlin.quickstart.services.AirportService
import com.couchbase.kotlin.quickstart.models.Airport
import com.couchbase.kotlin.quickstart.models.AirportModel
import com.couchbase.kotlin.quickstart.models.DestinationAirport
import com.papsign.ktor.openapigen.annotations.Path
import com.papsign.ktor.openapigen.annotations.Response
import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.delete
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.path.normal.put
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import com.papsign.ktor.openapigen.route.throws
import io.ktor.http.*
import org.koin.java.KoinJavaComponent

fun NormalOpenAPIRoute.airportRoutes() {
    val service = KoinJavaComponent.getKoin().get<AirportService>()

    route("/api/v1/airport") {
        throws(HttpStatusCode.InternalServerError, Exception::class) {
            throws(HttpStatusCode.Conflict, DocumentExistsException::class) {
                post<AirportRequest, Airport, AirportModel> { params, data ->
                    respond(service.createAirport(data, params.id))
                }
            }
            throws(HttpStatusCode.NotFound, DocumentNotFoundException::class) {
                get<AirportRequest, Airport> { params ->
                    respond(service.getAirportById(params.id))
                }
                put<AirportRequest, Airport, Airport> { params, airport ->
                    service.updateAirport(airport, params.id)
                    respond(airport)
                }
                delete<AirportRequest, AirportDeleteResponse> { params ->
                    service.deleteAirport(params.id)
                    respond(AirportDeleteResponse(params.id))
                }
            }
            route("list") {
                get<AirportPaginationRequest, List<Airport>> { params ->
                    respond(service.listAirports(params.country, params.limit ?: 10, params.offset ?: 0))
                }
            }
            route("direct-connections") {
                get<DirectDestinationsRequest, List<DestinationAirport>> { params ->
                    respond(service.directDestinations(params.airport,params.limit ?: 10, params.offset ?: 0))
                }
            }
        }
    }
}

data class AirportPaginationRequest (
    @QueryParam("Country (Example: France, United Kingdom, United States)", allowEmptyValues = true)
    val country: String?,
    @QueryParam("Number of airports to return (page size). Default value: 10.", allowEmptyValues = true)
    val limit: Int?,
    @QueryParam("Number of airports to skip (for pagination). Default value: 0.", allowEmptyValues = true)
    val offset: Int?
)

@Path("{id}")
data class AirportRequest(
    @PathParam("Airport ID like airport_1273")
    val id: String
)

data class DirectDestinationsRequest(
    @QueryParam("Airport (Example: SFO, JFK, LAX)")
    val airport: String,
    @QueryParam("Number of direct connections to return (page size). Default value: 10.", allowEmptyValues = true)
    val limit: Int?,
    @QueryParam("Number of direct connections to skip (for pagination). Default value: 0.", allowEmptyValues = true)
    val offset: Int?
)

@Response("Airport Deleted", statusCode = 200)
data class AirportDeleteResponse (
    val airport: String
)
