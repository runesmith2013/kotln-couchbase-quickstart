package com.couchbase.kotlin.quickstart

import com.couchbase.client.core.error.DocumentNotFoundException
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
import java.util.*

fun NormalOpenAPIRoute.profileRoutes() {
    val service = KoinJavaComponent.getKoin().get<ProfileService>()

    route("/") {
        route("profile") {
            post<Object, Profile, ProfileWithoutId> { _, data ->
                respond(service.createProfile(data))
            }
            get<PaginationRequest, List<Profile>> { params ->
                respond(service.listProfiles(params.skip?:0, params.limit?:10))
            }
            throws(HttpStatusCode.NotFound, DocumentNotFoundException::class) {
                get<ProfileRequest, Profile> { params ->
                    respond(service.getById(params.id))
                }
                throws(HttpStatusCode.BadRequest, MismatchedIdException::class) {
                    put<ProfileRequest, Profile, Profile> { params, profile ->
                        if (params.id != profile.pid) {
                            throw MismatchedIdException()
                        }
                        service.updateProfile(profile)
                    }
                }
                delete<ProfileRequest, ProfileDeleteResponse> { params ->
                    service.deleteProfile(params.id)
                    respond(ProfileDeleteResponse(params.id))
                }
            }
            route("search") {
                get<ProfileSearchRequest, List<Profile>> { params ->
                    respond(service.findProfiles(params.text, params.skip?:0, params.limit?:10))
                }
            }
        }
    }
}

class MismatchedIdException : Exception() {

}


data class PaginationRequest (
    @QueryParam("Profiles to skip before the results", allowEmptyValues = true)
    val skip: Int?,
    @QueryParam("Number of results", allowEmptyValues = true)
    val limit: Int?
)

@Path("{id}")
data class ProfileRequest(
    @PathParam("Profile identifier")
    val id: UUID
)

data class ProfileSearchRequest(
    @QueryParam("Text to search")
    val text: String = "",
    @QueryParam("Profiles to skip before the results", allowEmptyValues = true)
    val skip: Int?,
    @QueryParam("Number of results", allowEmptyValues = true)
    val limit: Int?
)

@Response("Removed the profile", statusCode = 202)
data class ProfileDeleteResponse (
    val profile: UUID
)
