package com.example.library.domain

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 著者を表すドメインオブジェクト
 * 
 * ビジネスルール:
 * - 著者名は必須かつ空白不可
 * - 生年月日は現在の日付よりも過去である必要がある
 */
data class Author(
    /** 著者ID (新規作成時はnull) */
    val id: Long? = null,
    
    /** 著者名 */
    val name: String,
    
    /** 生年月日 */
    val birthDate: LocalDate,
    
    /** 作成日時 */
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    /** 更新日時 */
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    init {
        // ビジネスルール検証
        require(name.isNotBlank()) { "著者名は必須です" }
        require(birthDate.isBefore(LocalDate.now())) { 
            "生年月日は過去の日付である必要があります。指定された日付: $birthDate" 
        }
    }
    
    /**
     * 著者情報を更新する
     */
    fun update(name: String, birthDate: LocalDate): Author {
        return copy(
            name = name,
            birthDate = birthDate,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * 現在の年齢を計算する
     */
    fun getAge(): Int {
        return LocalDate.now().year - birthDate.year - 
            if (LocalDate.now().dayOfYear < birthDate.dayOfYear) 1 else 0
    }
    
    /**
     * 著者名の表示用フォーマット
     */
    fun getDisplayName(): String = name.trim()
}