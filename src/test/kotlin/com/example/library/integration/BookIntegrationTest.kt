package com.example.library.integration

import com.example.library.controller.dto.AuthorCreateRequest
import com.example.library.controller.dto.BookCreateRequest
import com.example.library.controller.dto.BookUpdateRequest
import com.example.library.domain.PublicationStatus
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
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

/**
 * BookControllerの統合テスト
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.properties"])
@Transactional
class BookIntegrationTest {
    
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
    
    private var testAuthorId: Long = 0
    private var secondAuthorId: Long = 0
    
    @BeforeEach
    fun setupTestData() {
        // テスト用著者を作成
        val authorRequest1 = AuthorCreateRequest("村上春樹", LocalDate.of(1949, 1, 12))
        val authorResult1 = mockMvc.perform(post("/api/authors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(authorRequest1)))
            .andExpect(status().isCreated)
            .andReturn()
        
        val authorResponse1 = objectMapper.readTree(authorResult1.response.contentAsString)
        testAuthorId = authorResponse1["id"].asLong()
        
        // 2人目の著者も作成
        val authorRequest2 = AuthorCreateRequest("東野圭吾", LocalDate.of(1958, 2, 4))
        val authorResult2 = mockMvc.perform(post("/api/authors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(authorRequest2)))
            .andExpect(status().isCreated)
            .andReturn()
        
        val authorResponse2 = objectMapper.readTree(authorResult2.response.contentAsString)
        secondAuthorId = authorResponse2["id"].asLong()
    }
    
    @Test
    fun `書籍管理API統合テスト - 正常フロー`() {
        // 1. 書籍一覧の取得（空の状態）
        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(0))
            .andExpect(jsonPath("$.totalElements").value(0))
        
        // 2. 新しい書籍の登録
        val createRequest = BookCreateRequest(
            title = "ノルウェイの森",
            price = BigDecimal("1800.00"),
            publicationStatus = PublicationStatus.PUBLISHED,
            authorIds = listOf(testAuthorId)
        )
        
        val createResult = mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value("ノルウェイの森"))
            .andExpect(jsonPath("$.price").value(1800.00))
            .andExpect(jsonPath("$.formattedPrice").value("¥1800.00"))
            .andExpect(jsonPath("$.publicationStatus").value("PUBLISHED"))
            .andExpect(jsonPath("$.published").value(true))
            .andExpect(jsonPath("$.authors").isArray)
            .andExpect(jsonPath("$.authors.length()").value(1))
            .andExpect(jsonPath("$.authors[0].name").value("村上春樹"))
            .andExpect(jsonPath("$.authorNames").value("村上春樹"))
            .andReturn()
        
        // 3. 登録された書籍IDを取得
        val createResponseJson = createResult.response.contentAsString
        val createResponse = objectMapper.readTree(createResponseJson)
        val bookId = createResponse["id"].asLong()
        
        // 4. 書籍詳細の取得
        mockMvc.perform(get("/api/books/$bookId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(bookId))
            .andExpect(jsonPath("$.title").value("ノルウェイの森"))
            .andExpect(jsonPath("$.authors[0].name").value("村上春樹"))
        
        // 5. 書籍一覧の取得（1件追加された状態）
        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].title").value("ノルウェイの森"))
        
        // 6. 書籍情報の更新
        val updateRequest = BookUpdateRequest(
            title = "ノルウェイの森（改訂版）",
            price = BigDecimal("1900.00"),
            publicationStatus = PublicationStatus.PUBLISHED,
            authorIds = listOf(testAuthorId)
        )
        
        mockMvc.perform(put("/api/books/$bookId")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("ノルウェイの森（改訂版）"))
            .andExpect(jsonPath("$.price").value(1900.00))
        
        // 7. 存在チェック
        mockMvc.perform(get("/api/books/$bookId/exists"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.exists").value(true))
        
        // 8. 書籍の削除
        mockMvc.perform(delete("/api/books/$bookId"))
            .andExpect(status().isNoContent)
        
        // 9. 削除後の確認
        mockMvc.perform(get("/api/books/$bookId"))
            .andExpect(status().isNotFound)
        
        mockMvc.perform(get("/api/books/$bookId/exists"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.exists").value(false))
    }
    
    @Test
    fun `書籍検索機能のテスト`() {
        // テスト用書籍を複数作成
        val book1 = BookCreateRequest(
            title = "海辺のカフカ",
            price = BigDecimal("2200.00"),
            publicationStatus = PublicationStatus.PUBLISHED,
            authorIds = listOf(testAuthorId)
        )
        
        val book2 = BookCreateRequest(
            title = "容疑者Xの献身",
            price = BigDecimal("1500.00"),
            publicationStatus = PublicationStatus.UNPUBLISHED,
            authorIds = listOf(secondAuthorId)
        )
        
        // 書籍を登録
        mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(book1)))
            .andExpect(status().isCreated)
        
        mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(book2)))
            .andExpect(status().isCreated)
        
        // 1. タイトルでの部分検索
        mockMvc.perform(get("/api/books").param("title", "カフカ"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("海辺のカフカ"))
        
        // 2. 出版状況でのフィルター（出版済み）
        mockMvc.perform(get("/api/books").param("status", "PUBLISHED"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].publicationStatus").value("PUBLISHED"))
        
        // 3. 出版状況でのフィルター（未出版）
        mockMvc.perform(get("/api/books").param("status", "UNPUBLISHED"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].publicationStatus").value("UNPUBLISHED"))
        
        // 4. 著者IDでの検索
        mockMvc.perform(get("/api/books").param("authorId", testAuthorId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].authors[0].name").value("村上春樹"))
        
        // 5. 出版済み書籍専用エンドポイント
        mockMvc.perform(get("/api/books/published"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].published").value(true))
        
        // 6. 未出版書籍専用エンドポイント
        mockMvc.perform(get("/api/books/unpublished"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].published").value(false))
    }
    
    @Test
    fun `出版状況更新のテスト`() {
        // テスト用書籍を作成
        val createRequest = BookCreateRequest(
            title = "テスト書籍",
            price = BigDecimal("1000.00"),
            publicationStatus = PublicationStatus.UNPUBLISHED,
            authorIds = listOf(testAuthorId)
        )
        
        val createResult = mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated)
            .andReturn()
        
        val bookId = objectMapper.readTree(createResult.response.contentAsString)["id"].asLong()
        
        // 1. 未出版 → 出版済みに変更
        mockMvc.perform(patch("/api/books/$bookId/publication-status")
            .param("status", "PUBLISHED"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.publicationStatus").value("PUBLISHED"))
            .andExpect(jsonPath("$.published").value(true))
        
        // 2. 出版済み → 未出版への変更（ビジネスルール違反でエラー）
        mockMvc.perform(patch("/api/books/$bookId/publication-status")
            .param("status", "UNPUBLISHED"))
            .andExpect(status().isBadRequest)
    }
    
    @Test
    fun `著者管理のテスト`() {
        // テスト用書籍を作成
        val createRequest = BookCreateRequest(
            title = "共著書籍",
            price = BigDecimal("2500.00"),
            publicationStatus = PublicationStatus.PUBLISHED,
            authorIds = listOf(testAuthorId)
        )
        
        val createResult = mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated)
            .andReturn()
        
        val bookId = objectMapper.readTree(createResult.response.contentAsString)["id"].asLong()
        
        // 1. 著者を追加
        mockMvc.perform(post("/api/books/$bookId/authors/$secondAuthorId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.authors.length()").value(2))
            .andExpect(jsonPath("$.authorNames").value("村上春樹, 東野圭吾"))
        
        // 2. 著者を削除（1人残すので成功）
        mockMvc.perform(delete("/api/books/$bookId/authors/$secondAuthorId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.authors.length()").value(1))
            .andExpect(jsonPath("$.authorNames").value("村上春樹"))
        
        // 3. 最後の著者を削除しようとする（最低1人必要なのでエラー）
        mockMvc.perform(delete("/api/books/$bookId/authors/$testAuthorId"))
            .andExpect(status().isBadRequest)
    }
    
    @Test
    fun `バリデーションエラーのテスト`() {
        // 1. タイトルが空の場合
        val invalidTitleRequest = BookCreateRequest(
            title = "",
            price = BigDecimal("1000.00"),
            publicationStatus = PublicationStatus.PUBLISHED,
            authorIds = listOf(testAuthorId)
        )
        
        mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidTitleRequest)))
            .andExpect(status().isBadRequest)
        
        // 2. 価格が負の値の場合
        val invalidPriceRequest = BookCreateRequest(
            title = "テスト書籍",
            price = BigDecimal("-100.00"),
            publicationStatus = PublicationStatus.PUBLISHED,
            authorIds = listOf(testAuthorId)
        )
        
        mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidPriceRequest)))
            .andExpect(status().isBadRequest)
        
        // 3. 著者IDが空の場合
        val noAuthorRequest = BookCreateRequest(
            title = "テスト書籍",
            price = BigDecimal("1000.00"),
            publicationStatus = PublicationStatus.PUBLISHED,
            authorIds = emptyList()
        )
        
        mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(noAuthorRequest)))
            .andExpect(status().isBadRequest)
        
        // 4. 存在しない著者IDを指定
        val invalidAuthorRequest = BookCreateRequest(
            title = "テスト書籍",
            price = BigDecimal("1000.00"),
            publicationStatus = PublicationStatus.PUBLISHED,
            authorIds = listOf(99999L)
        )
        
        mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidAuthorRequest)))
            .andExpect(status().isNotFound)
    }
    
    @Test
    fun `ページネーションのテスト`() {
        // テスト用書籍を複数作成
        (1..25).forEach { i ->
            val request = BookCreateRequest(
                title = "テスト書籍$i",
                price = BigDecimal("${1000 + i}.00"),
                publicationStatus = if (i % 2 == 0) PublicationStatus.PUBLISHED else PublicationStatus.UNPUBLISHED,
                authorIds = listOf(testAuthorId)
            )
            
            mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated)
        }
        
        // 1. 1ページ目（デフォルトページサイズ20）
        mockMvc.perform(get("/api/books").param("page", "0").param("size", "20"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(20))
            .andExpect(jsonPath("$.totalElements").value(25))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").value(false))
        
        // 2. 2ページ目
        mockMvc.perform(get("/api/books").param("page", "1").param("size", "20"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(5))
            .andExpect(jsonPath("$.first").value(false))
            .andExpect(jsonPath("$.last").value(true))
        
        // 3. カスタムページサイズ
        mockMvc.perform(get("/api/books").param("page", "0").param("size", "10"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(10))
            .andExpect(jsonPath("$.totalPages").value(3))
    }
}