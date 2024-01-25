package com.couchbase.kotlin.quickstart.routes

import com.couchbase.client.core.error.DocumentExistsException
import com.couchbase.client.core.error.DocumentNotFoundException
import com.couchbase.kotlin.quickstart.models.Route
import com.couchbase.kotlin.quickstart.models.RouteModel
import com.couchbase.kotlin.quickstart.services.RouteService
import com.papsign.ktor.openapigen.APITag
import com.papsign.ktor.openapigen.annotations.Path
import com.papsign.ktor.openapigen.annotations.Response
import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.route.*
import com.papsign.ktor.openapigen.route.path.normal.*
import com.papsign.ktor.openapigen.route.response.respond
import io.ktor.http.*
import org.koin.java.KoinJavaComponent
import java.lang.IllegalArgumentException

enum class RouteAPITag(override val description: String) : APITag {
    Route( "APIs related to Route")}

fun NormalOpenAPIRoute.routeRoutes() {
    val service = KoinJavaComponent.getKoin().get<RouteService>()
    tag(RouteAPITag.Route) {
        route("/api/v1/route") {
            throws(HttpStatusCode.InternalServerError, Exception::class) {
                throws(HttpStatusCode.Conflict, "A Document with the provided id already exists", DocumentExistsException::class) {
                    throws(HttpStatusCode.BadRequest, "Required fields (airline, airlineid, destinationairport, sourceairport) cannot be null or blank.", IllegalArgumentException::class) {
                        post<RouteRequest, Route, RouteModel> (
                            info(
                                description = "Create Route with specified ID.\n\nThis provides an example of using Key Value operations in Couchbase to create a new document with a specified ID.\n\n Code: [`routes/RouteRoutes`](https://github.com/couchbase-examples/kotlin-quickstart/blob/main/src/main/kotlin/com/couchbase/kotlin/quickstart/routes/RouteRoutes.kt) \n File: `RouteRoutes` \n Method: `createRoute`"
                            ),
                            status(HttpStatusCode.Created)
                        ) { params, data ->
                            data.validate()
                            respond(service.createRoute(data, params.id))
                        }
                    }
                }
                throws(HttpStatusCode.NotFound, DocumentNotFoundException::class) {
                    get<RouteRequest, Route> (
                        info(
                            description = "Get Route with specified ID.\n\nThis provides an example of using Key Value operations in Couchbase to get a document with specified ID.\n\n Code: [`routes/RouteRoutes`](https://github.com/couchbase-examples/kotlin-quickstart/blob/main/src/main/kotlin/com/couchbase/kotlin/quickstart/routes/RouteRoutes.kt) \n File: `RouteRoutes` \n Method: `getRouteById`"
                        ),
                        status(HttpStatusCode.OK)
                    ) { params ->
                        respond(service.getRouteById(params.id))
                    }
                    throws(HttpStatusCode.BadRequest, "Required fields (airline, airlineid, destinationairport, sourceairport) cannot be null or blank.", IllegalArgumentException::class) {
                        put<RouteRequest, Route, Route> (
                            info(
                                description = "Update Route with specified ID.\n\nThis provides an example of using Key Value operations in Couchbase to upsert a document with specified ID.\n\n Code: [`routes/RouteRoutes`](https://github.com/couchbase-examples/kotlin-quickstart/blob/main/src/main/kotlin/com/couchbase/kotlin/quickstart/routes/RouteRoutes.kt) \n File: `RouteRoutes` \n Method: `updateRoute`"
                            ),
                            status(HttpStatusCode.OK)
                        ) { params, route ->
                            route.validate()
                            service.updateRoute(route, params.id)
                            respond(route)
                        }
                    }
                    delete<RouteRequest, RouteDeleteResponse> (
                        info(
                            description = "Update Route with specified ID.\n\nThis provides an example of using Key Value operations in Couchbase to upsert a document with specified ID.\n\n Code: [`routes/RouteRoutes`](https://github.com/couchbase-examples/kotlin-quickstart/blob/main/src/main/kotlin/com/couchbase/kotlin/quickstart/routes/RouteRoutes.kt) \n File: `RouteRoutes` \n Method: `deleteRoute`"
                        ),
                        status(HttpStatusCode.OK)
                    ) { params ->
                        service.deleteRoute(params.id)
                        respond(RouteDeleteResponse(params.id))
                    }
                }
            }
        }
    }
}

@Path("{id}")
data class RouteRequest(
    @PathParam("Route ID like route_10000")
    val id: String
)

@Response("Route Deleted", statusCode = 200)
data class RouteDeleteResponse (
    val airport: String
)
