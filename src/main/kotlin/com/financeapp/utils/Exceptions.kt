package com.financeapp.utils

open class AppException(
    val statusCode: Int,
    override val message: String
) : RuntimeException(message)

class BadRequestException(message: String) : AppException(400, message)
class UnauthorizedException(message: String = "Unauthorized") : AppException(401, message)
class ForbiddenException(message: String = "Forbidden") : AppException(403, message)
class NotFoundException(message: String) : AppException(404, message)
class ConflictException(message: String) : AppException(409, message)
