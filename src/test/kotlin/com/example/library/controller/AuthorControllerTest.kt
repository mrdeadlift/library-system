package com.example.library.controller

import com.example.library.controller.dto.AuthorCreateRequest
import com.example.library.controller.dto.AuthorResponse
import com.example.library.controller.dto.AuthorUpdateRequest
import com.example.library.controller.dto.PagedResponse
import com.example.library.service.AuthorService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * AuthorController のユニットテスト
 *
 * Service層をモック化してController単体の動作を検証する
 */
@WebMvcTest(AuthorController::class)
class AuthorControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var authorService: AuthorService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    /**
     * GET /api/authors - 著者一覧取得のテスト
     */
    @Test
    fun `getAuthors should return paged response with default parameters`() {
        // Given
        val authors =
            listOf(
                AuthorResponse(
                    id = 1L,
                    name = "山田太郎",
                    birthDate = LocalDate.of(1980, 1, 1),
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                ),
                AuthorResponse(
                    id = 2L,
                    name = "佐藤花子",
                    birthDate = LocalDate.of(1975, 5, 15),
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                ),
            )
        val pagedResponse =
            PagedResponse(
                content = authors,
                pageNumber = 0,
                pageSize = 20,
                totalElements = 2L,
                totalPages = 1,
                isFirst = true,
                isLast = true,
                isEmpty = false,
            )

        whenever(authorService.findAll(0, 20)).thenReturn(pagedResponse)

        // When & Then
        mockMvc
            .perform(get("/api/authors"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].name").value("山田太郎"))
            .andExpect(jsonPath("$.content[1].id").value(2))
            .andExpect(jsonPath("$.content[1].name").value("佐藤花子"))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.pageSize").value(20))

        verify(authorService).findAll(0, 20)
    }

    @Test
    fun `getAuthors should return filtered results when name parameter is provided`() {
        // Given
        val authors =
            listOf(
                AuthorResponse(
                    id = 1L,
                    name = "山田太郎",
                    birthDate = LocalDate.of(1980, 1, 1),
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                ),
            )
        val pagedResponse =
            PagedResponse(
                content = authors,
                pageNumber = 0,
                pageSize = 20,
                totalElements = 1L,
                totalPages = 1,
                isFirst = true,
                isLast = true,
                isEmpty = false,
            )

        whenever(authorService.searchByName("山田", 0, 20)).thenReturn(pagedResponse)

        // When & Then
        mockMvc
            .perform(
                get("/api/authors")
                    .param("name", "山田")
                    .param("page", "0")
                    .param("size", "20"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].name").value("山田太郎"))
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(authorService).searchByName("山田", 0, 20)
    }

    @Test
    fun `getAuthors should use custom pagination parameters`() {
        // Given
        val pagedResponse =
            PagedResponse(
                content = emptyList<AuthorResponse>(),
                pageNumber = 2,
                pageSize = 10,
                totalElements = 50L,
                totalPages = 5,
                isFirst = false,
                isLast = false,
                isEmpty = true,
            )

        whenever(authorService.findAll(2, 10)).thenReturn(pagedResponse)

        // When & Then
        mockMvc
            .perform(
                get("/api/authors")
                    .param("page", "2")
                    .param("size", "10"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.pageNumber").value(2))
            .andExpect(jsonPath("$.pageSize").value(10))

        verify(authorService).findAll(2, 10)
    }

    /**
     * GET /api/authors/{id} - 著者詳細取得のテスト
     */
    @Test
    fun `getAuthor should return author when valid id is provided`() {
        // Given
        val author =
            AuthorResponse(
                id = 1L,
                name = "山田太郎",
                birthDate = LocalDate.of(1980, 1, 1),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        whenever(authorService.findById(1L)).thenReturn(author)

        // When & Then
        mockMvc
            .perform(get("/api/authors/1"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("山田太郎"))
            .andExpect(jsonPath("$.birthDate").value("1980-01-01"))

        verify(authorService).findById(1L)
    }

    /**
     * POST /api/authors - 著者新規登録のテスト
     */
    @Test
    fun `createAuthor should return created author with 201 status`() {
        // Given
        val createRequest =
            AuthorCreateRequest(
                name = "新しい作家",
                birthDate = LocalDate.of(1990, 3, 20),
            )
        val createdAuthor =
            AuthorResponse(
                id = 3L,
                name = "新しい作家",
                birthDate = LocalDate.of(1990, 3, 20),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        whenever(authorService.create(createRequest)).thenReturn(createdAuthor)

        // When & Then
        mockMvc
            .perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)),
            ).andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.name").value("新しい作家"))
            .andExpect(jsonPath("$.birthDate").value("1990-03-20"))

        verify(authorService).create(createRequest)
    }

    @Test
    fun `createAuthor should return 400 when request body is invalid`() {
        // Given - 名前が空のリクエスト
        val invalidRequest = """{"name": "", "birthDate": "1990-01-01"}"""

        // When & Then
        mockMvc
            .perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidRequest),
            ).andExpect(status().isBadRequest)

        verify(authorService, never()).create(any())
    }

    /**
     * PUT /api/authors/{id} - 著者更新のテスト
     */
    @Test
    fun `updateAuthor should return updated author`() {
        // Given
        val updateRequest =
            AuthorUpdateRequest(
                name = "更新された作家",
                birthDate = LocalDate.of(1980, 1, 1),
            )
        val updatedAuthor =
            AuthorResponse(
                id = 1L,
                name = "更新された作家",
                birthDate = LocalDate.of(1980, 1, 1),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        whenever(authorService.update(1L, updateRequest)).thenReturn(updatedAuthor)

        // When & Then
        mockMvc
            .perform(
                put("/api/authors/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
            ).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("更新された作家"))

        verify(authorService).update(1L, updateRequest)
    }
}
