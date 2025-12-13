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
    private val jwtManager: JwtManager
) {

    suspend fun register(email: String, password: String, fullName: String): AuthResponse {
        // Validate input
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

        // Generate JWT token
        val token = jwtManager.generateToken(user.id, user.email)

        return AuthResponse(token = token, user = user)
    }

    suspend fun login(email: String, password: String): AuthResponse {
        // Find user by email
        val (user, hashedPassword) = userRepository.findByEmail(email)
            ?: throw UnauthorizedException("Invalid email or password")

        // Verify password
        if (!PasswordHasher.verify(password, hashedPassword)) {
            throw UnauthorizedException("Invalid email or password")
        }

        // Generate JWT token
        val token = jwtManager.generateToken(user.id, user.email)

        return AuthResponse(token = token, user = user)
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
