package com.couchbase.kotlin.quickstart

import com.papsign.ktor.openapigen.OpenAPIGen
import com.papsign.ktor.openapigen.openAPIGen
import com.papsign.ktor.openapigen.route.apiRouting
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
  install(ContentNegotiation) {
    jackson()
  }
  install(OpenAPIGen) {
    info {
      version = "0.0.1"
      title = "Kotlin Couchbase Example API"
      description = "Example REST API built with Kotlin Couchbase SDK"
    }
  }
  routing {
    get("/openapi.json") {
      call.respond(application.openAPIGen.api.serialize())
    }
    get("/") {
      call.respondRedirect("/swagger-ui/index.html?url=/openapi.json", true)
    }
  }
  fun applicationConfig():ApplicationConfig {
    return environment.config
  }
  install(Koin) {
    slf4jLogger()
    modules(module {
      singleOf(::applicationConfig)
    })
    modules(couchbaseModule)
    modules(profileModule)
  }

  apiRouting {
    profileRoutes()
  }
}
