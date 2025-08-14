package com.example.library.controller.dto

import com.example.library.domain.Author
import com.example.library.domain.Book
import com.example.library.domain.PublicationStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * BookResponse DTOのテストクラス
 */
class BookResponseTest {
    @Test
    fun `正常なBookドメインオブジェクトからレスポンスDTOに変換される`() {
        // Given
        val author =
            Author(
                id = 1L,
                name = "著者名",
                birthDate = LocalDate.of(1980, 1, 1),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        val createdAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0)
        val updatedAt = LocalDateTime.of(2024, 1, 2, 15, 30, 0)

        val book =
            Book(
                id = 1L,
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authors = listOf(author),
                createdAt = createdAt,
                updatedAt = updatedAt,
            )

        // When
        val response = BookResponse.from(book)

        // Then
        assertEquals(1L, response.id)
        assertEquals("テスト書籍", response.title)
        assertEquals(BigDecimal("1500.00"), response.price)
        assertEquals("¥1500.00", response.formattedPrice)
        assertEquals(PublicationStatus.PUBLISHED, response.publicationStatus)
        assertTrue(response.isPublished)
        assertEquals(1, response.authors.size)
        assertEquals("著者名", response.authors[0].name)
        assertEquals("著者名", response.authorNames)
        assertEquals(createdAt, response.createdAt)
        assertEquals(updatedAt, response.updatedAt)
    }

    @Test
    fun `未出版の書籍でもレスポンスDTOに変換される`() {
        // Given
        val author =
            Author(
                id = 1L,
                name = "著者名",
                birthDate = LocalDate.of(1980, 1, 1),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        val book =
            Book(
                id = 2L,
                title = "未出版書籍",
                price = BigDecimal("2000.00"),
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authors = listOf(author),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        // When
        val response = BookResponse.from(book)

        // Then
        assertEquals(2L, response.id)
        assertEquals("未出版書籍", response.title)
        assertEquals(PublicationStatus.UNPUBLISHED, response.publicationStatus)
        assertFalse(response.isPublished)
    }

    @Test
    fun `複数著者の書籍からレスポンスDTOに変換される`() {
        // Given
        val author1 =
            Author(
                id = 1L,
                name = "第一著者",
                birthDate = LocalDate.of(1980, 1, 1),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        val author2 =
            Author(
                id = 2L,
                name = "第二著者",
                birthDate = LocalDate.of(1985, 1, 1),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        val book =
            Book(
                id = 3L,
                title = "共著書籍",
                price = BigDecimal("3000.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authors = listOf(author1, author2),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        // When
        val response = BookResponse.from(book)

        // Then
        assertEquals(3L, response.id)
        assertEquals("共著書籍", response.title)
        assertEquals(2, response.authors.size)
        assertEquals("第一著者", response.authors[0].name)
        assertEquals("第二著者", response.authors[1].name)
        assertEquals("第一著者, 第二著者", response.authorNames)
    }

    @Test
    fun `IDがnullの書籍からレスポンス変換時に例外がスローされる`() {
        // Given
        val author =
            Author(
                id = 1L,
                name = "著者名",
                birthDate = LocalDate.of(1980, 1, 1),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        val book =
            Book(
                id = null, // IDがnull
                title = "書籍名",
                price = BigDecimal("1000.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authors = listOf(author),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        // When & Then
        val exception =
            assertThrows(IllegalStateException::class.java) {
                BookResponse.from(book)
            }
        assertEquals("書籍IDが設定されていません", exception.message)
    }

    @Test
    fun `価格が0の書籍でもレスポンスDTOに変換される`() {
        // Given
        val author =
            Author(
                id = 1L,
                name = "著者名",
                birthDate = LocalDate.of(1980, 1, 1),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        val book =
            Book(
                id = 4L,
                title = "無料書籍",
                price = BigDecimal.ZERO,
                publicationStatus = PublicationStatus.PUBLISHED,
                authors = listOf(author),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        // When
        val response = BookResponse.from(book)

        // Then
        assertEquals(4L, response.id)
        assertEquals("無料書籍", response.title)
        assertEquals(BigDecimal.ZERO, response.price)
        assertEquals("¥0", response.formattedPrice)
    }

    @Test
    fun `高価格の書籍でもレスポンスDTOに変換される`() {
        // Given
        val author =
            Author(
                id = 1L,
                name = "著者名",
                birthDate = LocalDate.of(1980, 1, 1),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        val book =
            Book(
                id = 5L,
                title = "高額書籍",
                price = BigDecimal("999999.99"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authors = listOf(author),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        // When
        val response = BookResponse.from(book)

        // Then
        assertEquals(5L, response.id)
        assertEquals("高額書籍", response.title)
        assertEquals(BigDecimal("999999.99"), response.price)
        assertEquals("¥999999.99", response.formattedPrice)
    }
}
