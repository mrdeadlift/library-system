package com.example.library.controller.dto

import com.example.library.domain.Author
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * AuthorResponse DTOのテストクラス
 */
class AuthorResponseTest {
    @Test
    fun `正常なAuthorドメインオブジェクトからレスポンスDTOに変換される`() {
        // Given
        val createdAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0)
        val updatedAt = LocalDateTime.of(2024, 1, 2, 15, 30, 0)

        val author =
            Author(
                id = 1L,
                name = "太郎作家",
                birthDate = LocalDate.of(1980, 5, 15),
                createdAt = createdAt,
                updatedAt = updatedAt,
            )

        // When
        val response = AuthorResponse.from(author)

        // Then
        assertEquals(1L, response.id)
        assertEquals("太郎作家", response.name)
        assertEquals(LocalDate.of(1980, 5, 15), response.birthDate)
        assertEquals(createdAt, response.createdAt)
        assertEquals(updatedAt, response.updatedAt)
    }

    @Test
    fun `IDがnullのAuthorからレスポンス変換時に例外がスローされる`() {
        // Given
        val author =
            Author(
                id = null, // IDがnull
                name = "著者名",
                birthDate = LocalDate.of(1990, 1, 1),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        // When & Then
        val exception =
            assertThrows(IllegalStateException::class.java) {
                AuthorResponse.from(author)
            }
        assertEquals("著者IDが設定されていません", exception.message)
    }

    @Test
    fun `最小文字数の著者名でもレスポンスDTOに変換される`() {
        // Given
        val author =
            Author(
                id = 2L,
                name = "A", // 最小文字数の名前
                birthDate = LocalDate.of(1995, 12, 31),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        // When
        val response = AuthorResponse.from(author)

        // Then
        assertEquals(2L, response.id)
        assertEquals("A", response.name)
        assertEquals(LocalDate.of(1995, 12, 31), response.birthDate)
    }

    @Test
    fun `極端に古い生年月日でもレスポンスDTOに変換される`() {
        // Given
        val author =
            Author(
                id = 3L,
                name = "古典作家",
                birthDate = LocalDate.of(1800, 1, 1), // 極端に古い日付
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        // When
        val response = AuthorResponse.from(author)

        // Then
        assertEquals(3L, response.id)
        assertEquals("古典作家", response.name)
        assertEquals(LocalDate.of(1800, 1, 1), response.birthDate)
    }
}
