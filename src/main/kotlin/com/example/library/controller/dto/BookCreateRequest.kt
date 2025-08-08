package com.example.library.controller.dto

import com.example.library.domain.PublicationStatus
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

/**
 * 書籍新規登録リクエストDTO
 * 
 * バリデーションルール:
 * - 書籍タイトルは必須かつ空白不可
 * - 価格は0以上である必要がある
 * - 最低1人の著者IDが必要
 * - 出版状況は指定可能（未指定の場合は未出版）
 */
data class BookCreateRequest(
    
    /** 書籍タイトル */
    @field:NotBlank(message = "書籍タイトルは必須です")
    val title: String,
    
    /** 価格 */
    @field:NotNull(message = "価格は必須です")
    @field:DecimalMin(value = "0.0", message = "価格は0以上である必要があります")
    val price: BigDecimal,
    
    /** 出版状況（デフォルト: 未出版） */
    val publicationStatus: PublicationStatus = PublicationStatus.UNPUBLISHED,
    
    /** 著者IDリスト */
    @field:NotEmpty(message = "最低1人の著者が必要です")
    val authorIds: List<Long>
)