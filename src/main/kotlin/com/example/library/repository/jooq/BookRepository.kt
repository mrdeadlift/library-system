package com.example.library.repository.jooq

import com.example.library.domain.Author
import com.example.library.domain.Book
import com.example.library.domain.PublicationStatus
import com.example.library.jooq.tables.references.AUTHORS
import com.example.library.jooq.tables.references.BOOKS
import com.example.library.jooq.tables.references.BOOK_AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * 書籍データアクセスリポジトリ（jOOQベース）
 */
@Repository
class BookRepository(
    private val dsl: DSLContext
) {

    /**
     * 全書籍を取得（ページネーション対応、著者情報含む）
     */
    fun findAll(offset: Int, limit: Int): List<Book> {
        val bookRecords = dsl.select()
            .from(BOOKS)
            .orderBy(BOOKS.ID)
            .limit(limit)
            .offset(offset)
            .fetch()

        // 各書籍の著者情報を取得してBookオブジェクトを作成
        return bookRecords.map { record ->
            val bookId = record[BOOKS.ID]!!
            val authors = findAuthorsByBookId(bookId)

            Book(
                id = bookId,
                title = record[BOOKS.TITLE] ?: throw IllegalStateException("書籍タイトルの取得に失敗しました"),
                price = record[BOOKS.PRICE] ?: throw IllegalStateException("価格の取得に失敗しました"),
                publicationStatus = record[BOOKS.PUBLICATION_STATUS]?.let {
                    PublicationStatus.fromJooqEnum(it)
                } ?: PublicationStatus.UNPUBLISHED,
                authors = authors,
                createdAt = record[BOOKS.CREATED_AT] ?: throw IllegalStateException("作成日時の取得に失敗しました"),
                updatedAt = record[BOOKS.UPDATED_AT] ?: throw IllegalStateException("更新日時の取得に失敗しました")
            )
        }
    }

    /**
     * 全書籍数を取得
     */
    fun countAll(): Long {
        return dsl.selectCount()
            .from(BOOKS)
            .fetchOne(0, Long::class.java) ?: 0
    }

    /**
     * IDで書籍を検索（著者情報含む）
     */
    fun findById(id: Long): Book? {
        val record = dsl.select()
            .from(BOOKS)
            .where(BOOKS.ID.eq(id))
            .fetchOne() ?: return null

        val authors = findAuthorsByBookId(id)

        return Book(
            id = record[BOOKS.ID],
            title = record[BOOKS.TITLE] ?: throw IllegalStateException("書籍タイトルの取得に失敗しました"),
            price = record[BOOKS.PRICE] ?: throw IllegalStateException("価格の取得に失敗しました"),
            publicationStatus = record[BOOKS.PUBLICATION_STATUS]?.let {
                PublicationStatus.fromJooqEnum(it)
            } ?: PublicationStatus.UNPUBLISHED,
            authors = authors,
            createdAt = record[BOOKS.CREATED_AT] ?: throw IllegalStateException("作成日時の取得に失敗しました"),
            updatedAt = record[BOOKS.UPDATED_AT] ?: throw IllegalStateException("更新日時の取得に失敗しました")
        )
    }

    /**
     * 書籍タイトルで検索（部分一致、ページネーション対応、著者情報含む）
     */
    fun findByTitleContaining(title: String, offset: Int, limit: Int): List<Book> {
        val bookRecords = dsl.select()
            .from(BOOKS)
            .where(BOOKS.TITLE.containsIgnoreCase(title))
            .orderBy(BOOKS.TITLE)
            .limit(limit)
            .offset(offset)
            .fetch()

        // 各書籍の著者情報を取得してBookオブジェクトを作成
        return bookRecords.map { record ->
            val bookId = record[BOOKS.ID]!!
            val authors = findAuthorsByBookId(bookId)

            Book(
                id = bookId,
                title = record[BOOKS.TITLE] ?: throw IllegalStateException("書籍タイトルの取得に失敗しました"),
                price = record[BOOKS.PRICE] ?: throw IllegalStateException("価格の取得に失敗しました"),
                publicationStatus = record[BOOKS.PUBLICATION_STATUS]?.let {
                    PublicationStatus.fromJooqEnum(it)
                } ?: PublicationStatus.UNPUBLISHED,
                authors = authors,
                createdAt = record[BOOKS.CREATED_AT] ?: throw IllegalStateException("作成日時の取得に失敗しました"),
                updatedAt = record[BOOKS.UPDATED_AT] ?: throw IllegalStateException("更新日時の取得に失敗しました")
            )
        }
    }

    /**
     * 書籍タイトルでの検索結果数を取得
     */
    fun countByTitleContaining(title: String): Long {
        return dsl.selectCount()
            .from(BOOKS)
            .where(BOOKS.TITLE.containsIgnoreCase(title))
            .fetchOne(0, Long::class.java) ?: 0
    }

    /**
     * 出版状況で書籍を検索（ページネーション対応、著者情報含む）
     */
    fun findByPublicationStatus(status: PublicationStatus, offset: Int, limit: Int): List<Book> {
        val bookRecords = dsl.select()
            .from(BOOKS)
            .where(BOOKS.PUBLICATION_STATUS.eq(status.toJooqEnum()))
            .orderBy(BOOKS.TITLE)
            .limit(limit)
            .offset(offset)
            .fetch()

        // 各書籍の著者情報を取得してBookオブジェクトを作成
        return bookRecords.map { record ->
            val bookId = record[BOOKS.ID]!!
            val authors = findAuthorsByBookId(bookId)

            Book(
                id = bookId,
                title = record[BOOKS.TITLE] ?: throw IllegalStateException("書籍タイトルの取得に失敗しました"),
                price = record[BOOKS.PRICE] ?: throw IllegalStateException("価格の取得に失敗しました"),
                publicationStatus = record[BOOKS.PUBLICATION_STATUS]?.let {
                    PublicationStatus.fromJooqEnum(it)
                } ?: PublicationStatus.UNPUBLISHED,
                authors = authors,
                createdAt = record[BOOKS.CREATED_AT] ?: throw IllegalStateException("作成日時の取得に失敗しました"),
                updatedAt = record[BOOKS.UPDATED_AT] ?: throw IllegalStateException("更新日時の取得に失敗しました")
            )
        }
    }

    /**
     * 出版状況での検索結果数を取得
     */
    fun countByPublicationStatus(status: PublicationStatus): Long {
        return dsl.selectCount()
            .from(BOOKS)
            .where(BOOKS.PUBLICATION_STATUS.eq(status.toJooqEnum()))
            .fetchOne(0, Long::class.java) ?: 0
    }

    /**
     * 著者IDで書籍を検索（ページネーション対応、著者情報含む）
     */
    fun findByAuthorId(authorId: Long, offset: Int, limit: Int): List<Book> {
        val bookRecords = dsl.select(BOOKS.fields().toList())
            .from(BOOKS)
            .join(BOOK_AUTHORS).on(BOOKS.ID.eq(BOOK_AUTHORS.BOOK_ID))
            .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
            .orderBy(BOOKS.TITLE)
            .limit(limit)
            .offset(offset)
            .fetch()

        // 各書籍の著者情報を取得してBookオブジェクトを作成
        return bookRecords.map { record ->
            val bookId = record[BOOKS.ID]!!
            val authors = findAuthorsByBookId(bookId)

            Book(
                id = bookId,
                title = record[BOOKS.TITLE] ?: throw IllegalStateException("書籍タイトルの取得に失敗しました"),
                price = record[BOOKS.PRICE] ?: throw IllegalStateException("価格の取得に失敗しました"),
                publicationStatus = record[BOOKS.PUBLICATION_STATUS]?.let {
                    PublicationStatus.fromJooqEnum(it)
                } ?: PublicationStatus.UNPUBLISHED,
                authors = authors,
                createdAt = record[BOOKS.CREATED_AT] ?: throw IllegalStateException("作成日時の取得に失敗しました"),
                updatedAt = record[BOOKS.UPDATED_AT] ?: throw IllegalStateException("更新日時の取得に失敗しました")
            )
        }
    }

    /**
     * 著者IDでの検索結果数を取得
     */
    fun countByAuthorId(authorId: Long): Long {
        return dsl.selectCount()
            .from(BOOKS)
            .join(BOOK_AUTHORS).on(BOOKS.ID.eq(BOOK_AUTHORS.BOOK_ID))
            .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
            .fetchOne(0, Long::class.java) ?: 0
    }

    /**
     * 新しい書籍を保存（著者関連付け含む）
     */
    fun save(book: Book): Book {
        require(book.authors.isNotEmpty()) { "書籍には最低1人の著者が必要です" }
        require(book.authors.all { it.id != null }) { "著者のIDが設定されていません" }

        val now = LocalDateTime.now()

        // 書籍を保存
        val insertedRecord = dsl.insertInto(BOOKS)
            .set(BOOKS.TITLE, book.title)
            .set(BOOKS.PRICE, book.price)
            .set(BOOKS.PUBLICATION_STATUS, book.publicationStatus.toJooqEnum())
            .set(BOOKS.CREATED_AT, now)
            .set(BOOKS.UPDATED_AT, now)
            .returning()
            .fetchOne() ?: throw IllegalStateException("書籍の保存に失敗しました")

        val bookId = insertedRecord[BOOKS.ID]

        // 著者との関連を保存
        book.authors.forEach { author ->
            dsl.insertInto(BOOK_AUTHORS)
                .set(BOOK_AUTHORS.BOOK_ID, bookId)
                .set(BOOK_AUTHORS.AUTHOR_ID, author.id)
                .execute()
        }

        return Book(
            id = bookId,
            title = insertedRecord[BOOKS.TITLE] ?: throw IllegalStateException("書籍タイトルの取得に失敗しました"),
            price = insertedRecord[BOOKS.PRICE] ?: throw IllegalStateException("価格の取得に失敗しました"),
            publicationStatus = insertedRecord[BOOKS.PUBLICATION_STATUS]?.let {
                PublicationStatus.fromJooqEnum(it)
            } ?: PublicationStatus.UNPUBLISHED,
            authors = book.authors,
            createdAt = insertedRecord[BOOKS.CREATED_AT] ?: throw IllegalStateException("作成日時の取得に失敗しました"),
            updatedAt = insertedRecord[BOOKS.UPDATED_AT] ?: throw IllegalStateException("更新日時の取得に失敗しました")
        )
    }

    /**
     * 既存の書籍を更新（著者関連付け含む）
     */
    fun update(book: Book): Book {
        require(book.id != null) { "更新対象の書籍IDが指定されていません" }
        require(book.authors.isNotEmpty()) { "書籍には最低1人の著者が必要です" }
        require(book.authors.all { it.id != null }) { "著者のIDが設定されていません" }

        val now = LocalDateTime.now()

        // 書籍情報を更新
        val updatedCount = dsl.update(BOOKS)
            .set(BOOKS.TITLE, book.title)
            .set(BOOKS.PRICE, book.price)
            .set(BOOKS.PUBLICATION_STATUS, book.publicationStatus.toJooqEnum())
            .set(BOOKS.UPDATED_AT, now)
            .where(BOOKS.ID.eq(book.id))
            .execute()

        if (updatedCount == 0) {
            throw IllegalArgumentException("指定されたID=${book.id}の書籍が見つかりません")
        }

        // 既存の著者関連を削除
        dsl.deleteFrom(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(book.id))
            .execute()

        // 新しい著者関連を保存
        book.authors.forEach { author ->
            dsl.insertInto(BOOK_AUTHORS)
                .set(BOOK_AUTHORS.BOOK_ID, book.id)
                .set(BOOK_AUTHORS.AUTHOR_ID, author.id)
                .execute()
        }

        return book.copy(updatedAt = now)
    }

    /**
     * 同タイトルの書籍が存在するかチェック
     */
    fun existsByTitle(title: String): Boolean {
        return (dsl.selectCount()
            .from(BOOKS)
            .where(BOOKS.TITLE.eq(title))
            .fetchOne(0, Int::class.java) ?: 0) > 0
    }

    /**
     * 指定ID以外で同タイトルの書籍が存在するかチェック（更新時の重複チェック用）
     */
    fun existsByTitleAndIdNot(title: String, id: Long): Boolean {
        return (dsl.selectCount()
            .from(BOOKS)
            .where(BOOKS.TITLE.eq(title))
            .and(BOOKS.ID.ne(id))
            .fetchOne(0, Int::class.java) ?: 0) > 0
    }

    /**
     * 指定された書籍IDの著者リストを取得
     */
    private fun findAuthorsByBookId(bookId: Long): List<Author> {
        return dsl.select(AUTHORS.fields().toList())
            .from(AUTHORS)
            .join(BOOK_AUTHORS).on(AUTHORS.ID.eq(BOOK_AUTHORS.AUTHOR_ID))
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .orderBy(AUTHORS.NAME)
            .fetch()
            .map { record ->
                Author(
                    id = record[AUTHORS.ID],
                    name = record[AUTHORS.NAME] ?: throw IllegalStateException("著者名の取得に失敗しました"),
                    birthDate = record[AUTHORS.BIRTH_DATE] ?: throw IllegalStateException("生年月日の取得に失敗しました"),
                    createdAt = record[AUTHORS.CREATED_AT] ?: throw IllegalStateException("作成日時の取得に失敗しました"),
                    updatedAt = record[AUTHORS.UPDATED_AT] ?: throw IllegalStateException("更新日時の取得に失敗しました")
                )
            }
    }
}
