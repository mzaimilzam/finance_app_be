package com.financeapp.repositories

import com.financeapp.database.DatabaseFactory.dbQuery
import com.financeapp.database.tables.CategoriesTable
import com.financeapp.database.tables.TransactionsTable
import com.financeapp.models.Category
import com.financeapp.models.CategoryType
import com.financeapp.models.CurrencySummary
import com.financeapp.models.Transaction as TransactionModel
import org.jetbrains.exposed.sql.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class TransactionRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    suspend fun findByUserId(
        userId: UUID,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        type: CategoryType? = null,
        page: Int = 1,
        limit: Int = 20
    ): Pair<List<TransactionModel>, Long> = dbQuery {
        var query = TransactionsTable
            .join(CategoriesTable, JoinType.LEFT, TransactionsTable.categoryId, CategoriesTable.id)
            .select { TransactionsTable.userId eq userId }

        // Apply date range filter
        startDate?.let { start ->
            query = query.andWhere { TransactionsTable.date greaterEq start }
        }
        endDate?.let { end ->
            query = query.andWhere { TransactionsTable.date lessEq end }
        }

        // Apply type filter
        type?.let { categoryType ->
            query = query.andWhere { CategoriesTable.type eq categoryType }
        }

        val total = query.count()

        val transactions = query
            .orderBy(TransactionsTable.date to SortOrder.DESC, TransactionsTable.createdAt to SortOrder.DESC)
            .limit(limit, offset = ((page - 1) * limit).toLong())
            .map { row -> rowToTransactionWithCategory(row) }

        Pair(transactions, total)
    }

    suspend fun findById(id: UUID): TransactionModel? = dbQuery {
        TransactionsTable
            .join(CategoriesTable, JoinType.LEFT, TransactionsTable.categoryId, CategoriesTable.id)
            .select { TransactionsTable.id eq id }
            .map { row -> rowToTransactionWithCategory(row) }
            .singleOrNull()
    }

    suspend fun create(
        userId: UUID,
        categoryId: UUID,
        amount: Double,
        currencyCode: String,
        note: String?,
        date: LocalDate
    ): TransactionModel = dbQuery {
        val insertStatement = TransactionsTable.insert {
            it[TransactionsTable.userId] = userId
            it[TransactionsTable.categoryId] = categoryId
            it[TransactionsTable.amount] = amount
            it[TransactionsTable.currencyCode] = currencyCode
            it[TransactionsTable.note] = note
            it[TransactionsTable.date] = date
        }

        val row = insertStatement.resultedValues?.first()
            ?: throw IllegalStateException("Failed to create transaction")

        rowToTransaction(row)
    }

    suspend fun update(
        id: UUID,
        categoryId: UUID?,
        amount: Double?,
        currencyCode: String?,
        note: String?,
        date: LocalDate?
    ): Boolean = dbQuery {
        val updateCount = TransactionsTable.update({ TransactionsTable.id eq id }) { statement ->
            categoryId?.let { cid -> statement[TransactionsTable.categoryId] = cid }
            amount?.let { a -> statement[TransactionsTable.amount] = a }
            currencyCode?.let { cc -> statement[TransactionsTable.currencyCode] = cc }
            note?.let { n -> statement[TransactionsTable.note] = n }
            date?.let { d -> statement[TransactionsTable.date] = d }
        }
        updateCount > 0
    }

    suspend fun delete(id: UUID): Boolean = dbQuery {
        val deleteCount = TransactionsTable.deleteWhere { Op.build { TransactionsTable.id eq id } }
        deleteCount > 0
    }

    suspend fun isOwnedByUser(id: UUID, userId: UUID): Boolean = dbQuery {
        TransactionsTable.select { (TransactionsTable.id eq id) and (TransactionsTable.userId eq userId) }
            .count() > 0
    }

    suspend fun getSummary(userId: UUID, startDate: LocalDate, endDate: LocalDate): List<CurrencySummary> = dbQuery {
        val transactions = TransactionsTable
            .join(CategoriesTable, JoinType.LEFT, TransactionsTable.categoryId, CategoriesTable.id)
            .slice(TransactionsTable.currencyCode, CategoriesTable.type, TransactionsTable.amount)
            .select { 
                (TransactionsTable.userId eq userId) and
                (TransactionsTable.date greaterEq startDate) and
                (TransactionsTable.date lessEq endDate)
            }
            .toList()

        // Group by currency and calculate totals
        transactions
            .groupBy { row: ResultRow -> row[TransactionsTable.currencyCode] }
            .map { (currency, rows) ->
                val income = rows
                    .filter { row: ResultRow -> row[CategoriesTable.type] == CategoryType.INCOME }
                    .sumOf { row: ResultRow -> row[TransactionsTable.amount] }
                val expense = rows
                    .filter { row: ResultRow -> row[CategoriesTable.type] == CategoryType.EXPENSE }
                    .sumOf { row: ResultRow -> row[TransactionsTable.amount] }

                CurrencySummary(
                    currency = currency,
                    income = income,
                    expense = expense,
                    balance = income - expense
                )
            }
    }

    suspend fun getRecent(userId: UUID, limit: Int = 5): List<TransactionModel> = dbQuery {
        TransactionsTable
            .join(CategoriesTable, JoinType.LEFT, TransactionsTable.categoryId, CategoriesTable.id)
            .select { TransactionsTable.userId eq userId }
            .orderBy(TransactionsTable.date to SortOrder.DESC, TransactionsTable.createdAt to SortOrder.DESC)
            .limit(limit)
            .map { row -> rowToTransactionWithCategory(row) }
    }

    private fun rowToTransaction(row: ResultRow): TransactionModel = TransactionModel(
        id = row[TransactionsTable.id],
        userId = row[TransactionsTable.userId],
        categoryId = row[TransactionsTable.categoryId],
        amount = row[TransactionsTable.amount],
        currencyCode = row[TransactionsTable.currencyCode],
        note = row[TransactionsTable.note],
        date = row[TransactionsTable.date].format(dateFormatter),
        createdAt = row[TransactionsTable.createdAt].format(dateTimeFormatter),
        category = null
    )

    private fun rowToTransactionWithCategory(row: ResultRow): TransactionModel = TransactionModel(
        id = row[TransactionsTable.id],
        userId = row[TransactionsTable.userId],
        categoryId = row[TransactionsTable.categoryId],
        amount = row[TransactionsTable.amount],
        currencyCode = row[TransactionsTable.currencyCode],
        note = row[TransactionsTable.note],
        date = row[TransactionsTable.date].format(dateFormatter),
        createdAt = row[TransactionsTable.createdAt].format(dateTimeFormatter),
        category = Category(
            id = row[CategoriesTable.id],
            userId = row[CategoriesTable.userId],
            name = row[CategoriesTable.name],
            type = row[CategoriesTable.type],
            iconCode = row[CategoriesTable.iconCode],
            isDeleted = row[CategoriesTable.isDeleted]
        )
    )
}
