package com.example.library.service

import com.example.library.controller.dto.BookCreateRequest
import com.example.library.controller.dto.BookResponse
import com.example.library.controller.dto.BookUpdateRequest
import com.example.library.controller.dto.PagedResponse
import com.example.library.domain.Book
import com.example.library.domain.PublicationStatus
import com.example.library.exception.DuplicateResourceException
import com.example.library.exception.ResourceNotFoundException
import com.example.library.repository.jooq.AuthorRepository
import com.example.library.repository.jooq.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 書籍管理ビジネスロジックサービス
 */
@Service
@Transactional(readOnly = true)
class BookService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
) {
    /**
     * 書籍一覧を取得（ページネーション対応）
     */
    fun findAll(
        pageNumber: Int = 0,
        pageSize: Int = 20,
    ): PagedResponse<BookResponse> {
        require(pageNumber >= 0) { "ページ番号は0以上である必要があります" }
        require(pageSize > 0) { "ページサイズは1以上である必要があります" }

        val offset = pageNumber * pageSize
        val books = bookRepository.findAll(offset, pageSize)
        val totalElements = bookRepository.countAll()

        val bookResponses = books.map { BookResponse.from(it) }

        return PagedResponse.of(bookResponses, pageNumber, pageSize, totalElements)
    }

    /**
     * 書籍タイトルで検索（ページネーション対応）
     */
    fun searchByTitle(
        title: String,
        pageNumber: Int = 0,
        pageSize: Int = 20,
    ): PagedResponse<BookResponse> {
        require(title.isNotBlank()) { "検索する書籍タイトルは必須です" }
        require(pageNumber >= 0) { "ページ番号は0以上である必要があります" }
        require(pageSize > 0) { "ページサイズは1以上である必要があります" }

        val offset = pageNumber * pageSize
        val books = bookRepository.findByTitleContaining(title, offset, pageSize)
        val totalElements = bookRepository.countByTitleContaining(title)

        val bookResponses = books.map { BookResponse.from(it) }

        return PagedResponse.of(bookResponses, pageNumber, pageSize, totalElements)
    }

    /**
     * 出版状況で書籍を検索（ページネーション対応）
     */
    fun findByPublicationStatus(
        status: PublicationStatus,
        pageNumber: Int = 0,
        pageSize: Int = 20,
    ): PagedResponse<BookResponse> {
        require(pageNumber >= 0) { "ページ番号は0以上である必要があります" }
        require(pageSize > 0) { "ページサイズは1以上である必要があります" }

        val offset = pageNumber * pageSize
        val books = bookRepository.findByPublicationStatus(status, offset, pageSize)
        val totalElements = bookRepository.countByPublicationStatus(status)

        val bookResponses = books.map { BookResponse.from(it) }

        return PagedResponse.of(bookResponses, pageNumber, pageSize, totalElements)
    }

    /**
     * 著者IDで書籍を検索（ページネーション対応）
     */
    fun findByAuthorId(
        authorId: Long,
        pageNumber: Int = 0,
        pageSize: Int = 20,
    ): PagedResponse<BookResponse> {
        require(pageNumber >= 0) { "ページ番号は0以上である必要があります" }
        require(pageSize > 0) { "ページサイズは1以上である必要があります" }

        // 著者の存在確認
        if (!authorRepository.existsById(authorId)) {
            throw ResourceNotFoundException("指定されたID=$authorId の著者が見つかりません")
        }

        val offset = pageNumber * pageSize
        val books = bookRepository.findByAuthorId(authorId, offset, pageSize)
        val totalElements = bookRepository.countByAuthorId(authorId)

        val bookResponses = books.map { BookResponse.from(it) }

        return PagedResponse.of(bookResponses, pageNumber, pageSize, totalElements)
    }

    /**
     * IDで書籍を取得
     */
    fun findById(id: Long): BookResponse {
        val book =
            bookRepository.findById(id)
                ?: throw ResourceNotFoundException("指定されたID=$id の書籍が見つかりません")

        return BookResponse.from(book)
    }

    /**
     * 新しい書籍を登録
     */
    @Transactional
    fun create(request: BookCreateRequest): BookResponse {
        // 同タイトルの書籍が既に存在するかチェック
        if (bookRepository.existsByTitle(request.title)) {
            throw DuplicateResourceException("書籍タイトル「${request.title}」は既に登録されています")
        }

        // 著者の存在確認
        val authors =
            request.authorIds.map { authorId ->
                authorRepository.findById(authorId)
                    ?: throw ResourceNotFoundException("指定されたID=$authorId の著者が見つかりません")
            }

        // ドメインオブジェクトを作成（バリデーションが実行される）
        val book =
            Book(
                title = request.title,
                price = request.price,
                publicationStatus = request.publicationStatus,
                authors = authors,
            )

        val savedBook = bookRepository.save(book)
        return BookResponse.from(savedBook)
    }

    /**
     * 書籍情報を更新
     */
    @Transactional
    fun update(
        id: Long,
        request: BookUpdateRequest,
    ): BookResponse {
        // 書籍の存在確認
        val existingBook =
            bookRepository.findById(id)
                ?: throw ResourceNotFoundException("指定されたID=$id の書籍が見つかりません")

        // 同タイトルの他の書籍が既に存在するかチェック
        if (bookRepository.existsByTitleAndIdNot(request.title, id)) {
            throw DuplicateResourceException("書籍タイトル「${request.title}」は既に他の書籍により登録されています")
        }

        // 著者の存在確認
        val authors =
            request.authorIds.map { authorId ->
                authorRepository.findById(authorId)
                    ?: throw ResourceNotFoundException("指定されたID=$authorId の著者が見つかりません")
            }

        // ドメインオブジェクトで更新（バリデーションが実行される）
        val updatedBook =
            try {
                existingBook
                    .update(request.title, request.price, authors)
                    .updatePublicationStatus(request.publicationStatus)
            } catch (e: IllegalStateException) {
                throw IllegalArgumentException("出版状況の更新に失敗しました: ${e.message}", e)
            }

        val savedBook = bookRepository.update(updatedBook)
        return BookResponse.from(savedBook)
    }

    /**
     * 書籍の出版状況を更新
     */
    @Transactional
    fun updatePublicationStatus(
        id: Long,
        newStatus: PublicationStatus,
    ): BookResponse {
        // 書籍の存在確認
        val existingBook =
            bookRepository.findById(id)
                ?: throw ResourceNotFoundException("指定されたID=$id の書籍が見つかりません")

        // ドメインオブジェクトで状況更新（ビジネスルール適用）
        val updatedBook =
            try {
                existingBook.updatePublicationStatus(newStatus)
            } catch (e: IllegalStateException) {
                throw IllegalArgumentException("出版状況の更新に失敗しました: ${e.message}", e)
            }

        val savedBook = bookRepository.update(updatedBook)
        return BookResponse.from(savedBook)
    }

    /**
     * 書籍に著者を追加
     */
    @Transactional
    fun addAuthor(
        bookId: Long,
        authorId: Long,
    ): BookResponse {
        // 書籍の存在確認
        val existingBook =
            bookRepository.findById(bookId)
                ?: throw ResourceNotFoundException("指定されたID=$bookId の書籍が見つかりません")

        // 著者の存在確認
        val author =
            authorRepository.findById(authorId)
                ?: throw ResourceNotFoundException("指定されたID=$authorId の著者が見つかりません")

        // ドメインオブジェクトで著者追加（重複チェック含む）
        val updatedBook =
            try {
                existingBook.addAuthor(author)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("著者の追加に失敗しました: ${e.message}", e)
            }

        val savedBook = bookRepository.update(updatedBook)
        return BookResponse.from(savedBook)
    }

    /**
     * 書籍から著者を削除
     */
    @Transactional
    fun removeAuthor(
        bookId: Long,
        authorId: Long,
    ): BookResponse {
        // 書籍の存在確認
        val existingBook =
            bookRepository.findById(bookId)
                ?: throw ResourceNotFoundException("指定されたID=$bookId の書籍が見つかりません")

        // ドメインオブジェクトで著者削除（最低1人制約チェック含む）
        val updatedBook =
            try {
                existingBook.removeAuthor(authorId)
            } catch (e: IllegalStateException) {
                throw IllegalArgumentException("著者の削除に失敗しました: ${e.message}", e)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("著者の削除に失敗しました: ${e.message}", e)
            }

        val savedBook = bookRepository.update(updatedBook)
        return BookResponse.from(savedBook)
    }

    /**
     * 書籍を削除
     */
    @Transactional
    fun deleteById(id: Long) {
        // 書籍の存在確認
        if (!bookRepository.existsById(id)) {
            throw ResourceNotFoundException("指定されたID=$id の書籍が見つかりません")
        }

        val deleted = bookRepository.deleteById(id)
        if (!deleted) {
            throw IllegalStateException("書籍の削除に失敗しました。ID=$id")
        }
    }

    /**
     * 書籍が存在するかチェック
     */
    fun existsById(id: Long): Boolean = bookRepository.existsById(id)

    /**
     * 出版済み書籍一覧を取得（ページネーション対応）
     */
    fun findPublishedBooks(
        pageNumber: Int = 0,
        pageSize: Int = 20,
    ): PagedResponse<BookResponse> = findByPublicationStatus(PublicationStatus.PUBLISHED, pageNumber, pageSize)

    /**
     * 未出版書籍一覧を取得（ページネーション対応）
     */
    fun findUnpublishedBooks(
        pageNumber: Int = 0,
        pageSize: Int = 20,
    ): PagedResponse<BookResponse> = findByPublicationStatus(PublicationStatus.UNPUBLISHED, pageNumber, pageSize)
}
