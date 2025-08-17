package com.example.library.integration

import com.example.library.controller.dto.AuthorCreateRequest
import com.example.library.controller.dto.BookCreateRequest
import com.example.library.controller.dto.BookUpdateRequest
import com.example.library.domain.PublicationStatus
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import kotlin.test.assertTrue

/**
 * 書籍管理機能のエンドツーエンド統合テスト
 * 著者管理機能と書籍管理機能の連携を含む包括的なテスト
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.properties"])
@Transactional
class BookManagementIntegrationTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setupTimezone() {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))
            System.setProperty("user.timezone", "Asia/Tokyo")
        }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `書籍管理システム全体の統合テスト - 実際の運用シナリオ`() {
        // === STEP 1: 著者を登録する ===
        val author1Request = AuthorCreateRequest("村上春樹", LocalDate.of(1949, 1, 12))
        val author1Result =
            mockMvc
                .perform(
                    post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(author1Request)),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.name").value("村上春樹"))
                .andReturn()

        val author1Id = objectMapper.readTree(author1Result.response.contentAsString)["id"].asLong()

        val author2Request = AuthorCreateRequest("東野圭吾", LocalDate.of(1958, 2, 4))
        val author2Result =
            mockMvc
                .perform(
                    post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(author2Request)),
                ).andExpect(status().isCreated)
                .andReturn()

        val author2Id = objectMapper.readTree(author2Result.response.contentAsString)["id"].asLong()

        // === STEP 2: 書籍を登録する（未出版状態） ===
        val book1Request =
            BookCreateRequest(
                title = "ノルウェイの森",
                price = BigDecimal("1800.00"),
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(author1Id),
            )

        val book1Result =
            mockMvc
                .perform(
                    post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book1Request)),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.title").value("ノルウェイの森"))
                .andExpect(jsonPath("$.published").value(false))
                .andExpect(jsonPath("$.authors[0].name").value("村上春樹"))
                .andReturn()

        val book1Id = objectMapper.readTree(book1Result.response.contentAsString)["id"].asLong()

        // === STEP 3: 同じ著者の別の書籍を登録 ===
        val book2Request =
            BookCreateRequest(
                title = "海辺のカフカ",
                price = BigDecimal("2200.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(author1Id),
            )

        val book2Result =
            mockMvc
                .perform(
                    post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book2Request)),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.title").value("海辺のカフカ"))
                .andExpect(jsonPath("$.published").value(true))
                .andReturn()

        val book2Id = objectMapper.readTree(book2Result.response.contentAsString)["id"].asLong()

        // === STEP 4: 別の著者の書籍を登録 ===
        val book3Request =
            BookCreateRequest(
                title = "容疑者Xの献身",
                price = BigDecimal("1600.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(author2Id),
            )

        val book3Result =
            mockMvc
                .perform(
                    post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book3Request)),
                ).andExpect(status().isCreated)
                .andReturn()

        // === STEP 5: 全書籍の状況確認 ===
        mockMvc
            .perform(get("/api/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.totalElements").value(3))

        // === STEP 6: 著者による書籍検索 ===
        // 村上春樹の書籍検索
        mockMvc
            .perform(get("/api/books").param("authorId", author1Id.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))

        // 東野圭吾の書籍検索
        mockMvc
            .perform(get("/api/books").param("authorId", author2Id.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("容疑者Xの献身"))

        // === STEP 7: タイトル検索 ===
        mockMvc
            .perform(get("/api/books").param("title", "ノルウェイ"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("ノルウェイの森"))

        mockMvc
            .perform(get("/api/books").param("title", "カフカ"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("海辺のカフカ"))

        // === STEP 8: 書籍の出版状況を変更 ===
        // 未出版書籍を出版済みに変更
        mockMvc
            .perform(
                patch("/api/books/$book1Id/publication-status")
                    .param("status", "PUBLISHED"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.published").value(true))
            .andExpect(jsonPath("$.publicationStatus").value("PUBLISHED"))

        // === STEP 9: 共著書籍の作成 ===
        val collaborationBookRequest =
            BookCreateRequest(
                title = "共著作品",
                price = BigDecimal("2500.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(author1Id, author2Id),
            )

        val collaborationResult =
            mockMvc
                .perform(
                    post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(collaborationBookRequest)),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.title").value("共著作品"))
                .andExpect(jsonPath("$.authors.length()").value(2))
                .andExpect(jsonPath("$.authorNames").value("村上春樹, 東野圭吾"))
                .andReturn()

        val collaborationBookId = objectMapper.readTree(collaborationResult.response.contentAsString)["id"].asLong()

        // === STEP 10: 著者の追加・削除テスト ===
        // 新しい著者を作成
        val author3Request = AuthorCreateRequest("川端康成", LocalDate.of(1899, 6, 14))
        val author3Result =
            mockMvc
                .perform(
                    post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(author3Request)),
                ).andExpect(status().isCreated)
                .andReturn()

        val author3Id = objectMapper.readTree(author3Result.response.contentAsString)["id"].asLong()

        // 共著書籍に3人目の著者を追加
        mockMvc
            .perform(post("/api/books/$collaborationBookId/authors/$author3Id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.authors.length()").value(3))
            .andExpect(jsonPath("$.authorNames").value("村上春樹, 東野圭吾, 川端康成"))

        // 1人の著者を削除
        mockMvc
            .perform(delete("/api/books/$collaborationBookId/authors/$author3Id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.authors.length()").value(2))
            .andExpect(jsonPath("$.authorNames").value("村上春樹, 東野圭吾"))

        // === STEP 11: 書籍情報の更新 ===
        val updateRequest =
            BookUpdateRequest(
                title = "ノルウェイの森（改訂版）",
                price = BigDecimal("1900.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(author1Id),
            )

        mockMvc
            .perform(
                put("/api/books/$book1Id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("ノルウェイの森（改訂版）"))
            .andExpect(jsonPath("$.price").value(1900.00))

        // === STEP 12: ページネーションのテスト ===
        // さらに書籍を追加してページネーションをテスト
        repeat(18) { i ->
            val additionalBookRequest =
                BookCreateRequest(
                    title = "追加書籍${i + 1}",
                    price = BigDecimal("${1000 + i * 50}.00"),
                    publicationStatus = if (i % 2 == 0) PublicationStatus.PUBLISHED else PublicationStatus.UNPUBLISHED,
                    authorIds = listOf(author1Id),
                )

            mockMvc
                .perform(
                    post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(additionalBookRequest)),
                ).andExpect(status().isCreated)
        }

        // 1ページ目（20件）
        mockMvc
            .perform(get("/api/books").param("page", "0").param("size", "20"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(20))
            .andExpect(jsonPath("$.totalElements").value(22)) // 4 + 18
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").value(false))

        // 2ページ目（2件）
        mockMvc
            .perform(get("/api/books").param("page", "1").param("size", "20"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.first").value(false))
            .andExpect(jsonPath("$.last").value(true))

        // === STEP 13: ビジネスルールの確認 ===
        // 出版済み → 未出版への変更（エラーになることを確認）
        mockMvc
            .perform(
                patch("/api/books/$book2Id/publication-status")
                    .param("status", "UNPUBLISHED"),
            ).andExpect(status().isBadRequest)

        // 最後の著者を削除しようとする（エラーになることを確認）
        val singleAuthorBookRequest =
            BookCreateRequest(
                title = "単著書籍",
                price = BigDecimal("1500.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(author1Id),
            )

        val singleAuthorResult =
            mockMvc
                .perform(
                    post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(singleAuthorBookRequest)),
                ).andExpect(status().isCreated)
                .andReturn()

        val singleAuthorBookId = objectMapper.readTree(singleAuthorResult.response.contentAsString)["id"].asLong()

        mockMvc
            .perform(delete("/api/books/$singleAuthorBookId/authors/$author1Id"))
            .andExpect(status().isBadRequest)

        // === STEP 14: 最終的なデータ状況確認 ===
        // 全体の書籍数確認
        mockMvc
            .perform(get("/api/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalElements").value(23)) // 4 + 18 + 1 (単著) = 23

        // 著者別の書籍数確認
        mockMvc
            .perform(get("/api/books").param("authorId", author1Id.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalElements").value(22)) // 村上春樹の書籍（ノルウェイ+海辺+共著+追加18+単著）

        mockMvc
            .perform(get("/api/books").param("authorId", author2Id.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalElements").value(2)) // 東野圭吾の書籍（共著+単著）
    }

    @Test
    fun `パフォーマンステスト - 大量データでの動作確認`() {
        // === 著者を複数作成 ===
        val authorIds = mutableListOf<Long>()
        repeat(10) { i ->
            val authorRequest = AuthorCreateRequest("著者$i", LocalDate.of(1950 + i, 1, 1))
            val result =
                mockMvc
                    .perform(
                        post("/api/authors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authorRequest)),
                    ).andExpect(status().isCreated)
                    .andReturn()

            authorIds.add(objectMapper.readTree(result.response.contentAsString)["id"].asLong())
        }

        // === 大量の書籍を作成 ===
        repeat(100) { i ->
            val randomAuthorIds = authorIds.shuffled().take((1..3).random())
            val bookRequest =
                BookCreateRequest(
                    title = "大量テスト書籍$i",
                    price = BigDecimal("${1000 + i}.00"),
                    publicationStatus = if (i % 2 == 0) PublicationStatus.PUBLISHED else PublicationStatus.UNPUBLISHED,
                    authorIds = randomAuthorIds,
                )

            mockMvc
                .perform(
                    post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)),
                ).andExpect(status().isCreated)
        }

        // === パフォーマンス確認 ===
        val startTime = System.currentTimeMillis()

        // 大量データでの一覧取得
        mockMvc
            .perform(get("/api/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalElements").value(100))

        // 検索パフォーマンス
        mockMvc
            .perform(get("/api/books").param("title", "大量テスト"))
            .andExpect(status().isOk)

        // 著者別検索パフォーマンス
        mockMvc
            .perform(get("/api/books").param("authorId", authorIds.first().toString()))
            .andExpect(status().isOk)

        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime

        // 処理時間が適切な範囲内であることを確認（例: 5秒以内）
        assertTrue(executionTime < 5000, "処理時間が長すぎます: ${executionTime}ms")
    }
}
