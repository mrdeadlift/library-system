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
 * BookUpdateRequest DTOのバリデーションテストクラス
 */
class BookUpdateRequestTest {
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
            BookUpdateRequest(
                title = "更新後書籍",
                price = BigDecimal("2500.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(1L, 2L),
            )

        // When
        val violations: Set<ConstraintViolation<BookUpdateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "バリデーションエラーがないことを期待")
    }

    @Test
    fun `書籍タイトルが空文字の場合バリデーションエラー`() {
        // Given
        val request =
            BookUpdateRequest(
                title = "",
                price = BigDecimal("1500.00"),
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(1L),
            )

        // When
        val violations: Set<ConstraintViolation<BookUpdateRequest>> = validator.validate(request)

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
            BookUpdateRequest(
                title = "   ",
                price = BigDecimal("1500.00"),
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(1L),
            )

        // When
        val violations: Set<ConstraintViolation<BookUpdateRequest>> = validator.validate(request)

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
            BookUpdateRequest(
                title = "負価格書籍",
                price = BigDecimal("-100.00"),
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(1L),
            )

        // When
        val violations: Set<ConstraintViolation<BookUpdateRequest>> = validator.validate(request)

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
            BookUpdateRequest(
                title = "無料書籍",
                price = BigDecimal.ZERO,
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(1L),
            )

        // When
        val violations: Set<ConstraintViolation<BookUpdateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "価格0でバリデーション成功")
    }

    @Test
    fun `著者IDリストが空の場合バリデーションエラー`() {
        // Given
        val request =
            BookUpdateRequest(
                title = "著者なし書籍",
                price = BigDecimal("1500.00"),
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = emptyList(),
            )

        // When
        val violations: Set<ConstraintViolation<BookUpdateRequest>> = validator.validate(request)

        // Then
        assertEquals(1, violations.size)
        val violation = violations.iterator().next()
        assertEquals("authorIds", violation.propertyPath.toString())
        assertEquals("最低1人の著者が必要です", violation.message)
    }

    @Test
    fun `未出版から出版済みへの更新`() {
        // Given
        val request =
            BookUpdateRequest(
                title = "出版予定書籍",
                price = BigDecimal("3000.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(1L),
            )

        // When
        val violations: Set<ConstraintViolation<BookUpdateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "未出版から出版済みへの更新でバリデーション成功")
    }

    @Test
    fun `出版済み状態での更新`() {
        // Given
        val request =
            BookUpdateRequest(
                title = "既出版書籍（更新）",
                price = BigDecimal("3500.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(1L, 2L),
            )

        // When
        val violations: Set<ConstraintViolation<BookUpdateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "出版済み状態での更新でバリデーション成功")
    }

    @Test
    fun `複数のバリデーションエラーが同時に発生する場合`() {
        // Given
        val request =
            BookUpdateRequest(
                title = "\t\n", // タブと改行のみ（空白として扱われる）
                price = BigDecimal("-1000.00"), // 負価格
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = emptyList(), // 空著者リスト
            )

        // When
        val violations: Set<ConstraintViolation<BookUpdateRequest>> = validator.validate(request)

        // Then
        assertEquals(3, violations.size)

        val violationMessages = violations.map { it.message }.toSet()
        assertTrue(violationMessages.contains("書籍タイトルは必須です"))
        assertTrue(violationMessages.contains("価格は0以上である必要があります"))
        assertTrue(violationMessages.contains("最低1人の著者が必要です"))
    }

    @Test
    fun `単一著者への変更`() {
        // Given
        val request =
            BookUpdateRequest(
                title = "単著に変更",
                price = BigDecimal("1800.00"),
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(5L),
            )

        // When
        val violations: Set<ConstraintViolation<BookUpdateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "単一著者への変更でバリデーション成功")
    }

    @Test
    fun `多数の著者への変更`() {
        // Given
        val request =
            BookUpdateRequest(
                title = "多数著者書籍",
                price = BigDecimal("4000.00"),
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = (1L..10L).toList(), // 10人の著者
            )

        // When
        val violations: Set<ConstraintViolation<BookUpdateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "多数著者でもバリデーション成功")
    }

    @Test
    fun `極端に高い価格でもバリデーション成功`() {
        // Given
        val request =
            BookUpdateRequest(
                title = "超高額書籍",
                price = BigDecimal("1000000.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(1L),
            )

        // When
        val violations: Set<ConstraintViolation<BookUpdateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "極端に高い価格でもバリデーション成功")
    }

    @Test
    fun `小数点以下の価格でもバリデーション成功`() {
        // Given
        val request =
            BookUpdateRequest(
                title = "小数点価格書籍",
                price = BigDecimal("99.99"),
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(1L),
            )

        // When
        val violations: Set<ConstraintViolation<BookUpdateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "小数点価格でもバリデーション成功")
    }

    @Test
    fun `タイトルに特殊文字を含む場合でもバリデーション成功`() {
        // Given
        val request =
            BookUpdateRequest(
                title = "特殊文字！@#$%^&*()書籍",
                price = BigDecimal("1200.00"),
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(1L),
            )

        // When
        val violations: Set<ConstraintViolation<BookUpdateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "特殊文字を含むタイトルでもバリデーション成功")
    }

    @Test
    fun `著者IDに0やマイナス値が含まれてもDTO レベルではバリデーション成功`() {
        // Given - ビジネスロジックレベルでの存在チェックは別途
        val request =
            BookUpdateRequest(
                title = "無効ID含む書籍",
                price = BigDecimal("1500.00"),
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(0L, -1L, 1L), // 無効なIDを含む
            )

        // When
        val violations: Set<ConstraintViolation<BookUpdateRequest>> = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty(), "DTOレベルでは無効IDでもバリデーション成功（ビジネスロジックで検証）")
    }
}
