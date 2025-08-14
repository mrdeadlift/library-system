package com.example.library.controller.dto

import com.example.library.domain.PublicationStatus
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * BookCreateRequest DTOのバリデーションテストクラス
 */
class BookCreateRequestTest {
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
            BookCreateRequest(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(1L, 2L),
            )

        // When
        val violations: Set<ConstraintViolation<BookCreateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "バリデーションエラーがないことを期待")
    }

    @Test
    fun `デフォルト値を使用した正常なリクエスト`() {
        // Given - publicationStatusをデフォルト値で省略
        val request =
            BookCreateRequest(
                title = "デフォルト書籍",
                price = BigDecimal("2000.00"),
                authorIds = listOf(1L),
            )

        // When
        val violations: Set<ConstraintViolation<BookCreateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "デフォルト値でバリデーションエラーなし")
        assertEquals(PublicationStatus.UNPUBLISHED, request.publicationStatus)
    }

    @Test
    fun `書籍タイトルが空文字の場合バリデーションエラー`() {
        // Given
        val request =
            BookCreateRequest(
                title = "",
                price = BigDecimal("1500.00"),
                authorIds = listOf(1L),
            )

        // When
        val violations: Set<ConstraintViolation<BookCreateRequest>> = validator.validate(request)

        // Then
        assertEquals(1, violations.size)
        val violation = violations.iterator().next()
        assertEquals("title", violation.propertyPath.toString())
        assertEquals("書籍タイトルは必須です", violation.message)
    }

    @Test
    fun `書籍タイトルが空白のみの場合バリデーションエラー`() {
        // Given
        val request =
            BookCreateRequest(
                title = "   ",
                price = BigDecimal("1500.00"),
                authorIds = listOf(1L),
            )

        // When
        val violations: Set<ConstraintViolation<BookCreateRequest>> = validator.validate(request)

        // Then
        assertEquals(1, violations.size)
        val violation = violations.iterator().next()
        assertEquals("title", violation.propertyPath.toString())
        assertEquals("書籍タイトルは必須です", violation.message)
    }

    @Test
    fun `価格が負数の場合バリデーションエラー`() {
        // Given
        val request =
            BookCreateRequest(
                title = "負価格書籍",
                price = BigDecimal("-100.00"),
                authorIds = listOf(1L),
            )

        // When
        val violations: Set<ConstraintViolation<BookCreateRequest>> = validator.validate(request)

        // Then
        assertEquals(1, violations.size)
        val violation = violations.iterator().next()
        assertEquals("price", violation.propertyPath.toString())
        assertEquals("価格は0以上である必要があります", violation.message)
    }

    @Test
    fun `価格が0の場合バリデーション成功`() {
        // Given
        val request =
            BookCreateRequest(
                title = "無料書籍",
                price = BigDecimal.ZERO,
                authorIds = listOf(1L),
            )

        // When
        val violations: Set<ConstraintViolation<BookCreateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "価格0でバリデーション成功")
    }

    @Test
    fun `著者IDリストが空の場合バリデーションエラー`() {
        // Given
        val request =
            BookCreateRequest(
                title = "著者なし書籍",
                price = BigDecimal("1500.00"),
                authorIds = emptyList(),
            )

        // When
        val violations: Set<ConstraintViolation<BookCreateRequest>> = validator.validate(request)

        // Then
        assertEquals(1, violations.size)
        val violation = violations.iterator().next()
        assertEquals("authorIds", violation.propertyPath.toString())
        assertEquals("最低1人の著者が必要です", violation.message)
    }

    @Test
    fun `単一著者IDでバリデーション成功`() {
        // Given
        val request =
            BookCreateRequest(
                title = "単著書籍",
                price = BigDecimal("1000.00"),
                authorIds = listOf(1L),
            )

        // When
        val violations: Set<ConstraintViolation<BookCreateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "単一著者でバリデーション成功")
    }

    @Test
    fun `複数著者IDでバリデーション成功`() {
        // Given
        val request =
            BookCreateRequest(
                title = "共著書籍",
                price = BigDecimal("2500.00"),
                authorIds = listOf(1L, 2L, 3L),
            )

        // When
        val violations: Set<ConstraintViolation<BookCreateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "複数著者でバリデーション成功")
    }

    @Test
    fun `出版済み状態でのリクエスト`() {
        // Given
        val request =
            BookCreateRequest(
                title = "既出版書籍",
                price = BigDecimal("3000.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(1L),
            )

        // When
        val violations: Set<ConstraintViolation<BookCreateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "出版済み状態でバリデーション成功")
    }

    @Test
    fun `複数のバリデーションエラーが同時に発生する場合`() {
        // Given
        val request =
            BookCreateRequest(
                title = "", // 空タイトル
                price = BigDecimal("-500.00"), // 負価格
                authorIds = emptyList(), // 空著者リスト
            )

        // When
        val violations: Set<ConstraintViolation<BookCreateRequest>> = validator.validate(request)

        // Then
        assertEquals(3, violations.size)

        val violationMessages = violations.map { it.message }.toSet()
        assertTrue(violationMessages.contains("書籍タイトルは必須です"))
        assertTrue(violationMessages.contains("価格は0以上である必要があります"))
        assertTrue(violationMessages.contains("最低1人の著者が必要です"))
    }

    @Test
    fun `高額な価格でもバリデーション成功`() {
        // Given
        val request =
            BookCreateRequest(
                title = "高額書籍",
                price = BigDecimal("999999.99"),
                authorIds = listOf(1L),
            )

        // When
        val violations: Set<ConstraintViolation<BookCreateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "高額でもバリデーション成功")
    }

    @Test
    fun `非常に長いタイトルでもバリデーション成功`() {
        // Given
        val longTitle = "長".repeat(500)
        val request =
            BookCreateRequest(
                title = longTitle,
                price = BigDecimal("1000.00"),
                authorIds = listOf(1L),
            )

        // When
        val violations: Set<ConstraintViolation<BookCreateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "長いタイトルでもバリデーション成功")
    }

    @Test
    fun `重複する著者IDでもバリデーション成功`() {
        // Given - ビジネスロジックレベルでの重複チェックは別途
        val request =
            BookCreateRequest(
                title = "重複著者書籍",
                price = BigDecimal("1500.00"),
                authorIds = listOf(1L, 1L, 2L), // 重複あり
            )

        // When
        val violations: Set<ConstraintViolation<BookCreateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "DTOレベルでは重複著者IDでもバリデーション成功")
    }
}
