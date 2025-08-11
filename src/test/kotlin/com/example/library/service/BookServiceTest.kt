package com.example.library.service

import com.example.library.controller.dto.BookCreateRequest
import com.example.library.controller.dto.BookUpdateRequest
import com.example.library.domain.Author
import com.example.library.domain.Book
import com.example.library.domain.PublicationStatus
import com.example.library.exception.DuplicateResourceException
import com.example.library.exception.ResourceNotFoundException
import com.example.library.repository.jooq.AuthorRepository
import com.example.library.repository.jooq.BookRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * BookServiceの単体テスト
 */
@ExtendWith(MockitoExtension::class)
class BookServiceTest {
    @Mock
    private lateinit var bookRepository: BookRepository

    @Mock
    private lateinit var authorRepository: AuthorRepository

    @InjectMocks
    private lateinit var bookService: BookService

    private lateinit var testAuthor: Author
    private lateinit var testBook: Book

    @BeforeEach
    fun setup() {
        testAuthor =
            Author(
                id = 1L,
                name = "村上春樹",
                birthDate = LocalDate.of(1949, 1, 12),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        testBook =
            Book(
                id = 1L,
                title = "ノルウェイの森",
                price = BigDecimal("1800.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authors = listOf(testAuthor),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
    }

    @Test
    fun `findAll - 正常系`() {
        // Given
        val books = listOf(testBook)
        whenever(bookRepository.findAll(0, 20)).thenReturn(books)
        whenever(bookRepository.countAll()).thenReturn(1L)

        // When
        val result = bookService.findAll(0, 20)

        // Then
        assertEquals(1, result.content.size)
        assertEquals("ノルウェイの森", result.content[0].title)
        assertEquals(0, result.pageNumber)
        assertEquals(20, result.pageSize)
        assertEquals(1L, result.totalElements)
    }

    @Test
    fun `findAll - 無効なページネーションパラメータ`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            bookService.findAll(-1, 20)
        }

        assertThrows<IllegalArgumentException> {
            bookService.findAll(0, 0)
        }
    }

    @Test
    fun `searchByTitle - 正常系`() {
        // Given
        val books = listOf(testBook)
        whenever(bookRepository.findByTitleContaining("ノルウェイ", 0, 20)).thenReturn(books)
        whenever(bookRepository.countByTitleContaining("ノルウェイ")).thenReturn(1L)

        // When
        val result = bookService.searchByTitle("ノルウェイ", 0, 20)

        // Then
        assertEquals(1, result.content.size)
        assertEquals("ノルウェイの森", result.content[0].title)
    }

    @Test
    fun `searchByTitle - 空のタイトル`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            bookService.searchByTitle("", 0, 20)
        }

        assertThrows<IllegalArgumentException> {
            bookService.searchByTitle("   ", 0, 20)
        }
    }

    @Test
    fun `findById - 正常系`() {
        // Given
        whenever(bookRepository.findById(1L)).thenReturn(testBook)

        // When
        val result = bookService.findById(1L)

        // Then
        assertEquals(1L, result.id)
        assertEquals("ノルウェイの森", result.title)
        assertEquals("村上春樹", result.authorNames)
    }

    @Test
    fun `findById - 書籍が存在しない`() {
        // Given
        whenever(bookRepository.findById(999L)).thenReturn(null)

        // When & Then
        val exception =
            assertThrows<ResourceNotFoundException> {
                bookService.findById(999L)
            }
        assertEquals("指定されたID=999 の書籍が見つかりません", exception.message)
    }

    @Test
    fun `create - 正常系`() {
        // Given
        val request =
            BookCreateRequest(
                title = "海辺のカフカ",
                price = BigDecimal("2200.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(1L),
            )

        whenever(bookRepository.existsByTitle("海辺のカフカ")).thenReturn(false)
        whenever(authorRepository.findById(1L)).thenReturn(testAuthor)
        whenever(bookRepository.save(any())).thenReturn(testBook.copy(title = "海辺のカフカ", price = BigDecimal("2200.00")))

        // When
        val result = bookService.create(request)

        // Then
        assertEquals("海辺のカフカ", result.title)
        assertEquals(BigDecimal("2200.00"), result.price)
        verify(bookRepository).save(any())
    }

    @Test
    fun `create - 重複タイトル`() {
        // Given
        val request =
            BookCreateRequest(
                title = "ノルウェイの森",
                price = BigDecimal("1800.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(1L),
            )

        whenever(bookRepository.existsByTitle("ノルウェイの森")).thenReturn(true)

        // When & Then
        val exception =
            assertThrows<DuplicateResourceException> {
                bookService.create(request)
            }
        assertEquals("書籍タイトル「ノルウェイの森」は既に登録されています", exception.message)
        verify(bookRepository, never()).save(any())
    }

    @Test
    fun `create - 存在しない著者ID`() {
        // Given
        val request =
            BookCreateRequest(
                title = "海辺のカフカ",
                price = BigDecimal("2200.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(999L),
            )

        whenever(bookRepository.existsByTitle("海辺のカフカ")).thenReturn(false)
        whenever(authorRepository.findById(999L)).thenReturn(null)

        // When & Then
        val exception =
            assertThrows<ResourceNotFoundException> {
                bookService.create(request)
            }
        assertEquals("指定されたID=999 の著者が見つかりません", exception.message)
        verify(bookRepository, never()).save(any())
    }

    @Test
    fun `update - 正常系`() {
        // Given
        val request =
            BookUpdateRequest(
                title = "ノルウェイの森（改訂版）",
                price = BigDecimal("1900.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(1L),
            )

        whenever(bookRepository.findById(1L)).thenReturn(testBook)
        whenever(bookRepository.existsByTitleAndIdNot("ノルウェイの森（改訂版）", 1L)).thenReturn(false)
        whenever(authorRepository.findById(1L)).thenReturn(testAuthor)
        whenever(
            bookRepository.update(any()),
        ).thenReturn(testBook.copy(title = "ノルウェイの森（改訂版）", price = BigDecimal("1900.00")))

        // When
        val result = bookService.update(1L, request)

        // Then
        assertEquals("ノルウェイの森（改訂版）", result.title)
        assertEquals(BigDecimal("1900.00"), result.price)
        verify(bookRepository).update(any())
    }

    @Test
    fun `update - 書籍が存在しない`() {
        // Given
        val request =
            BookUpdateRequest(
                title = "ノルウェイの森（改訂版）",
                price = BigDecimal("1900.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(1L),
            )

        whenever(bookRepository.findById(999L)).thenReturn(null)

        // When & Then
        val exception =
            assertThrows<ResourceNotFoundException> {
                bookService.update(999L, request)
            }
        assertEquals("指定されたID=999 の書籍が見つかりません", exception.message)
    }

    @Test
    fun `updatePublicationStatus - 正常系（未出版から出版済み）`() {
        // Given
        val unpublishedBook = testBook.copy(publicationStatus = PublicationStatus.UNPUBLISHED)
        whenever(bookRepository.findById(1L)).thenReturn(unpublishedBook)
        whenever(
            bookRepository.update(any()),
        ).thenReturn(unpublishedBook.copy(publicationStatus = PublicationStatus.PUBLISHED))

        // When
        val result = bookService.updatePublicationStatus(1L, PublicationStatus.PUBLISHED)

        // Then
        assertEquals(PublicationStatus.PUBLISHED, result.publicationStatus)
        assertTrue(result.isPublished)
        verify(bookRepository).update(any())
    }

    @Test
    fun `updatePublicationStatus - ビジネスルール違反（出版済みから未出版への変更）`() {
        // Given
        whenever(bookRepository.findById(1L)).thenReturn(testBook) // 出版済み

        // When & Then
        val exception =
            assertThrows<IllegalArgumentException> {
                bookService.updatePublicationStatus(1L, PublicationStatus.UNPUBLISHED)
            }
        assertTrue(exception.message?.contains("出版状況の更新に失敗しました") ?: false)
        verify(bookRepository, never()).update(any())
    }

    @Test
    fun `addAuthor - 正常系`() {
        // Given
        val secondAuthor =
            Author(
                id = 2L,
                name = "東野圭吾",
                birthDate = LocalDate.of(1958, 2, 4),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        whenever(bookRepository.findById(1L)).thenReturn(testBook)
        whenever(authorRepository.findById(2L)).thenReturn(secondAuthor)
        whenever(bookRepository.update(any())).thenReturn(testBook.copy(authors = listOf(testAuthor, secondAuthor)))

        // When
        val result = bookService.addAuthor(1L, 2L)

        // Then
        assertEquals(2, result.authors.size)
        assertTrue(result.authorNames.contains("東野圭吾"))
        verify(bookRepository).update(any())
    }

    @Test
    fun `addAuthor - 既に追加済みの著者`() {
        // Given - testBook already has testAuthor (id=1) in its authors list
        whenever(bookRepository.findById(1L)).thenReturn(testBook)
        whenever(authorRepository.findById(1L)).thenReturn(testAuthor)

        // When & Then - trying to add the same author again should fail
        val exception =
            assertThrows<IllegalArgumentException> {
                bookService.addAuthor(1L, 1L)
            }
        assertTrue(exception.message?.contains("著者「村上春樹」は既に追加されています") ?: false)
        verify(bookRepository, never()).update(any())
    }

    @Test
    fun `removeAuthor - 正常系`() {
        // Given
        val secondAuthor =
            Author(
                id = 2L,
                name = "東野圭吾",
                birthDate = LocalDate.of(1958, 2, 4),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
        val bookWithTwoAuthors = testBook.copy(authors = listOf(testAuthor, secondAuthor))

        whenever(bookRepository.findById(1L)).thenReturn(bookWithTwoAuthors)
        whenever(bookRepository.update(any())).thenReturn(bookWithTwoAuthors.copy(authors = listOf(testAuthor)))

        // When
        val result = bookService.removeAuthor(1L, 2L)

        // Then
        assertEquals(1, result.authors.size)
        assertEquals("村上春樹", result.authorNames)
        verify(bookRepository).update(any())
    }

    @Test
    fun `removeAuthor - 最後の著者を削除（最低1人必要でエラー）`() {
        // Given
        whenever(bookRepository.findById(1L)).thenReturn(testBook)

        // When & Then
        val exception =
            assertThrows<IllegalArgumentException> {
                bookService.removeAuthor(1L, 1L)
            }
        assertTrue(exception.message?.contains("著者の削除に失敗しました") ?: false)
        verify(bookRepository, never()).update(any())
    }

    @Test
    fun `deleteById - 正常系`() {
        // Given
        whenever(bookRepository.existsById(1L)).thenReturn(true)
        whenever(bookRepository.deleteById(1L)).thenReturn(true)

        // When
        bookService.deleteById(1L)

        // Then
        verify(bookRepository).deleteById(1L)
    }

    @Test
    fun `deleteById - 書籍が存在しない`() {
        // Given
        whenever(bookRepository.existsById(999L)).thenReturn(false)

        // When & Then
        val exception =
            assertThrows<ResourceNotFoundException> {
                bookService.deleteById(999L)
            }
        assertEquals("指定されたID=999 の書籍が見つかりません", exception.message)
        verify(bookRepository, never()).deleteById(any())
    }

    @Test
    fun `existsById - 正常系`() {
        // Given
        whenever(bookRepository.existsById(1L)).thenReturn(true)
        whenever(bookRepository.existsById(999L)).thenReturn(false)

        // When & Then
        assertTrue(bookService.existsById(1L))
        assertFalse(bookService.existsById(999L))
    }

    @Test
    fun `findByPublicationStatus - 正常系`() {
        // Given
        val publishedBooks = listOf(testBook)
        whenever(bookRepository.findByPublicationStatus(PublicationStatus.PUBLISHED, 0, 20)).thenReturn(publishedBooks)
        whenever(bookRepository.countByPublicationStatus(PublicationStatus.PUBLISHED)).thenReturn(1L)

        // When
        val result = bookService.findByPublicationStatus(PublicationStatus.PUBLISHED, 0, 20)

        // Then
        assertEquals(1, result.content.size)
        assertTrue(result.content[0].isPublished)
    }

    @Test
    fun `findByAuthorId - 正常系`() {
        // Given
        val books = listOf(testBook)
        whenever(authorRepository.existsById(1L)).thenReturn(true)
        whenever(bookRepository.findByAuthorId(1L, 0, 20)).thenReturn(books)
        whenever(bookRepository.countByAuthorId(1L)).thenReturn(1L)

        // When
        val result = bookService.findByAuthorId(1L, 0, 20)

        // Then
        assertEquals(1, result.content.size)
        assertEquals("村上春樹", result.content[0].authorNames)
    }

    @Test
    fun `findByAuthorId - 著者が存在しない`() {
        // Given
        whenever(authorRepository.existsById(999L)).thenReturn(false)

        // When & Then
        val exception =
            assertThrows<ResourceNotFoundException> {
                bookService.findByAuthorId(999L, 0, 20)
            }
        assertEquals("指定されたID=999 の著者が見つかりません", exception.message)
    }

    @Test
    fun `findPublishedBooks - 正常系`() {
        // Given
        val publishedBooks = listOf(testBook)
        whenever(bookRepository.findByPublicationStatus(PublicationStatus.PUBLISHED, 0, 20)).thenReturn(publishedBooks)
        whenever(bookRepository.countByPublicationStatus(PublicationStatus.PUBLISHED)).thenReturn(1L)

        // When
        val result = bookService.findPublishedBooks(0, 20)

        // Then
        assertEquals(1, result.content.size)
        assertTrue(result.content.all { it.isPublished })
    }

    @Test
    fun `findUnpublishedBooks - 正常系`() {
        // Given
        val unpublishedBook = testBook.copy(publicationStatus = PublicationStatus.UNPUBLISHED)
        val unpublishedBooks = listOf(unpublishedBook)
        whenever(
            bookRepository.findByPublicationStatus(PublicationStatus.UNPUBLISHED, 0, 20),
        ).thenReturn(unpublishedBooks)
        whenever(bookRepository.countByPublicationStatus(PublicationStatus.UNPUBLISHED)).thenReturn(1L)

        // When
        val result = bookService.findUnpublishedBooks(0, 20)

        // Then
        assertEquals(1, result.content.size)
        assertFalse(result.content.all { it.isPublished })
    }
}
