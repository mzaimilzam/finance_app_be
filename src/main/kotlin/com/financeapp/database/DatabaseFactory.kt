package com.financeapp.database

import com.financeapp.database.tables.CategoriesTable
import com.financeapp.database.tables.TransactionsTable
import com.financeapp.database.tables.UsersTable
import com.financeapp.models.CategoryType
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val driverClassName = config.property("database.driver").getString()
        val jdbcURL = config.property("database.url").getString()
        val dbUser = config.property("database.user").getString()
        val dbPassword = config.property("database.password").getString()
        val maxPoolSize = config.property("database.maxPoolSize").getString().toInt()

        // Log database configuration (password masked)
        println("╔════════════════════════════════════════════════════════════════╗")
        println("║              DATABASE CONFIGURATION                            ║")
        println("╠════════════════════════════════════════════════════════════════╣")
        println("║ Driver:    $driverClassName")
        println("║ URL:       $jdbcURL")
        println("║ User:      $dbUser")
        println("║ Password:  ${"*".repeat(dbPassword.length.coerceAtMost(10))}")
        println("║ Pool Size: $maxPoolSize")
        println("╚════════════════════════════════════════════════════════════════╝")

        val hikariConfig = HikariConfig().apply {
            this.driverClassName = driverClassName
            this.jdbcUrl = jdbcURL
            this.username = dbUser
            this.password = dbPassword
            this.maximumPoolSize = maxPoolSize
            this.isAutoCommit = false
            this.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)

        println("✅ Database connected successfully!")

        transaction {
            SchemaUtils.create(UsersTable, CategoriesTable, TransactionsTable)
            seedDefaultCategories()
        }
        
        println("✅ Database schema initialized!")
    }

    private fun seedDefaultCategories() {
        // Check if default categories already exist
        val existingDefaults = CategoriesTable
            .select { CategoriesTable.userId.isNull() }
            .count()

        if (existingDefaults > 0) return

        // Seed default income categories
        val incomeCategories = listOf(
            "Salary" to "ic_salary",
            "Freelance" to "ic_freelance",
            "Investment" to "ic_investment",
            "Gift" to "ic_gift",
            "Other Income" to "ic_other_income"
        )

        incomeCategories.forEach { (name, icon) ->
            CategoriesTable.insert {
                it[this.name] = name
                it[type] = CategoryType.INCOME
                it[iconCode] = icon
                it[userId] = null
                it[isDeleted] = false
            }
        }

        // Seed default expense categories
        val expenseCategories = listOf(
            "Food & Dining" to "ic_food",
            "Transportation" to "ic_transport",
            "Shopping" to "ic_shopping",
            "Entertainment" to "ic_entertainment",
            "Bills & Utilities" to "ic_bills",
            "Healthcare" to "ic_health",
            "Education" to "ic_education",
            "Travel" to "ic_travel",
            "Other Expense" to "ic_other_expense"
        )

        expenseCategories.forEach { (name, icon) ->
            CategoriesTable.insert {
                it[this.name] = name
                it[type] = CategoryType.EXPENSE
                it[iconCode] = icon
                it[userId] = null
                it[isDeleted] = false
            }
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
