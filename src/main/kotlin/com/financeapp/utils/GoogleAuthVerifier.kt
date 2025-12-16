package com.financeapp.utils

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import io.ktor.server.config.*
import java.util.Collections

class GoogleAuthVerifier(config: ApplicationConfig) {
    // We don't strictly enforcing Audience in the verifier here to make it easier for testing 
    // from different clients (Web/Android/iOS), but in production you should list all client IDs.
    private val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
        .build()

    fun verify(idTokenString: String): GoogleUser? {
        try {
            val idToken: GoogleIdToken? = verifier.verify(idTokenString)
            if (idToken != null) {
                val payload = idToken.payload
                val email = payload.email
                val name = payload["name"] as? String ?: "Google User"
                
                if (email != null) {
                    return GoogleUser(email, name)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

data class GoogleUser(
    val email: String,
    val name: String
)
