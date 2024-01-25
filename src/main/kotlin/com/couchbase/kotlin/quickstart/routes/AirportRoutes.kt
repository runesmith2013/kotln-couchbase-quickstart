package com.couchbase.kotlin.quickstart.routes

import com.couchbase.client.core.error.DocumentExistsException
import com.couchbase.client.core.error.DocumentNotFoundException
import com.couchbase.kotlin.quickstart.services.AirportService
import com.couchbase.kotlin.quickstart.models.Airport
import com.couchbase.kotlin.quickstart.models.AirportModel
import com.couchbase.kotlin.quickstart.models.DestinationAirport
import com.papsign.ktor.openapigen.APITag
import com.papsign.ktor.openapigen.annotations.Path
import com.papsign.ktor.openapigen.annotations.Response
import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam
import com.papsign.ktor.openapigen.route.*
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.delete
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.path.normal.put
import com.papsign.ktor.openapigen.route.response.respond
import io.ktor.http.*
import org.koin.java.KoinJavaComponent
import java.lang.IllegalArgumentException

enum class AirportAPITag(override val description: String) : APITag {
    Airport( "APIs related to Airport")
}

fun NormalOpenAPIRoute.airportRoutes() {
    val service = KoinJavaComponent.getKoin().get<AirportService>()

    tag(AirportAPITag.Airport) {
        route("/api/v1/airport") {
            throws(HttpStatusCode.InternalServerError, Exception::class) {
                throws(HttpStatusCode.Conflict,"A Document with the provided id already exists", DocumentExistsException::class) {
                    throws(HttpStatusCode.BadRequest, "Required fields (airportname, city, country, faa) cannot be null or blank.", IllegalArgumentException::class) {
                        post<AirportRequest, Airport, AirportModel> (
                            info(
                                description = "Create Airport with specified ID.\n\nThis provides an example of using Key Value operations in Couchbase to create a new document with a specified ID.\n\n Code: [`routes/AirportRoutes`](https://github.com/couchbase-examples/kotlin-quickstart/blob/main/src/main/kotlin/com/couchbase/kotlin/quickstart/routes/AirportRoutes.kt) \n File: `AirportRoutes` \n Method: `createAirport`"
                            ),
                            status(HttpStatusCode.Created)
                        ){ params, data ->
                            data.validate()
                            respond(service.createAirport(data, params.id))
                        }
                    }
                }
                throws(HttpStatusCode.NotFound, DocumentNotFoundException::class) {
                    get<AirportRequest, Airport> (
                        info(
                            description = "Get Airport with specified ID.\n\nThis provides an example of using Key Value operations in Couchbase to get a document with specified ID.\n\n Code: [`routes/AirportRoutes`](https://github.com/couchbase-examples/kotlin-quickstart/blob/main/src/main/kotlin/com/couchbase/kotlin/quickstart/routes/AirportRoutes.kt) \n File: `AirportRoutes` \n Method: `getAirportById`"
                        ),
                        status(HttpStatusCode.OK)
                    ) { params ->
                        respond(service.getAirportById(params.id))
                    }
                    throws(HttpStatusCode.BadRequest, "Required fields (airportname, city, country, faa) cannot be null or blank.", IllegalArgumentException::class) {
                        put<AirportRequest, Airport, Airport> (
                            info(
                                description = "Update Airport with specified ID.\n\nThis provides an example of using Key Value operations in Couchbase to upsert a document with specified ID.\n\n Code: [`routes/AirportRoutes`](https://github.com/couchbase-examples/kotlin-quickstart/blob/main/src/main/kotlin/com/couchbase/kotlin/quickstart/routes/AirportRoutes.kt) \n File: `AirportRoutes` \n Method: `updateAirport`"
                            ),
                            status(HttpStatusCode.OK)
                        ) { params, airport ->
                            airport.validate()
                            service.updateAirport(airport, params.id)
                            respond(airport)
                        }
                    }
                    delete<AirportRequest, AirportDeleteResponse> (
                        info(
                            description = "Delete Airport with specified ID.\n\nThis provides an example of using Key Value operations in Couchbase to delete a document with specified ID.\n\n Code: [`routes/AirportRoutes`](https://github.com/couchbase-examples/kotlin-quickstart/blob/main/src/main/kotlin/com/couchbase/kotlin/quickstart/routes/AirportRoutes.kt) \n File: `AirportRoutes` \n Method: `deleteAirport`"
                        ),
                        status(HttpStatusCode.OK)
                    ) { params ->
                        service.deleteAirport(params.id)
                        respond(AirportDeleteResponse(params.id))
                    }
                }
                route("list") {
                    get<AirportPaginationRequest, List<Airport>> (
                        info(
                            description = "Get list of Airports. Optionally, you can filter the list by Country.\n\nThis provides an example of using a SQL++ query in Couchbase to fetch a list of documents matching the specified criteria.\n\n Code: [`routes/AirportRoutes`](https://github.com/couchbase-examples/kotlin-quickstart/blob/main/src/main/kotlin/com/couchbase/kotlin/quickstart/routes/AirportRoutes.kt) \n File: `AirportRoutes` \n Method: `listAirports`"
                        ),
                        status(HttpStatusCode.OK)
                    ) { params ->
                        respond(service.listAirports(params.country, params.limit ?: 10, params.offset ?: 0))
                    }
                }
                route("direct-connections") {
                    get<DirectDestinationsRequest, List<DestinationAirport>> (
                        info(
                            description = "Get Direct Connections from specified Airport.\n\nThis provides an example of using a SQL++ query in Couchbase to fetch a list of documents matching the specified criteria.\n\n Code: [`routes/AirportRoutes`](https://github.com/couchbase-examples/kotlin-quickstart/blob/main/src/main/kotlin/com/couchbase/kotlin/quickstart/routes/AirportRoutes.kt) \n File: `AirportRoutes` \n Method: `directDestinations`"
                        ),
                        status(HttpStatusCode.OK)
                    ) { params ->
                        respond(service.directDestinations(params.airport,params.limit ?: 10, params.offset ?: 0))
                    }
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
