package com.financeapp.routes

import com.financeapp.models.CreateTransactionRequest
import com.financeapp.models.UpdateTransactionRequest
import com.financeapp.plugins.getUserId
import com.financeapp.services.TransactionService
import com.financeapp.utils.BadRequestException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*

fun Route.transactionRoutes() {
    val transactionService by inject<TransactionService>()

    authenticate("auth-jwt") {
        route("/transactions") {
            // Get transactions with optional filters
            get {
                val userId = call.getUserId()
                val startDate = call.request.queryParameters["startDate"]
                val endDate = call.request.queryParameters["endDate"]
                val type = call.request.queryParameters["type"]
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20

                val response = transactionService.getTransactions(
                    userId = userId,
                    startDate = startDate,
                    endDate = endDate,
                    type = type,
                    page = page,
                    limit = limit
                )

                call.respond(HttpStatusCode.OK, response)
            }

            // Get transaction summary
            get("/summary") {
                val userId = call.getUserId()
                val startDate = call.request.queryParameters["startDate"]
                    ?: throw BadRequestException("startDate is required")
                val endDate = call.request.queryParameters["endDate"]
                    ?: throw BadRequestException("endDate is required")

                val response = transactionService.getSummary(userId, startDate, endDate)
                call.respond(HttpStatusCode.OK, response)
            }

            // Get recent transactions
            get("/recent") {
                val userId = call.getUserId()
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 5

                val transactions = transactionService.getRecentTransactions(userId, limit)
                call.respond(HttpStatusCode.OK, transactions)
            }

            // Create transaction
            post {
                val userId = call.getUserId()
                val request = call.receive<CreateTransactionRequest>()

                val transaction = transactionService.createTransaction(userId, request)
                call.respond(HttpStatusCode.Created, transaction)
            }

            // Get single transaction
            get("/{id}") {
                val id = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw BadRequestException("Invalid transaction ID")

                val transaction = transactionService.getTransactionById(id)
                call.respond(HttpStatusCode.OK, transaction)
            }

            // Update transaction
            put("/{id}") {
                val userId = call.getUserId()
                val id = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw BadRequestException("Invalid transaction ID")

                val request = call.receive<UpdateTransactionRequest>()
                val transaction = transactionService.updateTransaction(id, userId, request)

                call.respond(HttpStatusCode.OK, transaction)
            }

            // Delete transaction
            delete("/{id}") {
                val userId = call.getUserId()
                val id = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw BadRequestException("Invalid transaction ID")

                transactionService.deleteTransaction(id, userId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
