package com.financeapp.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.util.*

class JwtManager(config: ApplicationConfig) {
    
    private val secret = config.property("jwt.secret").getString()
    private val issuer = config.property("jwt.issuer").getString()
    private val audience = config.property("jwt.audience").getString()
    private val expirationMs = config.property("jwt.expirationMs").getString().toLong()

    val realm = config.property("jwt.realm").getString()

    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withAudience(audience)
        .withIssuer(issuer)
        .build()

    fun generateToken(userId: UUID, email: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId.toString())
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + expirationMs))
            .sign(algorithm)
    }

    fun generateRefreshToken(userId: UUID): String {
        // Refresh token valid for 30 days
        val refreshExpirationMs = 30L * 24 * 60 * 60 * 1000
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId.toString())
            .withClaim("type", "refresh")
            .withExpiresAt(Date(System.currentTimeMillis() + refreshExpirationMs))
            .sign(algorithm)
    }

    fun verifyRefreshToken(token: String): UUID? {
        return try {
            val decodedJWT = verifier.verify(token)
            val type = decodedJWT.getClaim("type").asString()
            if (type == "refresh") {
                UUID.fromString(decodedJWT.getClaim("userId").asString())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
