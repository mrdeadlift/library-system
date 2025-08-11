package com.example.library.controller.dto

/**
 * ページネーション対応レスポンス用汎用DTO
 */
data class PagedResponse<T>(
    /** データ配列 */
    val content: List<T>,
    /** 現在ページ番号 (0ベース) */
    val pageNumber: Int,
    /** ページサイズ */
    val pageSize: Int,
    /** 全要素数 */
    val totalElements: Long,
    /** 全ページ数 */
    val totalPages: Int,
    /** 最初のページかどうか */
    val isFirst: Boolean,
    /** 最後のページかどうか */
    val isLast: Boolean,
    /** 空のページかどうか */
    val isEmpty: Boolean,
) {
    companion object {
        /**
         * ページネーション情報付きレスポンスを作成
         */
        fun <T> of(
            content: List<T>,
            pageNumber: Int,
            pageSize: Int,
            totalElements: Long,
        ): PagedResponse<T> {
            val totalPages = if (pageSize == 0) 0 else ((totalElements - 1) / pageSize).toInt() + 1

            return PagedResponse(
                content = content,
                pageNumber = pageNumber,
                pageSize = pageSize,
                totalElements = totalElements,
                totalPages = totalPages,
                isFirst = pageNumber == 0,
                isLast = pageNumber >= totalPages - 1,
                isEmpty = content.isEmpty(),
            )
        }
    }
}
