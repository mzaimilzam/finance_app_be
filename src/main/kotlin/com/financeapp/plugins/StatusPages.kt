package com.financeapp.plugins

import com.financeapp.models.ErrorResponse
import com.financeapp.utils.AppException
import com.financeapp.utils.BadRequestException as AppBadRequestException
import com.financeapp.utils.UnauthorizedException as AppUnauthorizedException
import com.financeapp.utils.ForbiddenException as AppForbiddenException
import com.financeapp.utils.NotFoundException as AppNotFoundException
import com.financeapp.utils.ConflictException as AppConflictException
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException

fun Application.configureStatusPages() {
    install(StatusPages) {
        // Custom application exceptions
        exception<AppBadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Bad Request",
                    message = cause.message ?: "Invalid request",
                    status = 400
                )
            )
        }

        exception<AppUnauthorizedException> { call, cause ->
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(
                    error = "Unauthorized",
                    message = cause.message ?: "Authentication required",
                    status = 401
                )
            )
        }

        exception<AppForbiddenException> { call, cause ->
            call.respond(
                HttpStatusCode.Forbidden,
                ErrorResponse(
                    error = "Forbidden",
                    message = cause.message ?: "Access denied",
                    status = 403
                )
            )
        }

        exception<AppNotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    error = "Not Found",
                    message = cause.message ?: "Resource not found",
                    status = 404
                )
            )
        }

        exception<AppConflictException> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                ErrorResponse(
                    error = "Conflict",
                    message = cause.message ?: "Resource already exists",
                    status = 409
                )
            )
        }

        exception<AppException> { call, cause ->
            call.respond(
                HttpStatusCode.fromValue(cause.statusCode),
                ErrorResponse(
                    error = HttpStatusCode.fromValue(cause.statusCode).description,
                    message = cause.message,
                    status = cause.statusCode
                )
            )
        }

        // JSON serialization errors (missing fields, invalid format)
        exception<JsonConvertException> { call, cause ->
            val message = when {
                cause.cause is kotlinx.serialization.MissingFieldException -> {
                    val fieldMatch = Regex("Field '(\\w+)'").find(cause.cause?.message ?: "")
                    val fieldName = fieldMatch?.groupValues?.get(1) ?: "unknown"
                    "Missing required field: $fieldName"
                }
                else -> cause.message ?: "Invalid JSON format"
            }
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Bad Request",
                    message = message,
                    status = 400
                )
            )
        }

        // Serialization errors
        exception<SerializationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Bad Request",
                    message = "Invalid request body: ${cause.message}",
                    status = 400
                )
            )
        }

        // Content type errors
        exception<ContentTransformationException> { call, cause ->
            call.respond(
                HttpStatusCode.UnsupportedMediaType,
                ErrorResponse(
                    error = "Unsupported Media Type",
                    message = "Invalid content type. Expected: application/json",
                    status = 415
                )
            )
        }

        // Invalid parameter errors (path/query params)
        exception<ParameterConversionException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Bad Request",
                    message = "Invalid parameter '${cause.parameterName}': expected ${cause.type}",
                    status = 400
                )
            )
        }

        // Missing parameter errors
        exception<MissingRequestParameterException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Bad Request",
                    message = "Missing required parameter: ${cause.parameterName}",
                    status = 400
                )
            )
        }

        // Illegal argument (validation errors)
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Bad Request",
                    message = cause.message ?: "Invalid request",
                    status = 400
                )
            )
        }

        // Catch-all for unhandled exceptions
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception: ${cause.javaClass.simpleName}", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = "Internal Server Error",
                    message = "An unexpected error occurred. Please try again later.",
                    status = 500
                )
            )
        }

        // Status code handlers
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    error = "Not Found",
                    message = "The requested endpoint was not found",
                    status = 404
                )
            )
        }

        status(HttpStatusCode.Unauthorized) { call, _ ->
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(
                    error = "Unauthorized",
                    message = "Authentication required",
                    status = 401
                )
            )
        }

        status(HttpStatusCode.MethodNotAllowed) { call, _ ->
            call.respond(
                HttpStatusCode.MethodNotAllowed,
                ErrorResponse(
                    error = "Method Not Allowed",
                    message = "HTTP method not allowed for this endpoint",
                    status = 405
                )
            )
        }
    }
}
