package com.example.library.controller.dto

import com.example.library.domain.Book
import com.example.library.domain.PublicationStatus
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 書籍情報レスポンスDTO
 */
data class BookResponse(
    /** 書籍ID */
    val id: Long,
    /** 書籍タイトル */
    val title: String,
    /** 価格 */
    val price: BigDecimal,
    /** フォーマット済み価格 */
    val formattedPrice: String,
    /** 出版状況 */
    val publicationStatus: PublicationStatus,
    /** 出版済みかどうか */
    val isPublished: Boolean,
    /** 著者リスト */
    val authors: List<AuthorResponse>,
    /** 著者名（カンマ区切り） */
    val authorNames: String,
    /** 作成日時 */
    val createdAt: LocalDateTime,
    /** 更新日時 */
    val updatedAt: LocalDateTime,
) {
    companion object {
        /**
         * ドメインオブジェクトからレスポンスDTOに変換
         */
        fun from(book: Book): BookResponse {
            return BookResponse(
                id = book.id ?: throw IllegalStateException("書籍IDが設定されていません"),
                title = book.title,
                price = book.price,
                formattedPrice = book.getFormattedPrice(),
                publicationStatus = book.publicationStatus,
                isPublished = book.isPublished(),
                authors = book.authors.map { AuthorResponse.from(it) },
                authorNames = book.getAuthorNames(),
                createdAt = book.createdAt,
                updatedAt = book.updatedAt,
            )
        }
    }
}
