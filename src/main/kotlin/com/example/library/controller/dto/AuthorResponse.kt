package com.example.library.controller.dto

import com.example.library.domain.Author
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 著者情報レスポンスDTO
 */
data class AuthorResponse(
    /** 著者ID */
    val id: Long,
    /** 著者名 */
    val name: String,
    /** 生年月日 */
    val birthDate: LocalDate,
    /** 作成日時 */
    val createdAt: LocalDateTime,
    /** 更新日時 */
    val updatedAt: LocalDateTime,
) {
    companion object {
        /**
         * ドメインオブジェクトからレスポンスDTOに変換
         */
        fun from(author: Author): AuthorResponse =
            AuthorResponse(
                id = author.id ?: throw IllegalStateException("著者IDが設定されていません"),
                name = author.name,
                birthDate = author.birthDate,
                createdAt = author.createdAt,
                updatedAt = author.updatedAt,
            )
    }
}
