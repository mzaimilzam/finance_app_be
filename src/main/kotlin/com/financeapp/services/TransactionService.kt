package com.financeapp.services

import com.financeapp.models.*
import com.financeapp.repositories.TransactionRepository
import com.financeapp.utils.BadRequestException
import com.financeapp.utils.ForbiddenException
import com.financeapp.utils.NotFoundException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID

class TransactionService(
    private val transactionRepository: TransactionRepository
) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun getTransactions(
        userId: UUID,
        startDate: String?,
        endDate: String?,
        type: String?,
        page: Int = 1,
        limit: Int = 20
    ): TransactionListResponse {
        val parsedStartDate = startDate?.let { parseDate(it) }
        val parsedEndDate = endDate?.let { parseDate(it) }
        val parsedType = type?.takeIf { it.uppercase() != "ALL" }?.let { 
            try {
                CategoryType.valueOf(it.uppercase())
            } catch (e: IllegalArgumentException) {
                throw BadRequestException("Invalid type. Must be INCOME, EXPENSE, or ALL")
            }
        }

        val validPage = maxOf(1, page)
        val validLimit = minOf(100, maxOf(1, limit))

        val (transactions, total) = transactionRepository.findByUserId(
            userId = userId,
            startDate = parsedStartDate,
            endDate = parsedEndDate,
            type = parsedType,
            page = validPage,
            limit = validLimit
        )

        return TransactionListResponse(
            transactions = transactions,
            page = validPage,
            limit = validLimit,
            total = total
        )
    }

    suspend fun getTransactionById(id: UUID): Transaction {
        return transactionRepository.findById(id)
            ?: throw NotFoundException("Transaction not found")
    }

    suspend fun createTransaction(
        userId: UUID,
        request: CreateTransactionRequest
    ): Transaction {
        require(request.amount > 0) { "Amount must be positive" }
        require(request.currencyCode.isNotBlank()) { "Currency code is required" }

        val date = parseDate(request.date)

        val transaction = transactionRepository.create(
            userId = userId,
            categoryId = request.categoryId,
            amount = request.amount,
            currencyCode = request.currencyCode.uppercase().trim(),
            note = request.note?.trim(),
            date = date
        )

        // Fetch with category for response
        return transactionRepository.findById(transaction.id)!!
    }

    suspend fun updateTransaction(
        id: UUID,
        userId: UUID,
        request: UpdateTransactionRequest
    ): Transaction {
        // Verify ownership
        if (!transactionRepository.isOwnedByUser(id, userId)) {
            throw ForbiddenException("You don't have permission to update this transaction")
        }

        request.amount?.let {
            require(it > 0) { "Amount must be positive" }
        }

        val date = request.date?.let { parseDate(it) }

        val updated = transactionRepository.update(
            id = id,
            categoryId = request.categoryId,
            amount = request.amount,
            currencyCode = request.currencyCode?.uppercase()?.trim(),
            note = request.note?.trim(),
            date = date
        )

        if (!updated) {
            throw NotFoundException("Transaction not found")
        }

        return transactionRepository.findById(id)!!
    }

    suspend fun deleteTransaction(id: UUID, userId: UUID): Boolean {
        // Verify ownership
        if (!transactionRepository.isOwnedByUser(id, userId)) {
            throw ForbiddenException("You don't have permission to delete this transaction")
        }

        return transactionRepository.delete(id)
    }

    suspend fun getSummary(
        userId: UUID,
        startDate: String,
        endDate: String
    ): TransactionSummaryResponse {
        val parsedStartDate = parseDate(startDate)
        val parsedEndDate = parseDate(endDate)

        if (parsedStartDate.isAfter(parsedEndDate)) {
            throw BadRequestException("Start date cannot be after end date")
        }

        val summaries = transactionRepository.getSummary(userId, parsedStartDate, parsedEndDate)
        return TransactionSummaryResponse(summaries = summaries)
    }

    suspend fun getRecentTransactions(userId: UUID, limit: Int = 5): List<Transaction> {
        val validLimit = minOf(20, maxOf(1, limit))
        return transactionRepository.getRecent(userId, validLimit)
    }

    private fun parseDate(dateString: String): LocalDate {
        return try {
            LocalDate.parse(dateString, dateFormatter)
        } catch (e: DateTimeParseException) {
            throw BadRequestException("Invalid date format. Use YYYY-MM-DD")
        }
    }
}
