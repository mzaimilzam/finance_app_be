package com.financeapp.plugins

import com.financeapp.routes.authRoutes
import com.financeapp.routes.categoryRoutes
import com.financeapp.routes.transactionRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.event.Level

fun Application.configureRouting() {
    install(CallLogging) {
        level = Level.INFO
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost() // For development - restrict in production
    }

    routing {
        get("/") {
            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "message" to "Finance App API",
                    "version" to "1.0.0",
                    "status" to "running"
                )
            )
        }

        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                mapOf("status" to "healthy")
            )
        }

        authRoutes()
        categoryRoutes()
        transactionRoutes()
    }
}
