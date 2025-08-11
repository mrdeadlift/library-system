package com.example.library.domain

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 書籍を表すドメインオブジェクト
 *
 * ビジネスルール:
 * - 書籍タイトルは必須かつ空白不可
 * - 価格は0以上である必要がある
 * - 最低1人の著者を持つ必要がある
 * - 出版済み → 未出版への変更は不可
 */
data class Book(
    /** 書籍ID (新規作成時はnull) */
    val id: Long? = null,
    /** 書籍タイトル */
    val title: String,
    /** 価格 */
    val price: BigDecimal,
    /** 出版状況 */
    val publicationStatus: PublicationStatus = PublicationStatus.UNPUBLISHED,
    /** 著者リスト */
    val authors: List<Author> = emptyList(),
    /** 作成日時 */
    val createdAt: LocalDateTime = LocalDateTime.now(),
    /** 更新日時 */
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    init {
        // ビジネスルール検証
        require(title.isNotBlank()) { "書籍タイトルは必須です" }
        require(price >= BigDecimal.ZERO) {
            "価格は0以上である必要があります。指定された価格: $price"
        }
        require(authors.isNotEmpty()) { "最低1人の著者が必要です" }
    }

    /**
     * 書籍情報を更新する
     */
    fun update(
        title: String,
        price: BigDecimal,
        authors: List<Author>,
    ): Book {
        return copy(
            title = title,
            price = price,
            authors = authors,
            updatedAt = LocalDateTime.now(),
        )
    }

    /**
     * 出版状況を変更する
     * ビジネスルール: 出版済み → 未出版への変更は不可
     */
    fun updatePublicationStatus(newStatus: PublicationStatus): Book {
        if (!publicationStatus.canTransitionTo(newStatus)) {
            throw IllegalStateException(
                "${publicationStatus}から${newStatus}への変更はできません。" +
                    "出版済みの書籍を未出版に戻すことはできません。",
            )
        }
        return copy(
            publicationStatus = newStatus,
            updatedAt = LocalDateTime.now(),
        )
    }

    /**
     * 著者を追加する
     */
    fun addAuthor(author: Author): Book {
        if (authors.any { it.id == author.id }) {
            throw IllegalArgumentException("著者「${author.name}」は既に追加されています")
        }
        return copy(
            authors = authors + author,
            updatedAt = LocalDateTime.now(),
        )
    }

    /**
     * 著者を削除する
     */
    fun removeAuthor(authorId: Long): Book {
        if (authors.size <= 1) {
            throw IllegalStateException("書籍には最低1人の著者が必要です")
        }

        val updatedAuthors = authors.filterNot { it.id == authorId }
        if (updatedAuthors.size == authors.size) {
            throw IllegalArgumentException("指定された著者ID($authorId)は見つかりません")
        }

        return copy(
            authors = updatedAuthors,
            updatedAt = LocalDateTime.now(),
        )
    }

    /**
     * 出版済みかどうかを判定
     */
    fun isPublished(): Boolean = publicationStatus == PublicationStatus.PUBLISHED

    /**
     * 書籍の表示用タイトル
     */
    fun getDisplayTitle(): String = title.trim()

    /**
     * 著者名を結合した文字列を取得
     */
    fun getAuthorNames(): String = authors.joinToString(", ") { it.getDisplayName() }

    /**
     * 価格の表示用フォーマット
     */
    fun getFormattedPrice(): String = "¥${price.toPlainString()}"
}
