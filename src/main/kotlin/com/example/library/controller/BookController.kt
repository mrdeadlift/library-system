package com.example.library.controller

import com.example.library.controller.dto.BookCreateRequest
import com.example.library.controller.dto.BookResponse
import com.example.library.controller.dto.BookUpdateRequest
import com.example.library.controller.dto.PagedResponse
import com.example.library.domain.PublicationStatus
import com.example.library.service.BookService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 書籍管理REST APIコントローラー
 */
@RestController
@RequestMapping("/api/books")
class BookController(
    private val bookService: BookService,
) {
    /**
     * 書籍一覧を取得（ページネーション対応）
     *
     * @param page ページ番号 (0ベース、デフォルト: 0)
     * @param size ページサイズ (デフォルト: 20)
     * @param title 書籍タイトルでの検索 (任意)
     * @param status 出版状況での検索 (任意)
     * @param authorId 著者IDでの検索 (任意)
     */
    @GetMapping
    fun getBooks(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) status: PublicationStatus?,
        @RequestParam(required = false) authorId: Long?,
    ): ResponseEntity<PagedResponse<BookResponse>> {
        val result =
            when {
                !title.isNullOrBlank() -> bookService.searchByTitle(title, page, size)
                status != null -> bookService.findByPublicationStatus(status, page, size)
                authorId != null -> bookService.findByAuthorId(authorId, page, size)
                else -> bookService.findAll(page, size)
            }

        return ResponseEntity.ok(result)
    }

    /**
     * IDで書籍詳細を取得
     *
     * @param id 書籍ID
     */
    @GetMapping("/{id}")
    fun getBook(
        @PathVariable id: Long,
    ): ResponseEntity<BookResponse> {
        val book = bookService.findById(id)
        return ResponseEntity.ok(book)
    }

    /**
     * 新しい書籍を登録
     *
     * @param request 書籍登録リクエスト
     */
    @PostMapping
    fun createBook(
        @Valid @RequestBody request: BookCreateRequest,
    ): ResponseEntity<BookResponse> {
        val createdBook = bookService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook)
    }

    /**
     * 既存の書籍情報を更新
     *
     * @param id 書籍ID
     * @param request 書籍更新リクエスト
     */
    @PutMapping("/{id}")
    fun updateBook(
        @PathVariable id: Long,
        @Valid @RequestBody request: BookUpdateRequest,
    ): ResponseEntity<BookResponse> {
        val updatedBook = bookService.update(id, request)
        return ResponseEntity.ok(updatedBook)
    }

    /**
     * 書籍の出版状況を更新
     *
     * @param id 書籍ID
     * @param status 新しい出版状況
     */
    @PatchMapping("/{id}/publication-status")
    fun updatePublicationStatus(
        @PathVariable id: Long,
        @RequestParam status: PublicationStatus,
    ): ResponseEntity<BookResponse> {
        val updatedBook = bookService.updatePublicationStatus(id, status)
        return ResponseEntity.ok(updatedBook)
    }

    /**
     * 書籍に著者を追加
     *
     * @param bookId 書籍ID
     * @param authorId 追加する著者ID
     */
    @PostMapping("/{bookId}/authors/{authorId}")
    fun addAuthorToBook(
        @PathVariable bookId: Long,
        @PathVariable authorId: Long,
    ): ResponseEntity<BookResponse> {
        val updatedBook = bookService.addAuthor(bookId, authorId)
        return ResponseEntity.ok(updatedBook)
    }

    /**
     * 書籍から著者を削除
     *
     * @param bookId 書籍ID
     * @param authorId 削除する著者ID
     */
    @DeleteMapping("/{bookId}/authors/{authorId}")
    fun removeAuthorFromBook(
        @PathVariable bookId: Long,
        @PathVariable authorId: Long,
    ): ResponseEntity<BookResponse> {
        val updatedBook = bookService.removeAuthor(bookId, authorId)
        return ResponseEntity.ok(updatedBook)
    }
}
