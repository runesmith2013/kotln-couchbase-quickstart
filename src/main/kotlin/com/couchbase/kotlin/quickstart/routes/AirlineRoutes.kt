package com.couchbase.kotlin.quickstart.routes

import com.couchbase.client.core.error.DocumentExistsException
import com.couchbase.client.core.error.DocumentNotFoundException
import com.couchbase.kotlin.quickstart.models.*
import com.couchbase.kotlin.quickstart.services.AirlineService
import com.papsign.ktor.openapigen.APITag
import com.papsign.ktor.openapigen.annotations.Path
import com.papsign.ktor.openapigen.annotations.Response
import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam
import com.papsign.ktor.openapigen.route.*
import com.papsign.ktor.openapigen.route.path.normal.*
import com.papsign.ktor.openapigen.route.response.respond
import io.ktor.http.*
import org.koin.java.KoinJavaComponent
import java.lang.IllegalArgumentException

enum class AirlineAPITag(override val description: String) : APITag {
    Airline( "APIs related to Airline")
}

fun NormalOpenAPIRoute.airlineRoutes() {
    val service = KoinJavaComponent.getKoin().get<AirlineService>()

    tag(AirlineAPITag.Airline) {
        route("/api/v1/airline") {
            throws(HttpStatusCode.InternalServerError, Exception::class) {
                throws(HttpStatusCode.Conflict, "A Document with the provided id already exists", DocumentExistsException::class) {
                    throws(HttpStatusCode.BadRequest, "Required fields (callsign, country, name) cannot be null or blank.", IllegalArgumentException::class) {
                        post<AirlineRequest, Airline, AirlineModel> (
                            info(
                                description = "Create Airport with specified ID.\n\nThis provides an example of using Key Value operations in Couchbase to create a new document with a specified ID. \n\n Code: [`routes/AirlineRoutes`](https://github.com/couchbase-examples/kotlin-quickstart/blob/kotlin-quickstart-v2/src/main/kotlin/com/couchbase/kotlin/quickstart/routes/AirlineRoutes.kt) \n File: `AirlineRoutes` \n Method: `createAirline`"
                            ),
                            status(HttpStatusCode.Created)
                            ) { params, data ->
                            data.validate()
                            respond(service.createAirline(data, params.id))
                        }
                    }
                }
                throws(HttpStatusCode.NotFound, DocumentNotFoundException::class) {
                    get<AirlineRequest, Airline> (
                        info(
                            description = "Get Airline with specified ID.\n\nThis provides an example of using Key Value operations in Couchbase to get a document with specified ID.\n\n Code: [`routes/AirlineRoutes`](https://github.com/couchbase-examples/kotlin-quickstart/blob/kotlin-quickstart-v2/src/main/kotlin/com/couchbase/kotlin/quickstart/routes/AirlineRoutes.kt) \n File: `AirlineRoutes` \n Method: `getAirlineById`"
                        ),
                        status(HttpStatusCode.OK)
                    ){ params ->
                        respond(service.getAirlineById(params.id))
                    }
                    throws(HttpStatusCode.BadRequest, "Required fields (callsign, country, name) cannot be null or blank.", IllegalArgumentException::class) {
                        put<AirlineRequest, Airline, Airline> (
                            info(
                                description = "Update Airline with specified ID.\n\nThis provides an example of using Key Value operations in Couchbase to upsert a document with specified ID.\n\n Code: [`routes/AirlineRoutes`](https://github.com/couchbase-examples/kotlin-quickstart/blob/kotlin-quickstart-v2/src/main/kotlin/com/couchbase/kotlin/quickstart/routes/AirlineRoutes.kt) \n File: `AirlineRoutes` \n Method: `updateAirline`"
                            ),
                            status(HttpStatusCode.OK)
                        ) { params, airport ->
                            airport.validate()
                            service.updateAirline(airport, params.id)
                            respond(airport)
                        }
                    }
                    delete<AirlineRequest, AirlineDeleteResponse> (
                        info(
                            description = "Delete Airline with specified ID.\n\nThis provides an example of using Key Value operations in Couchbase to delete a document with specified ID.\n\n Code: [`routes/AirlineRoutes`](https://github.com/couchbase-examples/kotlin-quickstart/blob/kotlin-quickstart-v2/src/main/kotlin/com/couchbase/kotlin/quickstart/routes/AirlineRoutes.kt) \n File: `AirlineRoutes` \n Method: `deleteAirline`"
                        ),
                        status(HttpStatusCode.OK)
                    ) { params ->
                        service.deleteAirline(params.id)
                        respond(AirlineDeleteResponse(params.id))
                    }
                }
                route("list") {
                    get<AirlinePaginationRequest, List<Airline>> (
                        info(
                            description = "Get list of Airlines. Optionally, you can filter the list by Country.\n\nThis provides an example of using SQL++ query in Couchbase to fetch a list of documents matching the specified criteria.\n\n Code: [`routes/AirlineRoutes`](https://github.com/couchbase-examples/kotlin-quickstart/blob/kotlin-quickstart-v2/src/main/kotlin/com/couchbase/kotlin/quickstart/routes/AirlineRoutes.kt) \n File: `AirlineRoutes` \n Method: `listAirlines`"
                        ),
                        status(HttpStatusCode.OK)
                    ) { params ->
                        respond(service.listAirlines(params.country, params.limit ?: 10, params.offset ?: 0))
                    }
                }
                route("to-airport") {
                    get<ToAirportRequest, List<Airline>> (
                        info(
                            description = "Get Airlines flying to specified destination Airport.\n\nThis provides an example of using SQL++ query in Couchbase to fetch a list of documents matching the specified criteria.\n\n Code: [`routes/AirlineRoutes`](https://github.com/couchbase-examples/kotlin-quickstart/blob/kotlin-quickstart-v2/src/main/kotlin/com/couchbase/kotlin/quickstart/routes/AirlineRoutes.kt) \n File: `AirlineRoutes` \n Method: `toAirport`"
                        ),
                        status(HttpStatusCode.OK)
                    ) { params ->
                        respond(service.toAirport(params.airport, params.limit ?: 10, params.offset ?: 0))
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
