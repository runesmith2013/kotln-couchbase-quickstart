package com.couchbase.kotlin.quickstart.routes

import com.couchbase.client.core.error.DocumentExistsException
import com.couchbase.client.core.error.DocumentNotFoundException
import com.couchbase.kotlin.quickstart.models.Route
import com.couchbase.kotlin.quickstart.models.RouteModel
import com.couchbase.kotlin.quickstart.services.RouteService
import com.papsign.ktor.openapigen.annotations.Path
import com.papsign.ktor.openapigen.annotations.Response
import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.route.path.normal.*
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import com.papsign.ktor.openapigen.route.throws
import io.ktor.http.*
import org.koin.java.KoinJavaComponent

fun NormalOpenAPIRoute.routeRoutes() {
    val service = KoinJavaComponent.getKoin().get<RouteService>()

    route("/api/v1/route") {
        throws(HttpStatusCode.InternalServerError, Exception::class) {
            throws(HttpStatusCode.Conflict, DocumentExistsException::class) {
                post<RouteRequest, Route, RouteModel> { params, data ->
                    respond(service.createRoute(data, params.id))
                }
            }
            throws(HttpStatusCode.NotFound, DocumentNotFoundException::class) {
                get<RouteRequest, Route> { params ->
                    respond(service.getRouteById(params.id))
                }
                put<RouteRequest, Route, Route> { params, route ->
                    service.updateRoute(route, params.id)
                    respond(route)
                }
                delete<RouteRequest, RouteDeleteResponse> { params ->
                    service.deleteRoute(params.id)
                    respond(RouteDeleteResponse(params.id))
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
