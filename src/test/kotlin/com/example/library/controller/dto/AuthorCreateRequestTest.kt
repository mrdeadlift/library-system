package com.example.library.controller.dto

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * AuthorCreateRequest DTOのバリデーションテストクラス
 */
class AuthorCreateRequestTest {
    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    @Test
    fun `正常なリクエストデータでバリデーションエラーなし`() {
        // Given
        val request =
            AuthorCreateRequest(
                name = "太郎作家",
                birthDate = LocalDate.of(1980, 5, 15),
            )

        // When
        val violations: Set<ConstraintViolation<AuthorCreateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "バリデーションエラーがないことを期待")
    }

    @Test
    fun `著者名がnull の場合バリデーションエラー`() {
        // Given
        // Note: Kotlinではdata classでval nameがnon-nullなので、
        // この場合はコンパイル時エラーになる
    }

    @Test
    fun `著者名が空文字の場合バリデーションエラー`() {
        // Given
        val request =
            AuthorCreateRequest(
                name = "",
                birthDate = LocalDate.of(1980, 5, 15),
            )

        // When
        val violations: Set<ConstraintViolation<AuthorCreateRequest>> = validator.validate(request)

        // Then
        assertEquals(1, violations.size)
        val violation = violations.iterator().next()
        assertEquals("name", violation.propertyPath.toString())
        assertEquals("著者名は必須です", violation.message)
    }

    @Test
    fun `著者名が空白のみの場合バリデーションエラー`() {
        // Given
        val request =
            AuthorCreateRequest(
                name = "   ",
                birthDate = LocalDate.of(1980, 5, 15),
            )

        // When
        val violations: Set<ConstraintViolation<AuthorCreateRequest>> = validator.validate(request)

        // Then
        assertEquals(1, violations.size)
        val violation = violations.iterator().next()
        assertEquals("name", violation.propertyPath.toString())
        assertEquals("著者名は必須です", violation.message)
    }

    @Test
    fun `生年月日が未来の日付の場合バリデーションエラー`() {
        // Given
        val futureDate = LocalDate.now().plusDays(1)
        val request =
            AuthorCreateRequest(
                name = "未来人作家",
                birthDate = futureDate,
            )

        // When
        val violations: Set<ConstraintViolation<AuthorCreateRequest>> = validator.validate(request)

        // Then
        assertEquals(1, violations.size)
        val violation = violations.iterator().next()
        assertEquals("birthDate", violation.propertyPath.toString())
        assertEquals("生年月日は過去の日付である必要があります", violation.message)
    }

    @Test
    fun `生年月日が今日の日付の場合バリデーションエラー`() {
        // Given
        val today = LocalDate.now()
        val request =
            AuthorCreateRequest(
                name = "今日生まれ作家",
                birthDate = today,
            )

        // When
        val violations: Set<ConstraintViolation<AuthorCreateRequest>> = validator.validate(request)

        // Then
        assertEquals(1, violations.size)
        val violation = violations.iterator().next()
        assertEquals("birthDate", violation.propertyPath.toString())
        assertEquals("生年月日は過去の日付である必要があります", violation.message)
    }

    @Test
    fun `生年月日が昨日の日付の場合バリデーション成功`() {
        // Given
        val yesterday = LocalDate.now().minusDays(1)
        val request =
            AuthorCreateRequest(
                name = "昨日生まれ作家",
                birthDate = yesterday,
            )

        // When
        val violations: Set<ConstraintViolation<AuthorCreateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "昨日の日付はバリデーション成功")
    }

    @Test
    fun `極端に古い生年月日でもバリデーション成功`() {
        // Given
        val ancientDate = LocalDate.of(1800, 1, 1)
        val request =
            AuthorCreateRequest(
                name = "古典作家",
                birthDate = ancientDate,
            )

        // When
        val violations: Set<ConstraintViolation<AuthorCreateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "古い日付でもバリデーション成功")
    }

    @Test
    fun `複数のバリデーションエラーが同時に発生する場合`() {
        // Given
        val futureDate = LocalDate.now().plusYears(1)
        val request =
            AuthorCreateRequest(
                name = "", // 空文字
                birthDate = futureDate, // 未来の日付
            )

        // When
        val violations: Set<ConstraintViolation<AuthorCreateRequest>> = validator.validate(request)

        // Then
        assertEquals(2, violations.size)

        val violationMessages = violations.map { it.message }.toSet()
        assertTrue(violationMessages.contains("著者名は必須です"))
        assertTrue(violationMessages.contains("生年月日は過去の日付である必要があります"))
    }

    @Test
    fun `長い著者名でもバリデーション成功`() {
        // Given
        val longName = "あ".repeat(255) // 長い名前
        val request =
            AuthorCreateRequest(
                name = longName,
                birthDate = LocalDate.of(1990, 1, 1),
            )

        // When
        val violations: Set<ConstraintViolation<AuthorCreateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "長い名前でもバリデーション成功（データベース制約は別途）")
    }
}
