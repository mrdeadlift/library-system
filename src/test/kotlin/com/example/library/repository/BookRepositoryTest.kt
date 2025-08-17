package com.example.library.repository

import com.example.library.domain.Author
import com.example.library.domain.Book
import com.example.library.domain.PublicationStatus
import com.example.library.repository.jooq.AuthorRepository
import com.example.library.repository.jooq.BookRepository
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * BookRepositoryの統合テスト
 */
@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.properties"])
@Transactional
class BookRepositoryTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setupTimezone() {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))
            System.setProperty("user.timezone", "Asia/Tokyo")
        }
    }

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var authorRepository: AuthorRepository

    private val authorTemplate1 =
        Author(
            name = "村上春樹",
            birthDate = LocalDate.of(1949, 1, 12),
        )

    private val authorTemplate2 =
        Author(
            name = "東野圭吾",
            birthDate = LocalDate.of(1958, 2, 4),
        )

    private lateinit var testAuthor1: Author
    private lateinit var testAuthor2: Author

    @BeforeEach
    fun setupTestData() {
        // テスト用著者を作成
        testAuthor1 = authorRepository.save(authorTemplate1)
        testAuthor2 = authorRepository.save(authorTemplate2)
    }

    @Test
    fun `save - 正常系`() {
        // Given
        val book =
            Book(
                title = "ノルウェイの森",
                price = BigDecimal("1800.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authors = listOf(testAuthor1),
            )

        // When
        val savedBook = bookRepository.save(book)

        // Then
        assertNotNull(savedBook.id)
        assertEquals("ノルウェイの森", savedBook.title)
        assertEquals(BigDecimal("1800.00"), savedBook.price)
        assertEquals(PublicationStatus.PUBLISHED, savedBook.publicationStatus)
        assertEquals(1, savedBook.authors.size)
        assertEquals("村上春樹", savedBook.authors[0].name)
        assertNotNull(savedBook.createdAt)
        assertNotNull(savedBook.updatedAt)
    }

    @Test
    fun `save - 複数著者`() {
        // Given
        val book =
            Book(
                title = "共著書籍",
                price = BigDecimal("2500.00"),
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authors = listOf(testAuthor1, testAuthor2),
            )

        // When
        val savedBook = bookRepository.save(book)

        // Then
        assertNotNull(savedBook.id)
        assertEquals(2, savedBook.authors.size)
        val authorNames = savedBook.authors.map { it.name }
        assertTrue(authorNames.contains("村上春樹"))
        assertTrue(authorNames.contains("東野圭吾"))
    }

    @Test
    fun `findById - 正常系`() {
        // Given
        val savedBook =
            bookRepository.save(
                Book(
                    title = "海辺のカフカ",
                    price = BigDecimal("2200.00"),
                    publicationStatus = PublicationStatus.PUBLISHED,
                    authors = listOf(testAuthor1),
                ),
            )

        // When
        val foundBook = bookRepository.findById(savedBook.id!!)

        // Then
        assertNotNull(foundBook)
        assertEquals(savedBook.id, foundBook!!.id)
        assertEquals("海辺のカフカ", foundBook.title)
        assertEquals(1, foundBook.authors.size)
        assertEquals("村上春樹", foundBook.authors[0].name)
    }

    @Test
    fun `findById - 存在しない書籍`() {
        // When
        val foundBook = bookRepository.findById(99999L)

        // Then
        assertNull(foundBook)
    }

    @Test
    fun `findAll - 正常系`() {
        // Given
        bookRepository.save(
            Book(
                title = "書籍1",
                price = BigDecimal("1000.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authors = listOf(testAuthor1),
            ),
        )

        bookRepository.save(
            Book(
                title = "書籍2",
                price = BigDecimal("1500.00"),
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authors = listOf(testAuthor2),
            ),
        )

        // When
        val books = bookRepository.findAll(0, 10)
        val totalCount = bookRepository.countAll()

        // Then
        assertEquals(2, books.size)
        assertEquals(2L, totalCount)

        // 順序確認（IDで昇順）
        assertTrue(books[0].id!! < books[1].id!!)
    }

    @Test
    fun `findAll - ページネーション`() {
        // Given
        repeat(5) { i ->
            bookRepository.save(
                Book(
                    title = "書籍${i + 1}",
                    price = BigDecimal("${1000 + i * 100}.00"),
                    publicationStatus = PublicationStatus.PUBLISHED,
                    authors = listOf(testAuthor1),
                ),
            )
        }

        // When
        val firstPage = bookRepository.findAll(0, 3)
        val secondPage = bookRepository.findAll(3, 3)

        // Then
        assertEquals(3, firstPage.size)
        assertEquals(2, secondPage.size)

        // 重複なし確認
        val firstPageIds = firstPage.map { it.id }
        val secondPageIds = secondPage.map { it.id }
        assertTrue(firstPageIds.intersect(secondPageIds.toSet()).isEmpty())
    }

    @Test
    fun `findByTitleContaining - 正常系`() {
        // Given
        bookRepository.save(
            Book(
                title = "ノルウェイの森",
                price = BigDecimal("1800.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authors = listOf(testAuthor1),
            ),
        )

        bookRepository.save(
            Book(
                title = "海辺のカフカ",
                price = BigDecimal("2200.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authors = listOf(testAuthor1),
            ),
        )

        // When
        val result1 = bookRepository.findByTitleContaining("ノルウェイ", 0, 10)
        val result2 = bookRepository.findByTitleContaining("カフカ", 0, 10)
        val result3 = bookRepository.findByTitleContaining("存在しない", 0, 10)

        val count1 = bookRepository.countByTitleContaining("ノルウェイ")
        val count2 = bookRepository.countByTitleContaining("カフカ")

        // Then
        assertEquals(1, result1.size)
        assertEquals("ノルウェイの森", result1[0].title)
        assertEquals(1L, count1)

        assertEquals(1, result2.size)
        assertEquals("海辺のカフカ", result2[0].title)
        assertEquals(1L, count2)

        assertEquals(0, result3.size)
    }

    @Test
    fun `findByPublicationStatus - 正常系`() {
        // Given
        bookRepository.save(
            Book(
                title = "出版済み書籍",
                price = BigDecimal("1800.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authors = listOf(testAuthor1),
            ),
        )

        bookRepository.save(
            Book(
                title = "未出版書籍",
                price = BigDecimal("2000.00"),
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authors = listOf(testAuthor2),
            ),
        )

        // When
        val publishedBooks = bookRepository.findByPublicationStatus(PublicationStatus.PUBLISHED, 0, 10)
        val unpublishedBooks = bookRepository.findByPublicationStatus(PublicationStatus.UNPUBLISHED, 0, 10)

        val publishedCount = bookRepository.countByPublicationStatus(PublicationStatus.PUBLISHED)
        val unpublishedCount = bookRepository.countByPublicationStatus(PublicationStatus.UNPUBLISHED)

        // Then
        assertEquals(1, publishedBooks.size)
        assertEquals("出版済み書籍", publishedBooks[0].title)
        assertEquals(PublicationStatus.PUBLISHED, publishedBooks[0].publicationStatus)
        assertEquals(1L, publishedCount)

        assertEquals(1, unpublishedBooks.size)
        assertEquals("未出版書籍", unpublishedBooks[0].title)
        assertEquals(PublicationStatus.UNPUBLISHED, unpublishedBooks[0].publicationStatus)
        assertEquals(1L, unpublishedCount)
    }

    @Test
    fun `findByAuthorId - 正常系`() {
        // Given
        bookRepository.save(
            Book(
                title = "村上春樹の書籍1",
                price = BigDecimal("1800.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authors = listOf(testAuthor1),
            ),
        )

        bookRepository.save(
            Book(
                title = "共著書籍",
                price = BigDecimal("2500.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authors = listOf(testAuthor1, testAuthor2),
            ),
        )

        bookRepository.save(
            Book(
                title = "東野圭吾の書籍",
                price = BigDecimal("1500.00"),
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authors = listOf(testAuthor2),
            ),
        )

        // When
        val author1Books = bookRepository.findByAuthorId(testAuthor1.id!!, 0, 10)
        val author2Books = bookRepository.findByAuthorId(testAuthor2.id!!, 0, 10)

        val author1Count = bookRepository.countByAuthorId(testAuthor1.id!!)
        val author2Count = bookRepository.countByAuthorId(testAuthor2.id!!)

        // Then
        assertEquals(2, author1Books.size) // 単著1冊 + 共著1冊
        assertEquals(2L, author1Count)
        assertTrue(author1Books.any { it.title == "村上春樹の書籍1" })
        assertTrue(author1Books.any { it.title == "共著書籍" })

        assertEquals(2, author2Books.size) // 単著1冊 + 共著1冊
        assertEquals(2L, author2Count)
        assertTrue(author2Books.any { it.title == "共著書籍" })
        assertTrue(author2Books.any { it.title == "東野圭吾の書籍" })
    }

    @Test
    fun `update - 正常系`() {
        // Given
        val originalBook =
            bookRepository.save(
                Book(
                    title = "元のタイトル",
                    price = BigDecimal("1000.00"),
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                    authors = listOf(testAuthor1),
                ),
            )

        // When
        val updatedBook =
            originalBook.copy(
                title = "更新後のタイトル",
                price = BigDecimal("1500.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authors = listOf(testAuthor1, testAuthor2),
            )

        val result = bookRepository.update(updatedBook)

        // Then
        assertEquals("更新後のタイトル", result.title)
        assertEquals(BigDecimal("1500.00"), result.price)
        assertEquals(PublicationStatus.PUBLISHED, result.publicationStatus)
        assertEquals(2, result.authors.size)

        // データベースから再取得して確認
        val reloadedBook = bookRepository.findById(originalBook.id!!)!!
        assertEquals("更新後のタイトル", reloadedBook.title)
        assertEquals(2, reloadedBook.authors.size)
    }

    @Test
    fun `existsByTitle - 正常系`() {
        // Given
        bookRepository.save(
            Book(
                title = "重複チェック書籍",
                price = BigDecimal("1000.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authors = listOf(testAuthor1),
            ),
        )

        // When & Then
        assertTrue(bookRepository.existsByTitle("重複チェック書籍"))
        assertFalse(bookRepository.existsByTitle("存在しない書籍"))
    }

    @Test
    fun `existsByTitleAndIdNot - 正常系`() {
        // Given
        val savedBook =
            bookRepository.save(
                Book(
                    title = "更新チェック書籍",
                    price = BigDecimal("1000.00"),
                    publicationStatus = PublicationStatus.PUBLISHED,
                    authors = listOf(testAuthor1),
                ),
            )

        // When & Then
        assertFalse(bookRepository.existsByTitleAndIdNot("更新チェック書籍", savedBook.id!!)) // 自分自身は除外
        assertTrue(bookRepository.existsByTitleAndIdNot("更新チェック書籍", 99999L)) // 他のIDなら存在
        assertFalse(bookRepository.existsByTitleAndIdNot("存在しない書籍", savedBook.id)) // 存在しないタイトル
    }
}
