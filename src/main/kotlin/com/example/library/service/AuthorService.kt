package com.example.library.service

import com.example.library.controller.dto.AuthorCreateRequest
import com.example.library.controller.dto.AuthorResponse
import com.example.library.controller.dto.AuthorUpdateRequest
import com.example.library.controller.dto.PagedResponse
import com.example.library.domain.Author
import com.example.library.exception.ResourceNotFoundException
import com.example.library.exception.DuplicateResourceException
import com.example.library.repository.jooq.AuthorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 著者管理ビジネスロジックサービス
 */
@Service
@Transactional(readOnly = true)
class AuthorService(
    private val authorRepository: AuthorRepository
) {
    
    /**
     * 著者一覧を取得（ページネーション対応）
     */
    fun findAll(pageNumber: Int = 0, pageSize: Int = 20): PagedResponse<AuthorResponse> {
        require(pageNumber >= 0) { "ページ番号は0以上である必要があります" }
        require(pageSize > 0) { "ページサイズは1以上である必要があります" }
        
        val offset = pageNumber * pageSize
        val authors = authorRepository.findAll(offset, pageSize)
        val totalElements = authorRepository.countAll()
        
        val authorResponses = authors.map { AuthorResponse.from(it) }
        
        return PagedResponse.of(authorResponses, pageNumber, pageSize, totalElements)
    }
    
    /**
     * 著者名で検索（ページネーション対応）
     */
    fun searchByName(name: String, pageNumber: Int = 0, pageSize: Int = 20): PagedResponse<AuthorResponse> {
        require(name.isNotBlank()) { "検索する著者名は必須です" }
        require(pageNumber >= 0) { "ページ番号は0以上である必要があります" }
        require(pageSize > 0) { "ページサイズは1以上である必要があります" }
        
        val offset = pageNumber * pageSize
        val authors = authorRepository.findByNameContaining(name, offset, pageSize)
        val totalElements = authorRepository.countByNameContaining(name)
        
        val authorResponses = authors.map { AuthorResponse.from(it) }
        
        return PagedResponse.of(authorResponses, pageNumber, pageSize, totalElements)
    }
    
    /**
     * IDで著者を取得
     */
    fun findById(id: Long): AuthorResponse {
        val author = authorRepository.findById(id)
            ?: throw ResourceNotFoundException("指定されたID=$id の著者が見つかりません")
        
        return AuthorResponse.from(author)
    }
    
    /**
     * 新しい著者を登録
     */
    @Transactional
    fun create(request: AuthorCreateRequest): AuthorResponse {
        // 同名の著者が既に存在するかチェック
        if (authorRepository.existsByName(request.name)) {
            throw DuplicateResourceException("著者名「${request.name}」は既に登録されています")
        }
        
        // ドメインオブジェクトを作成（バリデーションが実行される）
        val author = Author(
            name = request.name,
            birthDate = request.birthDate
        )
        
        val savedAuthor = authorRepository.save(author)
        return AuthorResponse.from(savedAuthor)
    }
    
    /**
     * 著者情報を更新
     */
    @Transactional
    fun update(id: Long, request: AuthorUpdateRequest): AuthorResponse {
        // 著者の存在確認
        val existingAuthor = authorRepository.findById(id)
            ?: throw ResourceNotFoundException("指定されたID=$id の著者が見つかりません")
        
        // 同名の他の著者が既に存在するかチェック
        if (authorRepository.existsByNameAndIdNot(request.name, id)) {
            throw DuplicateResourceException("著者名「${request.name}」は既に他の著者により登録されています")
        }
        
        // ドメインオブジェクトで更新（バリデーションが実行される）
        val updatedAuthor = existingAuthor.update(request.name, request.birthDate)
        
        val savedAuthor = authorRepository.update(updatedAuthor)
        return AuthorResponse.from(savedAuthor)
    }
    
    /**
     * 著者を削除
     */
    @Transactional
    fun deleteById(id: Long) {
        // 著者の存在確認
        if (!authorRepository.existsById(id)) {
            throw ResourceNotFoundException("指定されたID=$id の著者が見つかりません")
        }
        
        // TODO: 書籍との関連チェック（将来的に書籍管理機能実装時に追加予定）
        // 現在は著者テーブルから直接削除
        
        val deleted = authorRepository.deleteById(id)
        if (!deleted) {
            throw IllegalStateException("著者の削除に失敗しました。ID=$id")
        }
    }
    
    /**
     * 著者が存在するかチェック
     */
    fun existsById(id: Long): Boolean {
        return authorRepository.existsById(id)
    }
}