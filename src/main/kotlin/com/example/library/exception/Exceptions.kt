package com.example.library.exception

/**
 * ビジネスルール違反例外の基底クラス
 */
open class BusinessRuleViolationException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * リソースが見つからない場合の例外
 */
class ResourceNotFoundException(
    message: String,
) : RuntimeException(message)

/**
 * 出版状況の不正な遷移を試行した場合の例外
 */
class InvalidPublicationStatusTransitionException(
    currentStatus: String,
    requestedStatus: String,
) : BusinessRuleViolationException(
        "出版状況を${currentStatus}から${requestedStatus}に変更することはできません。" +
            "出版済みの書籍を未出版に戻すことはできません。",
    )

/**
 * 書籍の著者制約違反例外
 */
class BookAuthorConstraintViolationException(
    message: String,
) : BusinessRuleViolationException(message)

/**
 * 重複するデータの登録を試行した場合の例外
 */
class DuplicateResourceException(
    message: String,
) : BusinessRuleViolationException(message)

/**
 * バリデーション違反例外
 */
class ValidationException(
    message: String,
    val fieldErrors: Map<String, String> = emptyMap(),
) : RuntimeException(message)
