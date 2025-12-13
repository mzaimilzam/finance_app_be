package com.financeapp.plugins

import com.financeapp.models.ErrorResponse
import com.financeapp.utils.JwtManager
import com.financeapp.utils.UnauthorizedException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import java.util.*

fun Application.configureSecurity() {
    val jwtManager by inject<JwtManager>()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtManager.realm
            verifier(jwtManager.verifier)
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                val email = credential.payload.getClaim("email").asString()
                
                if (userId != null && email != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        error = "Unauthorized",
                        message = "Token is invalid or expired",
                        status = 401
                    )
                )
            }
        }
    }
}

fun ApplicationCall.getUserId(): UUID {
    val principal = principal<JWTPrincipal>()
        ?: throw UnauthorizedException("Not authenticated")
    val userIdStr = principal.payload.getClaim("userId").asString()
        ?: throw UnauthorizedException("Invalid token")
    return UUID.fromString(userIdStr)
}
