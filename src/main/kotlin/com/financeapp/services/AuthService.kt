package com.financeapp.services

import com.financeapp.models.AuthResponse
import com.financeapp.models.User
import com.financeapp.repositories.UserRepository
import com.financeapp.utils.ConflictException
import com.financeapp.utils.JwtManager
import com.financeapp.utils.PasswordHasher
import com.financeapp.utils.UnauthorizedException

class AuthService(
    private val userRepository: UserRepository,
    private val jwtManager: JwtManager,
    private val googleVerifier: com.financeapp.utils.GoogleAuthVerifier
) {

    suspend fun register(email: String, password: String, fullName: String): AuthResponse {
        // ... (validation code unchanged) ...
        validateEmail(email)
        validatePassword(password)
        validateName(fullName)

        // Check if email already exists
        if (userRepository.emailExists(email)) {
            throw ConflictException("Email already registered")
        }

        // Hash password and create user
        val hashedPassword = PasswordHasher.hash(password)
        val user = userRepository.create(email, hashedPassword, fullName)

        // Generate tokens
        val token = jwtManager.generateToken(user.id, user.email)
        val refreshToken = jwtManager.generateRefreshToken(user.id)

        return AuthResponse(token = token, refreshToken = refreshToken, user = user)
    }

    suspend fun login(email: String, password: String): AuthResponse {
        // Find user by email
        val result = userRepository.findByEmail(email)
            ?: throw UnauthorizedException("Invalid email or password")
        
        val user = result.first
        val hashedPassword = result.second

        // If user has no password (e.g. Google auth only), they can't login with password
        if (hashedPassword == null) {
             throw UnauthorizedException("Please sign in with Google")
        }

        // Verify password
        if (!PasswordHasher.verify(password, hashedPassword)) {
            throw UnauthorizedException("Invalid email or password")
        }

        // Generate tokens
        val token = jwtManager.generateToken(user.id, user.email)
        val refreshToken = jwtManager.generateRefreshToken(user.id)

        return AuthResponse(token = token, refreshToken = refreshToken, user = user)
    }

    suspend fun googleLogin(idToken: String): AuthResponse {
        val googleUser = googleVerifier.verify(idToken)
            ?: throw UnauthorizedException("Invalid Google ID Token")

        // Check if user exists
        val existing = userRepository.findByEmail(googleUser.email)
        
        val user = if (existing != null) {
            existing.first
        } else {
            // Register new user with null password
            userRepository.create(googleUser.email, null, googleUser.name)
        }

        // Generate tokens
        val token = jwtManager.generateToken(user.id, user.email)
        val refreshToken = jwtManager.generateRefreshToken(user.id)

        return AuthResponse(token = token, refreshToken = refreshToken, user = user)
    }

    suspend fun refreshToken(token: String): AuthResponse {
        val userId = jwtManager.verifyRefreshToken(token)
            ?: throw UnauthorizedException("Invalid or expired refresh token")

        val user = userRepository.findById(userId)
            ?: throw UnauthorizedException("User not found")

        val newAccessToken = jwtManager.generateToken(user.id, user.email)
        val newRefreshToken = jwtManager.generateRefreshToken(user.id)

        return AuthResponse(token = newAccessToken, refreshToken = newRefreshToken, user = user)
    }

    private fun validateEmail(email: String) {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        require(email.isNotBlank() && email.matches(emailRegex)) {
            "Invalid email format"
        }
    }

    private fun validatePassword(password: String) {
        require(password.length >= 8) {
            "Password must be at least 8 characters"
        }
    }

    private fun validateName(name: String) {
        require(name.isNotBlank() && name.length >= 2) {
            "Name must be at least 2 characters"
        }
    }
}
