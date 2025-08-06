package com.example.library.domain

import com.example.library.jooq.enums.PublicationStatus as JooqPublicationStatus

/**
 * 出版状況を表すドメイン列挙型
 * ビジネスルールと状態遷移ロジックを含む
 */
enum class PublicationStatus {
    /** 未出版 */
    UNPUBLISHED,
    
    /** 出版済み */
    PUBLISHED;
    
    /**
     * 指定された新しい状態に遷移可能かチェック
     * ビジネスルール: 出版済み → 未出版への変更は不可
     */
    fun canTransitionTo(newStatus: PublicationStatus): Boolean {
        return when (this) {
            UNPUBLISHED -> true  // 未出版からはどちらにも変更可能
            PUBLISHED -> newStatus == PUBLISHED  // 出版済みからは出版済みのみ
        }
    }
    
    /**
     * jOOQ enum型への変換
     */
    fun toJooqEnum(): JooqPublicationStatus {
        return when (this) {
            UNPUBLISHED -> JooqPublicationStatus.UNPUBLISHED
            PUBLISHED -> JooqPublicationStatus.PUBLISHED
        }
    }
    
    companion object {
        /**
         * jOOQ enum型からドメイン型への変換
         */
        fun fromJooqEnum(jooqStatus: JooqPublicationStatus): PublicationStatus {
            return when (jooqStatus) {
                JooqPublicationStatus.UNPUBLISHED -> UNPUBLISHED
                JooqPublicationStatus.PUBLISHED -> PUBLISHED
            }
        }
    }
}