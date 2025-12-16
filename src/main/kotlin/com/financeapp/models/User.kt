package com.financeapp.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class User(
    @Contextual
    val id: UUID,
    val email: String,
    val fullName: String,
    val createdAt: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val user: User
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class GoogleLoginRequest(
    val idToken: String
)
