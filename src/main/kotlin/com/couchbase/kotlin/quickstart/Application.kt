package com.couchbase.kotlin.quickstart

import com.couchbase.kotlin.quickstart.routes.airlineRoutes
import com.couchbase.kotlin.quickstart.routes.airportRoutes
import com.couchbase.kotlin.quickstart.routes.routeRoutes
import com.papsign.ktor.openapigen.OpenAPIGen
import com.papsign.ktor.openapigen.openAPIGen
import com.papsign.ktor.openapigen.route.apiRouting
import com.couchbase.kotlin.quickstart.models.airlineModule
import com.couchbase.kotlin.quickstart.models.airportModule
import com.couchbase.kotlin.quickstart.models.routeModule
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import kotlin.time.ExperimentalTime

fun main(args: Array<String>): Unit = EngineMain.main(args)

@ExperimentalTime
fun Application.initialize() {

  // Open API (Swagger) UI
  install(OpenAPIGen) {

    // this servers OpenAPI definition on /openapi.json
    serveOpenApiJson = true
    // this servers Swagger UI on /swagger-ui/index.html
    serveSwaggerUi = true

    info {
      version = "1.0"
      title = "Quickstart in Couchbase with Kotlin and Ktor"
      description = """
            A quickstart API using Kotlin and Ktor with Couchbase and sample data.

            We have a visual representation of the API documentation using Swagger, allowing interaction with the API endpoints directly through the browser. It provides a clear view of the API, including endpoints, HTTP methods, request parameters, and response objects.

            Click on an individual endpoint to expand it and see detailed information, such as the endpoint's description, possible response status codes, and accepted request parameters.

            Trying Out the API

            You can try out an API by clicking on the "Try it out" button next to the endpoints.

            - Parameters: If an endpoint requires parameters, Swagger UI provides input boxes for you to fill in. This could include path parameters, query strings, headers, or the body of a POST/PUT request.

            - Execution: Once you've inputted all the necessary parameters, you can click the "Execute" button to make a live API call. Swagger UI will send the request to the API and display the response directly in the documentation. This includes the response code, response headers, and response body.

            Models

            Swagger documents the structure of request and response bodies using models. These models define the expected data structure using JSON schema and are extremely helpful in understanding what data to send and expect.

            For details on the API, please check the tutorial on the Couchbase Developer Portal: https://developer.couchbase.com/tutorial-quickstart-kotlin-ktor
        """.trimIndent()
    }
  }

    // to support JSON serialization
    install(ContentNegotiation) {
        jackson()
    }

    routing {
    get("/openapi.json") {
      call.respond(application.openAPIGen.api.serialize())
    }

    get("/") {
      call.respondRedirect("/swagger-ui/index.html?url=/openapi.json", true)
    }
  }

    // Exposes application configuration as Koin bean
    fun applicationConfig(): ApplicationConfig {
        return environment.config
    }
    install(Koin) {
        slf4jLogger()
        modules(module {
            singleOf(::applicationConfig)
        })
        // database connection module
        modules(couchbaseModule)
        //airport operation module
        modules(airportModule)
        //airline operation module
        modules(airlineModule)
        //route operation module
        modules(routeModule)
    }


  apiRouting {
    airportRoutes()
    airlineRoutes()
    routeRoutes()
  }
}
