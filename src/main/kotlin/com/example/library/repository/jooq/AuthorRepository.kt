package com.example.library.repository.jooq

import com.example.library.domain.Author
import com.example.library.jooq.tables.references.AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * 著者データアクセスリポジトリ（jOOQベース）
 */
@Repository
class AuthorRepository(
    private val dsl: DSLContext
) {

    /**
     * 全著者を取得（ページネーション対応）
     */
    fun findAll(offset: Int, limit: Int): List<Author> {
        return dsl.select()
            .from(AUTHORS)
            .orderBy(AUTHORS.ID)
            .limit(limit)
            .offset(offset)
            .fetchInto(Author::class.java)
    }

    /**
     * 全著者数を取得
     */
    fun countAll(): Long {
        return dsl.selectCount()
            .from(AUTHORS)
            .fetchOne(0, Long::class.java) ?: 0
    }

    /**
     * IDで著者を検索
     */
    fun findById(id: Long): Author? {
        return dsl.select()
            .from(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .fetchOneInto(Author::class.java)
    }

    /**
     * 著者名で検索（部分一致、ページネーション対応）
     */
    fun findByNameContaining(name: String, offset: Int, limit: Int): List<Author> {
        return dsl.select()
            .from(AUTHORS)
            .where(AUTHORS.NAME.containsIgnoreCase(name))
            .orderBy(AUTHORS.NAME)
            .limit(limit)
            .offset(offset)
            .fetchInto(Author::class.java)
    }

    /**
     * 著者名での検索結果数を取得
     */
    fun countByNameContaining(name: String): Long {
        return dsl.selectCount()
            .from(AUTHORS)
            .where(AUTHORS.NAME.containsIgnoreCase(name))
            .fetchOne(0, Long::class.java) ?: 0
    }

    /**
     * 同名の著者が存在するかチェック
     */
    fun existsByName(name: String): Boolean {
        return (dsl.selectCount()
            .from(AUTHORS)
            .where(AUTHORS.NAME.eq(name))
            .fetchOne(0, Int::class.java) ?: 0) > 0
    }

    /**
     * 指定ID以外で同名の著者が存在するかチェック（更新時の重複チェック用）
     */
    fun existsByNameAndIdNot(name: String, id: Long): Boolean {
        return (dsl.selectCount()
            .from(AUTHORS)
            .where(AUTHORS.NAME.eq(name))
            .and(AUTHORS.ID.ne(id))
            .fetchOne(0, Int::class.java) ?: 0) > 0
    }

    /**
     * 新しい著者を保存
     */
    fun save(author: Author): Author {
        val now = LocalDateTime.now()

        val insertedRecord = dsl.insertInto(AUTHORS)
            .set(AUTHORS.NAME, author.name)
            .set(AUTHORS.BIRTH_DATE, author.birthDate)
            .set(AUTHORS.CREATED_AT, now)
            .set(AUTHORS.UPDATED_AT, now)
            .returning()
            .fetchOne()

        return insertedRecord?.let {
            Author(
                id = it[AUTHORS.ID],
                name = it[AUTHORS.NAME] ?: throw IllegalStateException("著者名の取得に失敗しました"),
                birthDate = it[AUTHORS.BIRTH_DATE] ?: throw IllegalStateException("生年月日の取得に失敗しました"),
                createdAt = it[AUTHORS.CREATED_AT] ?: throw IllegalStateException("作成日時の取得に失敗しました"),
                updatedAt = it[AUTHORS.UPDATED_AT] ?: throw IllegalStateException("更新日時の取得に失敗しました")
            )
        } ?: throw IllegalStateException("著者の保存に失敗しました")
    }

    /**
     * 既存の著者を更新
     */
    fun update(author: Author): Author {
        require(author.id != null) { "更新対象の著者IDが指定されていません" }

        val now = LocalDateTime.now()

        val updatedCount = dsl.update(AUTHORS)
            .set(AUTHORS.NAME, author.name)
            .set(AUTHORS.BIRTH_DATE, author.birthDate)
            .set(AUTHORS.UPDATED_AT, now)
            .where(AUTHORS.ID.eq(author.id))
            .execute()

        if (updatedCount == 0) {
            throw IllegalArgumentException("指定されたID=${author.id}の著者が見つかりません")
        }

        return author.copy(updatedAt = now)
    }


    /**
     * 著者が存在するかチェック
     */
    fun existsById(id: Long): Boolean {
        return (dsl.selectCount()
            .from(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .fetchOne(0, Int::class.java) ?: 0) > 0
    }
}
