package com.example.library.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import java.time.LocalDate

/**
 * 著者情報更新リクエストDTO
 *
 * バリデーションルール:
 * - 著者名は必須かつ空白不可
 * - 生年月日は過去の日付である必要がある
 */
data class AuthorUpdateRequest(
    /** 著者名 */
    @field:NotBlank(message = "著者名は必須です")
    val name: String,
    /** 生年月日 */
    @field:Past(message = "生年月日は過去の日付である必要があります")
    val birthDate: LocalDate,
)
