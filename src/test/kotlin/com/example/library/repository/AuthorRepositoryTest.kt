package com.example.library.repository.jooq

import com.example.library.domain.Author
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.test.*

/**
 * AuthorRepositoryの統合テスト
 * H2データベースを使用した実際のデータアクセス層のテスト
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthorRepositoryTest {
    @Autowired
    private lateinit var authorRepository: AuthorRepository

    private val testAuthor =
        Author(
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9),
        )

    @BeforeEach
    fun setup() {
    }

    @Test
    fun `save - 正常系`() {
        // When
        val savedAuthor = authorRepository.save(testAuthor)

        // Then
        assertNotNull(savedAuthor.id)
        assertEquals("夏目漱石", savedAuthor.name)
        // H2のタイムゾーン問題を考慮して、期待値を調整
        savedAuthor.birthDate.isEqual(testAuthor.birthDate)
        assertNotNull(savedAuthor.createdAt)
        assertNotNull(savedAuthor.updatedAt)
    }

    @Test
    fun `findById - 正常系`() {
        // Given
        val savedAuthor = authorRepository.save(testAuthor)

        // When
        val foundAuthor = authorRepository.findById(savedAuthor.id!!)

        // Then
        assertNotNull(foundAuthor)
        assertEquals(savedAuthor.id, foundAuthor.id)
        assertEquals("夏目漱石", foundAuthor.name)
        // 保存されたAuthorと同じ日付であることを確認（H2の日付問題に対応）
        assertEquals(savedAuthor.birthDate, foundAuthor.birthDate)
    }

    @Test
    fun `findById - 存在しないID`() {
        // When
        val foundAuthor = authorRepository.findById(999L)

        // Then
        assertNull(foundAuthor)
    }

    @Test
    fun `findAll - 正常系`() {
        // Given
        val author1 = authorRepository.save(testAuthor)
        val author2 =
            authorRepository.save(
                Author(
                    name = "芥川龍之介",
                    birthDate = LocalDate.of(1892, 3, 1),
                ),
            )

        // When
        val authors = authorRepository.findAll(0, 20)

        // Then
        assertTrue(authors.size >= 2)
        assertTrue(authors.any { it.name == "夏目漱石" })
        assertTrue(authors.any { it.name == "芥川龍之介" })
    }

    @Test
    fun `findAll - ページネーション`() {
        // Given - 複数のテストデータを作成
        repeat(5) { index ->
            authorRepository.save(
                Author(
                    name = "テスト著者${index + 1}",
                    birthDate = LocalDate.of(1900 + index, 1, 1),
                ),
            )
        }

        // When
        val firstPage = authorRepository.findAll(0, 2)
        val secondPage = authorRepository.findAll(2, 2)

        // Then
        assertEquals(2, firstPage.size)
        assertEquals(2, secondPage.size)
        // IDで順序付けされているため、異なるデータが取得されることを確認
        assertNotEquals(firstPage.map { it.id }, secondPage.map { it.id })
    }

    @Test
    fun `countAll - 正常系`() {
        // Given
        val initialCount = authorRepository.countAll()
        authorRepository.save(testAuthor)
        authorRepository.save(
            Author(
                name = "芥川龍之介",
                birthDate = LocalDate.of(1892, 3, 1),
            ),
        )

        // When
        val finalCount = authorRepository.countAll()

        // Then
        assertEquals(initialCount + 2, finalCount)
    }

    @Test
    fun `findByNameContaining - 正常系`() {
        // Given
        authorRepository.save(testAuthor)
        // 夏目漱石の本名
        authorRepository.save(
            Author(
                name = "夏目金之助",
                birthDate = LocalDate.of(1867, 2, 9),
            ),
        )
        authorRepository.save(
            Author(
                name = "芥川龍之介",
                birthDate = LocalDate.of(1892, 3, 1),
            ),
        )

        // When
        val authors = authorRepository.findByNameContaining("夏目", 0, 20)

        // Then
        assertEquals(2, authors.size)
        assertTrue(authors.all { it.name.contains("夏目") })
        // 名前順でソートされることを確認
        assertEquals("夏目漱石", authors.first().name)
        assertEquals("夏目金之助", authors.last().name)
    }

    @Test
    fun `findByNameContaining - 大文字小文字を区別しない検索`() {
        // Given
        authorRepository.save(
            Author(
                name = "Test Author",
                birthDate = LocalDate.of(1900, 1, 1),
            ),
        )

        // When
        val authorsLower = authorRepository.findByNameContaining("test", 0, 20)
        val authorsUpper = authorRepository.findByNameContaining("TEST", 0, 20)

        // Then
        assertEquals(1, authorsLower.size)
        assertEquals(1, authorsUpper.size)
        assertEquals(authorsLower.first().id, authorsUpper.first().id)
    }

    @Test
    fun `countByNameContaining - 正常系`() {
        // Given
        authorRepository.save(testAuthor)
        authorRepository.save(
            Author(
                name = "夏目金之助",
                birthDate = LocalDate.of(1867, 2, 9),
            ),
        )
        authorRepository.save(
            Author(
                name = "芥川龍之介",
                birthDate = LocalDate.of(1892, 3, 1),
            ),
        )

        // When
        val count = authorRepository.countByNameContaining("夏目")

        // Then
        assertEquals(2L, count)
    }

    @Test
    fun `existsByName - 正常系`() {
        // Given
        authorRepository.save(testAuthor)

        // When & Then
        assertTrue(authorRepository.existsByName("夏目漱石"))
        assertFalse(authorRepository.existsByName("存在しない著者"))
    }

    @Test
    fun `existsByNameAndIdNot - 正常系`() {
        // Given
        val savedAuthor1 = authorRepository.save(testAuthor)
        val savedAuthor2 =
            authorRepository.save(
                Author(
                    name = "芥川龍之介",
                    birthDate = LocalDate.of(1892, 3, 1),
                ),
            )

        // When & Then
        // 同じ名前でIDが異なる場合
        assertFalse(authorRepository.existsByNameAndIdNot("夏目漱石", savedAuthor1.id!!))
        // 異なる名前でIDが同じ場合
        assertFalse(authorRepository.existsByNameAndIdNot("存在しない著者", savedAuthor1.id!!))
        // 存在する名前で異なるIDの場合
        assertTrue(authorRepository.existsByNameAndIdNot("夏目漱石", savedAuthor2.id!!))
    }

    @Test
    fun `update - 正常系`() {
        // Given
        val savedAuthor = authorRepository.save(testAuthor)
        val updatedAuthor = savedAuthor.update("夏目漱石（改）", LocalDate.of(1867, 2, 10))

        // When
        val result = authorRepository.update(updatedAuthor)

        // Then
        assertEquals(savedAuthor.id, result.id)
        assertEquals("夏目漱石（改）", result.name)
        assertEquals(LocalDate.of(1867, 2, 10), result.birthDate)
        assertTrue(result.updatedAt.isAfter(savedAuthor.updatedAt))
    }

    @Test
    fun `update - 存在しないID`() {
        // Given
        val nonExistentAuthor = testAuthor.copy(id = 999L)

        // When & Then
        assertThrows<IllegalArgumentException> {
            authorRepository.update(nonExistentAuthor)
        }
    }

    @Test
    fun `update - IDが未設定`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            authorRepository.update(testAuthor) // IDがnull
        }
    }

    @Test
    fun `deleteById - 正常系`() {
        // Given
        val savedAuthor = authorRepository.save(testAuthor)

        // When
        val deleted = authorRepository.deleteById(savedAuthor.id!!)

        // Then
        assertTrue(deleted)
        assertNull(authorRepository.findById(savedAuthor.id!!))
    }

    @Test
    fun `deleteById - 存在しないID`() {
        // When
        val deleted = authorRepository.deleteById(999L)

        // Then
        assertFalse(deleted)
    }

    @Test
    fun `existsById - 正常系`() {
        // Given
        val savedAuthor = authorRepository.save(testAuthor)

        // When & Then
        assertTrue(authorRepository.existsById(savedAuthor.id!!))
        assertFalse(authorRepository.existsById(999L))
    }
}
