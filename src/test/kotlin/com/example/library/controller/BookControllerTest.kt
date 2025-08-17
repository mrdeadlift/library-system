package com.example.library.controller

import com.example.library.controller.dto.AuthorResponse
import com.example.library.controller.dto.BookCreateRequest
import com.example.library.controller.dto.BookResponse
import com.example.library.controller.dto.BookUpdateRequest
import com.example.library.controller.dto.PagedResponse
import com.example.library.domain.PublicationStatus
import com.example.library.service.BookService
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
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * BookController のユニットテスト
 *
 * Service層をモック化してController単体の動作を検証する
 */
@WebMvcTest(BookController::class)
class BookControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var bookService: BookService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private fun createSampleBookResponse(
        id: Long = 1L,
        title: String = "サンプル書籍",
        publicationStatus: PublicationStatus = PublicationStatus.PUBLISHED,
    ) = BookResponse(
        id = id,
        title = title,
        price = BigDecimal("1500.00"),
        formattedPrice = "¥1,500",
        publicationStatus = publicationStatus,
        isPublished = publicationStatus == PublicationStatus.PUBLISHED,
        authors =
            listOf(
                AuthorResponse(
                    id = 1L,
                    name = "著者名",
                    birthDate = LocalDate.of(1980, 1, 1),
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                ),
            ),
        authorNames = "著者名",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
    )

    /**
     * GET /api/books - 書籍一覧取得のテスト
     */
    @Test
    fun `getBooks should return paged response with default parameters`() {
        // Given
        val books =
            listOf(
                createSampleBookResponse(1L, "書籍1"),
                createSampleBookResponse(2L, "書籍2"),
            )
        val pagedResponse =
            PagedResponse(
                content = books,
                pageNumber = 0,
                pageSize = 20,
                totalElements = 2L,
                totalPages = 1,
                isFirst = true,
                isLast = true,
                isEmpty = false,
            )

        whenever(bookService.findAll(0, 20)).thenReturn(pagedResponse)

        // When & Then
        mockMvc
            .perform(get("/api/books"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].title").value("書籍1"))
            .andExpect(jsonPath("$.content[1].id").value(2))
            .andExpect(jsonPath("$.content[1].title").value("書籍2"))
            .andExpect(jsonPath("$.totalElements").value(2))

        verify(bookService).findAll(0, 20)
    }

    @Test
    fun `getBooks should return filtered results when title parameter is provided`() {
        // Given
        val books = listOf(createSampleBookResponse(1L, "検索結果"))
        val pagedResponse =
            PagedResponse(
                content = books,
                pageNumber = 0,
                pageSize = 20,
                totalElements = 1L,
                totalPages = 1,
                isFirst = true,
                isLast = true,
                isEmpty = false,
            )

        whenever(bookService.searchByTitle("検索", 0, 20)).thenReturn(pagedResponse)

        // When & Then
        mockMvc
            .perform(
                get("/api/books")
                    .param("title", "検索"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].title").value("検索結果"))
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(bookService).searchByTitle("検索", 0, 20)
    }

    @Test
    fun `getBooks should return filtered results when status parameter is provided`() {
        // Given
        val books = listOf(createSampleBookResponse(publicationStatus = PublicationStatus.PUBLISHED))
        val pagedResponse =
            PagedResponse(
                content = books,
                pageNumber = 0,
                pageSize = 20,
                totalElements = 1L,
                totalPages = 1,
                isFirst = true,
                isLast = true,
                isEmpty = false,
            )

        whenever(bookService.findByPublicationStatus(PublicationStatus.PUBLISHED, 0, 20))
            .thenReturn(pagedResponse)

        // When & Then
        mockMvc
            .perform(
                get("/api/books")
                    .param("status", "PUBLISHED"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].publicationStatus").value("PUBLISHED"))

        verify(bookService).findByPublicationStatus(PublicationStatus.PUBLISHED, 0, 20)
    }

    @Test
    fun `getBooks should return filtered results when authorId parameter is provided`() {
        // Given
        val books = listOf(createSampleBookResponse())
        val pagedResponse =
            PagedResponse(
                content = books,
                pageNumber = 0,
                pageSize = 20,
                totalElements = 1L,
                totalPages = 1,
                isFirst = true,
                isLast = true,
                isEmpty = false,
            )

        whenever(bookService.findByAuthorId(1L, 0, 20)).thenReturn(pagedResponse)

        // When & Then
        mockMvc
            .perform(
                get("/api/books")
                    .param("authorId", "1"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].authors[0].id").value(1))

        verify(bookService).findByAuthorId(1L, 0, 20)
    }

    /**
     * GET /api/books/{id} - 書籍詳細取得のテスト
     */
    @Test
    fun `getBook should return book when valid id is provided`() {
        // Given
        val book = createSampleBookResponse()
        whenever(bookService.findById(1L)).thenReturn(book)

        // When & Then
        mockMvc
            .perform(get("/api/books/1"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("サンプル書籍"))
            .andExpect(jsonPath("$.price").value(1500.00))
            .andExpect(jsonPath("$.publicationStatus").value("PUBLISHED"))

        verify(bookService).findById(1L)
    }

    /**
     * POST /api/books - 書籍新規登録のテスト
     */
    @Test
    fun `createBook should return created book with 201 status`() {
        // Given
        val createRequest =
            BookCreateRequest(
                title = "新しい書籍",
                price = BigDecimal("2000.00"),
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(1L),
            )
        val createdBook =
            createSampleBookResponse(
                id = 3L,
                title = "新しい書籍",
                publicationStatus = PublicationStatus.UNPUBLISHED,
            )

        whenever(bookService.create(createRequest)).thenReturn(createdBook)

        // When & Then
        mockMvc
            .perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)),
            ).andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.title").value("新しい書籍"))
            .andExpect(jsonPath("$.publicationStatus").value("UNPUBLISHED"))

        verify(bookService).create(createRequest)
    }

    @Test
    fun `createBook should return 400 when request body is invalid`() {
        // Given - タイトルが空のリクエスト
        val invalidRequest = """{"title": "", "price": 1500, "publicationStatus": "UNPUBLISHED", "authorIds": [1]}"""

        // When & Then
        mockMvc
            .perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidRequest),
            ).andExpect(status().isBadRequest)

        verify(bookService, never()).create(any())
    }

    /**
     * PUT /api/books/{id} - 書籍更新のテスト
     */
    @Test
    fun `updateBook should return updated book`() {
        // Given
        val updateRequest =
            BookUpdateRequest(
                title = "更新された書籍",
                price = BigDecimal("1800.00"),
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(1L),
            )
        val updatedBook =
            createSampleBookResponse(
                title = "更新された書籍",
                publicationStatus = PublicationStatus.PUBLISHED,
            )

        whenever(bookService.update(1L, updateRequest)).thenReturn(updatedBook)

        // When & Then
        mockMvc
            .perform(
                put("/api/books/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
            ).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.title").value("更新された書籍"))
            .andExpect(jsonPath("$.publicationStatus").value("PUBLISHED"))

        verify(bookService).update(1L, updateRequest)
    }

    /**
     * PATCH /api/books/{id}/publication-status - 出版状況更新のテスト
     */
    @Test
    fun `updatePublicationStatus should return updated book`() {
        // Given
        val updatedBook = createSampleBookResponse(publicationStatus = PublicationStatus.PUBLISHED)
        whenever(bookService.updatePublicationStatus(1L, PublicationStatus.PUBLISHED))
            .thenReturn(updatedBook)

        // When & Then
        mockMvc
            .perform(
                patch("/api/books/1/publication-status")
                    .param("status", "PUBLISHED"),
            ).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.publicationStatus").value("PUBLISHED"))

        verify(bookService).updatePublicationStatus(1L, PublicationStatus.PUBLISHED)
    }

    /**
     * POST /api/books/{bookId}/authors/{authorId} - 書籍への著者追加のテスト
     */
    @Test
    fun `addAuthorToBook should return updated book`() {
        // Given
        val updatedBook = createSampleBookResponse()
        whenever(bookService.addAuthor(1L, 2L)).thenReturn(updatedBook)

        // When & Then
        mockMvc
            .perform(post("/api/books/1/authors/2"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))

        verify(bookService).addAuthor(1L, 2L)
    }

    /**
     * DELETE /api/books/{bookId}/authors/{authorId} - 書籍からの著者削除のテスト
     */
    @Test
    fun `removeAuthorFromBook should return updated book`() {
        // Given
        val updatedBook = createSampleBookResponse()
        whenever(bookService.removeAuthor(1L, 2L)).thenReturn(updatedBook)

        // When & Then
        mockMvc
            .perform(delete("/api/books/1/authors/2"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))

        verify(bookService).removeAuthor(1L, 2L)
    }
}
