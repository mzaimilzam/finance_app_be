package com.financeapp.di

import com.financeapp.repositories.CategoryRepository
import com.financeapp.repositories.TransactionRepository
import com.financeapp.repositories.UserRepository
import com.financeapp.services.AuthService
import com.financeapp.services.CategoryService
import com.financeapp.services.TransactionService
import com.financeapp.utils.JwtManager
import io.ktor.server.application.*
import org.koin.dsl.module

fun appModule(application: Application) = module {
    // JWT Manager
    single { JwtManager(application.environment.config) }

    // Repositories
    single { UserRepository() }
    single { CategoryRepository() }
    single { TransactionRepository() }

    // Utils
    single { com.financeapp.utils.GoogleAuthVerifier(application.environment.config) }

    // Services
    single { AuthService(get(), get(), get()) }
    single { CategoryService(get()) }
    single { TransactionService(get()) }
}
