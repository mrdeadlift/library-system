package com.example.library.controller

import com.example.library.controller.dto.AuthorCreateRequest
import com.example.library.controller.dto.AuthorResponse
import com.example.library.controller.dto.AuthorUpdateRequest
import com.example.library.controller.dto.PagedResponse
import com.example.library.service.AuthorService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 著者管理REST APIコントローラー
 */
@RestController
@RequestMapping("/api/authors")
class AuthorController(
    private val authorService: AuthorService,
) {
    /**
     * 著者一覧を取得（ページネーション対応）
     *
     * @param page ページ番号 (0ベース、デフォルト: 0)
     * @param size ページサイズ (デフォルト: 20)
     * @param name 著者名での検索 (任意)
     */
    @GetMapping
    fun getAuthors(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) name: String?,
    ): ResponseEntity<PagedResponse<AuthorResponse>> {
        val result =
            if (name.isNullOrBlank()) {
                authorService.findAll(page, size)
            } else {
                authorService.searchByName(name, page, size)
            }

        return ResponseEntity.ok(result)
    }

    /**
     * IDで著者詳細を取得
     *
     * @param id 著者ID
     */
    @GetMapping("/{id}")
    fun getAuthor(
        @PathVariable id: Long,
    ): ResponseEntity<AuthorResponse> {
        val author = authorService.findById(id)
        return ResponseEntity.ok(author)
    }

    /**
     * 新しい著者を登録
     *
     * @param request 著者登録リクエスト
     */
    @PostMapping
    fun createAuthor(
        @Valid @RequestBody request: AuthorCreateRequest,
    ): ResponseEntity<AuthorResponse> {
        val createdAuthor = authorService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAuthor)
    }

    /**
     * 既存の著者情報を更新
     *
     * @param id 著者ID
     * @param request 著者更新リクエスト
     */
    @PutMapping("/{id}")
    fun updateAuthor(
        @PathVariable id: Long,
        @Valid @RequestBody request: AuthorUpdateRequest,
    ): ResponseEntity<AuthorResponse> {
        val updatedAuthor = authorService.update(id, request)
        return ResponseEntity.ok(updatedAuthor)
    }

    /**
     * 著者を削除
     *
     * @param id 著者ID
     */
    @DeleteMapping("/{id}")
    fun deleteAuthor(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        authorService.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    /**
     * 著者の存在チェック
     *
     * @param id 著者ID
     */
    @GetMapping("/{id}/exists")
    fun checkAuthorExists(
        @PathVariable id: Long,
    ): ResponseEntity<Map<String, Boolean>> {
        val exists = authorService.existsById(id)
        return ResponseEntity.ok(mapOf("exists" to exists))
    }
}
