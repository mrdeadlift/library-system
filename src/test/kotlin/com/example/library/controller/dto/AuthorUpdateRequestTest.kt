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
 * AuthorUpdateRequest DTOのバリデーションテストクラス
 */
class AuthorUpdateRequestTest {
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
            AuthorUpdateRequest(
                name = "更新後作家",
                birthDate = LocalDate.of(1985, 3, 20),
            )

        // When
        val violations: Set<ConstraintViolation<AuthorUpdateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "バリデーションエラーがないことを期待")
    }

    @Test
    fun `著者名が空文字の場合バリデーションエラー`() {
        // Given
        val request =
            AuthorUpdateRequest(
                name = "",
                birthDate = LocalDate.of(1985, 3, 20),
            )

        // When
        val violations: Set<ConstraintViolation<AuthorUpdateRequest>> = validator.validate(request)

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
            AuthorUpdateRequest(
                name = "   ",
                birthDate = LocalDate.of(1985, 3, 20),
            )

        // When
        val violations: Set<ConstraintViolation<AuthorUpdateRequest>> = validator.validate(request)

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
            AuthorUpdateRequest(
                name = "未来人作家",
                birthDate = futureDate,
            )

        // When
        val violations: Set<ConstraintViolation<AuthorUpdateRequest>> = validator.validate(request)

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
            AuthorUpdateRequest(
                name = "今日生まれ作家",
                birthDate = today,
            )

        // When
        val violations: Set<ConstraintViolation<AuthorUpdateRequest>> = validator.validate(request)

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
            AuthorUpdateRequest(
                name = "昨日生まれ作家",
                birthDate = yesterday,
            )

        // When
        val violations: Set<ConstraintViolation<AuthorUpdateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "昨日の日付はバリデーション成功")
    }

    @Test
    fun `複数のバリデーションエラーが同時に発生する場合`() {
        // Given
        val futureDate = LocalDate.now().plusYears(1)
        val request =
            AuthorUpdateRequest(
                name = "\t\n", // タブと改行文字のみ（空白として扱われる）
                birthDate = futureDate,
            )

        // When
        val violations: Set<ConstraintViolation<AuthorUpdateRequest>> = validator.validate(request)

        // Then
        assertEquals(2, violations.size)

        val violationMessages = violations.map { it.message }.toSet()
        assertTrue(violationMessages.contains("著者名は必須です"))
        assertTrue(violationMessages.contains("生年月日は過去の日付である必要があります"))
    }

    @Test
    fun `名前が数字や記号でもバリデーション成功`() {
        // Given
        val request =
            AuthorUpdateRequest(
                name = "著者123!@#",
                birthDate = LocalDate.of(1990, 1, 1),
            )

        // When
        val violations: Set<ConstraintViolation<AuthorUpdateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "数字や記号を含む名前でもバリデーション成功")
    }

    @Test
    fun `極端に長い著者名でもバリデーション成功`() {
        // Given
        val veryLongName = "長".repeat(1000)
        val request =
            AuthorUpdateRequest(
                name = veryLongName,
                birthDate = LocalDate.of(1990, 1, 1),
            )

        // When
        val violations: Set<ConstraintViolation<AuthorUpdateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "非常に長い名前でもバリデーション成功（データベース制約は別途）")
    }

    @Test
    fun `単一文字の著者名でもバリデーション成功`() {
        // Given
        val request =
            AuthorUpdateRequest(
                name = "A",
                birthDate = LocalDate.of(2000, 12, 31),
            )

        // When
        val violations: Set<ConstraintViolation<AuthorUpdateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "単一文字の名前でもバリデーション成功")
    }

    @Test
    fun `改行を含む著者名でもバリデーション成功`() {
        // Given
        val request =
            AuthorUpdateRequest(
                name = "改行\n含む名前",
                birthDate = LocalDate.of(1995, 6, 15),
            )

        // When
        val violations: Set<ConstraintViolation<AuthorUpdateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "改行を含む名前でもバリデーション成功")
    }
}
