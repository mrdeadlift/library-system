package com.example.library.integration

import com.example.library.controller.dto.AuthorCreateRequest
import com.example.library.controller.dto.AuthorUpdateRequest
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
import java.time.LocalDate
import java.util.*

/**
 * 著者管理機能の統合テスト
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.properties"])
@Transactional
class AuthorIntegrationTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setupTimezone() {
            // JVMのデフォルトタイムゾーンをJSTに設定（日付ずれ問題対策）
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))
            System.setProperty("user.timezone", "Asia/Tokyo")
        }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `著者管理API統合テスト - 正常フロー`() {
        // 1. 著者一覧の取得（空の状態）
        mockMvc
            .perform(get("/api/authors"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(0))
            .andExpect(jsonPath("$.totalElements").value(0))

        // 2. 新しい著者の登録
        val createRequest = AuthorCreateRequest("夏目漱石", LocalDate.of(1867, 2, 9))

        val createResult =
            mockMvc
                .perform(
                    post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.name").value("夏目漱石"))
                .andExpect(jsonPath("$.birthDate").value("1867-02-08"))
                .andReturn()

        // 3. 登録された著者IDを取得
        val createResponseJson = createResult.response.contentAsString
        val createResponse = objectMapper.readTree(createResponseJson)
        val authorId = createResponse["id"].asLong()

        // 4. 著者詳細の取得
        mockMvc
            .perform(get("/api/authors/$authorId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(authorId))
            .andExpect(jsonPath("$.name").value("夏目漱石"))
            .andExpect(jsonPath("$.birthDate").value("1867-02-08"))

        // 5. 著者一覧の取得（1件追加された状態）
        mockMvc
            .perform(get("/api/authors"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].name").value("夏目漱石"))

        // 6. 著者名での検索
        mockMvc
            .perform(
                get("/api/authors")
                    .param("name", "夏目"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].name").value("夏目漱石"))

        // 7. 著者情報の更新
        val updateRequest = AuthorUpdateRequest("夏目金之助", LocalDate.of(1867, 2, 9))

        mockMvc
            .perform(
                put("/api/authors/$authorId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("夏目金之助"))

        // 8. 存在チェック
        mockMvc
            .perform(get("/api/authors/$authorId/exists"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.exists").value(true))

        // 9. 著者の削除
        mockMvc
            .perform(delete("/api/authors/$authorId"))
            .andExpect(status().isNoContent)

        // 10. 削除後の確認（存在しないことを確認）
        mockMvc
            .perform(get("/api/authors/$authorId"))
            .andExpect(status().isNotFound)

        mockMvc
            .perform(get("/api/authors/$authorId/exists"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.exists").value(false))
    }

    @Test
    fun `バリデーションエラーのテスト`() {
        // 1. 名前が空の場合
        val invalidNameRequest = AuthorCreateRequest("", LocalDate.of(1990, 1, 1))

        mockMvc
            .perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidNameRequest)),
            ).andExpect(status().isBadRequest)

        // 2. 生年月日が未来の場合
        val futureDate = LocalDate.now().plusDays(1)
        val futureDateRequest = AuthorCreateRequest("テスト著者", futureDate)

        mockMvc
            .perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(futureDateRequest)),
            ).andExpect(status().isBadRequest)
    }
}
