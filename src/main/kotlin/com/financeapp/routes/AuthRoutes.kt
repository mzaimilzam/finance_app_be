package com.financeapp.routes

import com.financeapp.models.LoginRequest
import com.financeapp.models.RegisterRequest
import com.financeapp.services.AuthService
import com.financeapp.utils.BadRequestException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.authRoutes() {
    val authService by inject<AuthService>()

    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            
            if (request.email.isBlank() || request.password.isBlank() || request.fullName.isBlank()) {
                throw BadRequestException("Email, password, and full name are required")
            }

            val response = authService.register(
                email = request.email.trim().lowercase(),
                password = request.password,
                fullName = request.fullName.trim()
            )

            call.respond(HttpStatusCode.Created, response)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            if (request.email.isBlank() || request.password.isBlank()) {
                throw BadRequestException("Email and password are required")
            }

            val response = authService.login(
                email = request.email.trim().lowercase(),
                password = request.password
            )

            call.respond(HttpStatusCode.OK, response)
        }
    }
}
