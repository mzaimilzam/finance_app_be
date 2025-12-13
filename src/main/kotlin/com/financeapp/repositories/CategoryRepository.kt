package com.financeapp.repositories

import com.financeapp.database.DatabaseFactory.dbQuery
import com.financeapp.database.tables.CategoriesTable
import com.financeapp.models.Category
import com.financeapp.models.CategoryType
import org.jetbrains.exposed.sql.*
import java.util.UUID

class CategoryRepository {

    suspend fun findAllForUser(userId: UUID): List<Category> = dbQuery {
        CategoriesTable.select { 
            (CategoriesTable.userId.isNull() or (CategoriesTable.userId eq userId)) and 
            (CategoriesTable.isDeleted eq false) 
        }
        .orderBy(CategoriesTable.name to SortOrder.ASC)
        .map { row -> rowToCategory(row) }
    }

    suspend fun findById(id: UUID): Category? = dbQuery {
        CategoriesTable.select { CategoriesTable.id eq id }
            .map { row -> rowToCategory(row) }
            .singleOrNull()
    }

    suspend fun create(userId: UUID, name: String, type: CategoryType, iconCode: String): Category = dbQuery {
        val insertStatement = CategoriesTable.insert {
            it[CategoriesTable.userId] = userId
            it[CategoriesTable.name] = name
            it[CategoriesTable.type] = type
            it[CategoriesTable.iconCode] = iconCode
            it[CategoriesTable.isDeleted] = false
        }

        val row = insertStatement.resultedValues?.first()
            ?: throw IllegalStateException("Failed to create category")

        rowToCategory(row)
    }

    suspend fun update(id: UUID, name: String?, type: CategoryType?, iconCode: String?): Boolean = dbQuery {
        val updateCount = CategoriesTable.update({ CategoriesTable.id eq id }) { statement ->
            name?.let { n -> statement[CategoriesTable.name] = n }
            type?.let { t -> statement[CategoriesTable.type] = t }
            iconCode?.let { ic -> statement[CategoriesTable.iconCode] = ic }
        }
        updateCount > 0
    }

    suspend fun softDelete(id: UUID): Boolean = dbQuery {
        val updateCount = CategoriesTable.update({ CategoriesTable.id eq id }) { statement ->
            statement[CategoriesTable.isDeleted] = true
        }
        updateCount > 0
    }

    private fun rowToCategory(row: ResultRow): Category = Category(
        id = row[CategoriesTable.id],
        userId = row[CategoriesTable.userId],
        name = row[CategoriesTable.name],
        type = row[CategoriesTable.type],
        iconCode = row[CategoriesTable.iconCode],
        isDeleted = row[CategoriesTable.isDeleted]
    )
}
