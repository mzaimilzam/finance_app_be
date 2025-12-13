package com.financeapp

import com.financeapp.database.DatabaseFactory
import com.financeapp.di.appModule
import com.financeapp.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    // Initialize Koin DI
    install(Koin) {
        slf4jLogger()
        modules(appModule(this@module))
    }

    // Initialize database
    DatabaseFactory.init(environment.config)

    // Configure plugins
    configureSerialization()
    configureStatusPages()
    configureSecurity()
    configureRouting()

    log.info("Finance App Server started successfully!")
}
