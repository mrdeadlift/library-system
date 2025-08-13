package com.example.library.exception

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import java.time.LocalDateTime

/**
 * GlobalExceptionHandlerの単体テスト
 */
class GlobalExceptionHandlerTest {
    private lateinit var globalExceptionHandler: GlobalExceptionHandler

    @BeforeEach
    fun setup() {
        globalExceptionHandler = GlobalExceptionHandler()
    }

    @Test
    fun `handleResourceNotFound - ResourceNotFoundExceptionに対して404とエラーレスポンス返却`() {
        // Given
        val exception = ResourceNotFoundException("指定されたID=1 の書籍が見つかりません")

        // When
        val response = globalExceptionHandler.handleResourceNotFound(exception)

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)

        val errorResponse = response.body!!
        assertEquals("RESOURCE_NOT_FOUND", errorResponse.code)
        assertEquals("指定されたID=1 の書籍が見つかりません", errorResponse.message)
        assertNull(errorResponse.details)
        assertNotNull(errorResponse.timestamp)
    }

    @Test
    fun `handleBusinessRuleViolation - BusinessRuleViolationExceptionに対して400とエラーレスポンス返却`() {
        // Given
        val exception = BusinessRuleViolationException("ビジネスルール違反が発生しました")

        // When
        val response = globalExceptionHandler.handleBusinessRuleViolation(exception)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)

        val errorResponse = response.body!!
        assertEquals("BUSINESS_RULE_VIOLATION", errorResponse.code)
        assertEquals("ビジネスルール違反が発生しました", errorResponse.message)
        assertNull(errorResponse.details)
        assertNotNull(errorResponse.timestamp)
    }

    @Test
    fun `handleBusinessRuleViolation - InvalidPublicationStatusTransitionExceptionの処理`() {
        // Given
        val exception = InvalidPublicationStatusTransitionException("PUBLISHED", "UNPUBLISHED")

        // When
        val response = globalExceptionHandler.handleBusinessRuleViolation(exception)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)

        val errorResponse = response.body!!
        assertEquals("BUSINESS_RULE_VIOLATION", errorResponse.code)
        assertEquals(
            "出版状況をPUBLISHEDからUNPUBLISHEDに変更することはできません。出版済みの書籍を未出版に戻すことはできません。",
            errorResponse.message,
        )
        assertNull(errorResponse.details)
    }

    @Test
    fun `handleDuplicateResource - DuplicateResourceExceptionに対して409とエラーレスポンス返却`() {
        // Given
        val exception = DuplicateResourceException("書籍タイトル「ノルウェイの森」は既に登録されています")

        // When
        val response = globalExceptionHandler.handleDuplicateResource(exception)

        // Then
        assertEquals(HttpStatus.CONFLICT, response.statusCode)

        val errorResponse = response.body!!
        assertEquals("DUPLICATE_RESOURCE", errorResponse.code)
        assertEquals("書籍タイトル「ノルウェイの森」は既に登録されています", errorResponse.message)
        assertNull(errorResponse.details)
        assertNotNull(errorResponse.timestamp)
    }

    @Test
    fun `handleValidationError - MethodArgumentNotValidExceptionに対して400とバリデーションエラー返却`() {
        // Given
        val target = TestRequestClass("", -100)
        val bindingResult = BeanPropertyBindingResult(target, "testRequest")

        bindingResult.addError(FieldError("testRequest", "title", "", false, null, null, "タイトルは必須です"))
        bindingResult.addError(
            FieldError(
                "testRequest",
                "price",
                -100,
                false,
                null,
                null,
                "価格は0以上である必要があります",
            ),
        )

        // Create a fake MethodParameter using a constructor
        val constructor = TestRequestClass::class.java.constructors[0]
        val methodParameter = MethodParameter(constructor, 0)

        val exception = MethodArgumentNotValidException(methodParameter, bindingResult)

        // When
        val response = globalExceptionHandler.handleValidationError(exception)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)

        val errorResponse = response.body!!
        assertEquals("VALIDATION_ERROR", errorResponse.code)
        assertEquals("title: タイトルは必須です, price: 価格は0以上である必要があります", errorResponse.message)

        @Suppress("UNCHECKED_CAST")
        val details = errorResponse.details as Map<String, String>
        assertEquals(2, details.size)
        assertEquals("タイトルは必須です", details["title"])
        assertEquals("価格は0以上である必要があります", details["price"])
        assertNotNull(errorResponse.timestamp)
    }

    @Test
    fun `handleValidationError - フィールドエラーが空の場合`() {
        // Given
        val target = TestRequestClass("valid", 100)
        val bindingResult = BeanPropertyBindingResult(target, "testRequest")

        // Create a fake MethodParameter using a constructor
        val constructor = TestRequestClass::class.java.constructors[0]
        val methodParameter = MethodParameter(constructor, 0)

        val exception = MethodArgumentNotValidException(methodParameter, bindingResult)

        // When
        val response = globalExceptionHandler.handleValidationError(exception)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)

        val errorResponse = response.body!!
        assertEquals("VALIDATION_ERROR", errorResponse.code)
        assertEquals("", errorResponse.message)

        @Suppress("UNCHECKED_CAST")
        val details = errorResponse.details as Map<String, String>
        assertEquals(0, details.size)
    }

    @Test
    fun `handleCustomValidationError - ValidationExceptionに対して400とエラーレスポンス返却`() {
        // Given
        val fieldErrors =
            mapOf(
                "authorIds" to "著者IDリストが空です",
                "publicationStatus" to "無効な出版状態です",
            )
        val exception = ValidationException("カスタムバリデーションエラー", fieldErrors)

        // When
        val response = globalExceptionHandler.handleCustomValidationError(exception)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)

        val errorResponse = response.body!!
        assertEquals("VALIDATION_ERROR", errorResponse.code)
        assertEquals("カスタムバリデーションエラー", errorResponse.message)
        assertEquals(fieldErrors, errorResponse.details)
        assertNotNull(errorResponse.timestamp)
    }

    @Test
    fun `handleCustomValidationError - フィールドエラーが空の場合`() {
        // Given
        val exception = ValidationException("カスタムバリデーションエラー", emptyMap())

        // When
        val response = globalExceptionHandler.handleCustomValidationError(exception)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)

        val errorResponse = response.body!!
        assertEquals("VALIDATION_ERROR", errorResponse.code)
        assertEquals("カスタムバリデーションエラー", errorResponse.message)
        assertNull(errorResponse.details)
    }

    @Test
    fun `handleIllegalArgument - IllegalArgumentExceptionに対して400とエラーレスポンス返却`() {
        // Given
        val exception = IllegalArgumentException("無効な引数が指定されました")

        // When
        val response = globalExceptionHandler.handleIllegalArgument(exception)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)

        val errorResponse = response.body!!
        assertEquals("INVALID_ARGUMENT", errorResponse.code)
        assertEquals("無効な引数が指定されました", errorResponse.message)
        assertNull(errorResponse.details)
        assertNotNull(errorResponse.timestamp)
    }

    @Test
    fun `handleIllegalArgument - メッセージがnullの場合デフォルトメッセージ使用`() {
        // Given
        val exception = IllegalArgumentException(null as String?)

        // When
        val response = globalExceptionHandler.handleIllegalArgument(exception)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)

        val errorResponse = response.body!!
        assertEquals("INVALID_ARGUMENT", errorResponse.code)
        assertEquals("Invalid argument provided", errorResponse.message)
        assertNull(errorResponse.details)
    }

    @Test
    fun `handleIllegalState - IllegalStateExceptionに対して400とエラーレスポンス返却`() {
        // Given
        val exception = IllegalStateException("無効な状態操作です")

        // When
        val response = globalExceptionHandler.handleIllegalState(exception)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)

        val errorResponse = response.body!!
        assertEquals("INVALID_STATE", errorResponse.code)
        assertEquals("無効な状態操作です", errorResponse.message)
        assertNull(errorResponse.details)
        assertNotNull(errorResponse.timestamp)
    }

    @Test
    fun `handleIllegalState - メッセージがnullの場合デフォルトメッセージ使用`() {
        // Given
        val exception = IllegalStateException(null as String?)

        // When
        val response = globalExceptionHandler.handleIllegalState(exception)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)

        val errorResponse = response.body!!
        assertEquals("INVALID_STATE", errorResponse.code)
        assertEquals("Invalid state operation", errorResponse.message)
        assertNull(errorResponse.details)
    }

    @Test
    fun `handleGenericException - 予期しない例外に対して500とエラーレスポンス返却`() {
        // Given
        val exception = RuntimeException("予期しないエラーが発生しました")

        // When
        val response = globalExceptionHandler.handleGenericException(exception)

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)

        val errorResponse = response.body!!
        assertEquals("INTERNAL_SERVER_ERROR", errorResponse.code)
        assertEquals("Internal server error occurred", errorResponse.message)
        assertNull(errorResponse.details)
        assertNotNull(errorResponse.timestamp)
    }

    @Test
    fun `handleGenericException - NullPointerExceptionの処理`() {
        // Given
        val exception = NullPointerException("Null pointer exception occurred")

        // When
        val response = globalExceptionHandler.handleGenericException(exception)

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)

        val errorResponse = response.body!!
        assertEquals("INTERNAL_SERVER_ERROR", errorResponse.code)
        assertEquals("Internal server error occurred", errorResponse.message)
        assertNull(errorResponse.details)
    }

    @Test
    fun `ErrorResponse - タイムスタンプが自動設定される`() {
        // Given
        val beforeTimestamp = LocalDateTime.now().minusSeconds(1)

        // When
        val errorResponse =
            ErrorResponse(
                code = "TEST_CODE",
                message = "テストメッセージ",
                details = null,
            )

        val afterTimestamp = LocalDateTime.now().plusSeconds(1)

        // Then
        assertEquals("TEST_CODE", errorResponse.code)
        assertEquals("テストメッセージ", errorResponse.message)
        assertNull(errorResponse.details)
        assertNotNull(errorResponse.timestamp)
        assert(errorResponse.timestamp.isAfter(beforeTimestamp))
        assert(errorResponse.timestamp.isBefore(afterTimestamp))
    }

    @Test
    fun `ErrorResponse - 手動でタイムスタンプを設定`() {
        // Given
        val customTimestamp = LocalDateTime.of(2024, 1, 1, 12, 0, 0)

        // When
        val errorResponse =
            ErrorResponse(
                code = "TEST_CODE",
                message = "テストメッセージ",
                details = mapOf("field" to "value"),
                timestamp = customTimestamp,
            )

        // Then
        assertEquals("TEST_CODE", errorResponse.code)
        assertEquals("テストメッセージ", errorResponse.message)
        assertEquals(mapOf("field" to "value"), errorResponse.details)
        assertEquals(customTimestamp, errorResponse.timestamp)
    }

    /**
     * テスト用のダミークラス
     */
    private data class TestRequestClass(
        val title: String,
        val price: Int,
    )
}
