package com.financeapp.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Transaction(
    @Contextual
    val id: UUID,
    @Contextual
    val userId: UUID,
    @Contextual
    val categoryId: UUID,
    val amount: Double,
    val currencyCode: String,
    val note: String?,
    val date: String,
    val createdAt: String,
    val category: Category? = null
)

@Serializable
data class CreateTransactionRequest(
    @Contextual
    val categoryId: UUID,
    val amount: Double,
    val currencyCode: String,
    val note: String? = null,
    val date: String
)

@Serializable
data class UpdateTransactionRequest(
    @Contextual
    val categoryId: UUID? = null,
    val amount: Double? = null,
    val currencyCode: String? = null,
    val note: String? = null,
    val date: String? = null
)

@Serializable
data class TransactionListResponse(
    val transactions: List<Transaction>,
    val page: Int,
    val limit: Int,
    val total: Long
)

@Serializable
data class CurrencySummary(
    val currency: String,
    val income: Double,
    val expense: Double,
    val balance: Double
)

@Serializable
data class TransactionSummaryResponse(
    val summaries: List<CurrencySummary>
)
