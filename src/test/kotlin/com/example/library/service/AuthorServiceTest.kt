package com.example.library.service

import com.example.library.controller.dto.AuthorCreateRequest
import com.example.library.controller.dto.AuthorUpdateRequest
import com.example.library.domain.Author
import com.example.library.exception.DuplicateResourceException
import com.example.library.exception.ResourceNotFoundException
import com.example.library.repository.jooq.AuthorRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * AuthorServiceの単体テスト
 */
@ExtendWith(MockitoExtension::class)
class AuthorServiceTest {
    @Mock
    private lateinit var authorRepository: AuthorRepository

    @InjectMocks
    private lateinit var authorService: AuthorService

    private val testAuthor =
        Author(
            id = 1L,
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

    @BeforeEach
    fun setup() {
    }

    @Test
    fun `findAll - 正常系`() {
        // Given
        val authors = listOf(testAuthor)
        whenever(authorRepository.findAll(0, 20)).thenReturn(authors)
        whenever(authorRepository.countAll()).thenReturn(1L)

        // When
        val result = authorService.findAll(0, 20)

        // Then
        assertEquals(1, result.content.size)
        assertEquals("夏目漱石", result.content[0].name)
        assertEquals(0, result.pageNumber)
        assertEquals(20, result.pageSize)
        assertEquals(1L, result.totalElements)
    }

    @Test
    fun `findAll - 無効なページネーションパラメータ`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            authorService.findAll(-1, 20)
        }

        assertThrows<IllegalArgumentException> {
            authorService.findAll(0, 0)
        }
    }

    @Test
    fun `searchByName - 正常系`() {
        // Given
        val authors = listOf(testAuthor)
        whenever(authorRepository.findByNameContaining("夏目", 0, 20)).thenReturn(authors)
        whenever(authorRepository.countByNameContaining("夏目")).thenReturn(1L)

        // When
        val result = authorService.searchByName("夏目", 0, 20)

        // Then
        assertEquals(1, result.content.size)
        assertEquals("夏目漱石", result.content[0].name)
    }

    @Test
    fun `searchByName - 空の検索名`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            authorService.searchByName("", 0, 20)
        }

        assertThrows<IllegalArgumentException> {
            authorService.searchByName("   ", 0, 20)
        }
    }

    @Test
    fun `findById - 正常系`() {
        // Given
        whenever(authorRepository.findById(1L)).thenReturn(testAuthor)

        // When
        val result = authorService.findById(1L)

        // Then
        assertEquals(1L, result.id)
        assertEquals("夏目漱石", result.name)
        assertEquals(LocalDate.of(1867, 2, 9), result.birthDate)
    }

    @Test
    fun `findById - 著者が存在しない`() {
        // Given
        whenever(authorRepository.findById(999L)).thenReturn(null)

        // When & Then
        val exception =
            assertThrows<ResourceNotFoundException> {
                authorService.findById(999L)
            }
        assertEquals("指定されたID=999 の著者が見つかりません", exception.message)
    }

    @Test
    fun `create - 正常系`() {
        // Given
        val request =
            AuthorCreateRequest(
                name = "芥川龍之介",
                birthDate = LocalDate.of(1892, 3, 1),
            )

        whenever(authorRepository.existsByName("芥川龍之介")).thenReturn(false)
        whenever(
            authorRepository.save(any()),
        ).thenReturn(testAuthor.copy(name = "芥川龍之介", birthDate = LocalDate.of(1892, 3, 1)))

        // When
        val result = authorService.create(request)

        // Then
        assertEquals("芥川龍之介", result.name)
        assertEquals(LocalDate.of(1892, 3, 1), result.birthDate)
        verify(authorRepository).save(any())
    }

    @Test
    fun `create - 重複著者名`() {
        // Given
        val request =
            AuthorCreateRequest(
                name = "夏目漱石",
                birthDate = LocalDate.of(1867, 2, 9),
            )

        whenever(authorRepository.existsByName("夏目漱石")).thenReturn(true)

        // When & Then
        val exception =
            assertThrows<DuplicateResourceException> {
                authorService.create(request)
            }
        assertEquals("著者名「夏目漱石」は既に登録されています", exception.message)
        verify(authorRepository, never()).save(any())
    }

    @Test
    fun `update - 正常系`() {
        // Given
        val request =
            AuthorUpdateRequest(
                name = "夏目漱石（改）",
                birthDate = LocalDate.of(1867, 2, 9),
            )

        whenever(authorRepository.findById(1L)).thenReturn(testAuthor)
        whenever(authorRepository.existsByNameAndIdNot("夏目漱石（改）", 1L)).thenReturn(false)
        whenever(authorRepository.update(any())).thenReturn(testAuthor.copy(name = "夏目漱石（改）"))

        // When
        val result = authorService.update(1L, request)

        // Then
        assertEquals("夏目漱石（改）", result.name)
        assertEquals(LocalDate.of(1867, 2, 9), result.birthDate)
        verify(authorRepository).update(any())
    }

    @Test
    fun `update - 著者が存在しない`() {
        // Given
        val request =
            AuthorUpdateRequest(
                name = "夏目漱石（改）",
                birthDate = LocalDate.of(1867, 2, 9),
            )

        whenever(authorRepository.findById(999L)).thenReturn(null)

        // When & Then
        val exception =
            assertThrows<ResourceNotFoundException> {
                authorService.update(999L, request)
            }
        assertEquals("指定されたID=999 の著者が見つかりません", exception.message)
    }

    @Test
    fun `update - 重複著者名`() {
        // Given
        val request =
            AuthorUpdateRequest(
                name = "芥川龍之介",
                birthDate = LocalDate.of(1867, 2, 9),
            )

        whenever(authorRepository.findById(1L)).thenReturn(testAuthor)
        whenever(authorRepository.existsByNameAndIdNot("芥川龍之介", 1L)).thenReturn(true)

        // When & Then
        val exception =
            assertThrows<DuplicateResourceException> {
                authorService.update(1L, request)
            }
        assertEquals("著者名「芥川龍之介」は既に他の著者により登録されています", exception.message)
        verify(authorRepository, never()).update(any())
    }
}
