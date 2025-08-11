package com.example.library.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

/**
 * グローバル例外ハンドラー
 * アプリケーション全体の例外を統一的に処理する
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    /**
     * リソースが見つからない場合
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    code = "RESOURCE_NOT_FOUND",
                    message = ex.message,
                    details = null,
                ),
            )
    }

    /**
     * ビジネスルール違反
     */
    @ExceptionHandler(BusinessRuleViolationException::class)
    fun handleBusinessRuleViolation(ex: BusinessRuleViolationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    code = "BUSINESS_RULE_VIOLATION",
                    message = ex.message,
                    details = null,
                ),
            )
    }

    /**
     * 重複リソース例外
     */
    @ExceptionHandler(DuplicateResourceException::class)
    fun handleDuplicateResource(ex: DuplicateResourceException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(
                ErrorResponse(
                    code = "DUPLICATE_RESOURCE",
                    message = ex.message,
                    details = null,
                ),
            )
    }

    /**
     * Bean Validation例外
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val fieldErrors =
            ex.bindingResult.fieldErrors.associate { fieldError ->
                fieldError.field to (fieldError.defaultMessage ?: "Invalid value")
            }

        val errorMessage = fieldErrors.entries.joinToString(", ") { "${it.key}: ${it.value}" }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    code = "VALIDATION_ERROR",
                    message = errorMessage,
                    details = fieldErrors,
                ),
            )
    }

    /**
     * カスタムバリデーション例外
     */
    @ExceptionHandler(ValidationException::class)
    fun handleCustomValidationError(ex: ValidationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    code = "VALIDATION_ERROR",
                    message = ex.message,
                    details = ex.fieldErrors.ifEmpty { null },
                ),
            )
    }

    /**
     * IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    code = "INVALID_ARGUMENT",
                    message = ex.message ?: "Invalid argument provided",
                    details = null,
                ),
            )
    }

    /**
     * IllegalStateException
     */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    code = "INVALID_STATE",
                    message = ex.message ?: "Invalid state operation",
                    details = null,
                ),
            )
    }

    /**
     * その他の予期しない例外
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        // 本番環境では詳細なエラー情報を隠す
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    code = "INTERNAL_SERVER_ERROR",
                    message = "Internal server error occurred",
                    details = null,
                ),
            )
    }
}

/**
 * API エラーレスポンス
 */
data class ErrorResponse(
    /** エラーコード */
    val code: String,
    /** エラーメッセージ */
    val message: String?,
    /** 詳細情報（フィールドエラーなど） */
    val details: Any?,
    /** タイムスタンプ */
    val timestamp: LocalDateTime = LocalDateTime.now(),
)
