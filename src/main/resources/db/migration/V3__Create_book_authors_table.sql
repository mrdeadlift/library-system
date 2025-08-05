-- 書籍-著者の関連を管理する中間テーブル
CREATE TABLE book_authors (
    book_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- 複合主キー: 同じ書籍-著者ペアは一度だけ登録可能
    PRIMARY KEY (book_id, author_id),
    
    -- 外部キー制約: 書籍削除時は関連データも削除
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    
    -- 外部キー制約: 著者削除時は関連データも削除
    FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
);

-- 著者から書籍を取得する際の高速化
CREATE INDEX idx_book_authors_author_id ON book_authors(author_id);

-- 書籍から著者を取得する際の高速化（主キーの一部なので不要だが明示的に）
CREATE INDEX idx_book_authors_book_id ON book_authors(book_id);

-- ビジネスルール確認: 書籍は最低1人の著者を持つ必要がある
-- 注意: この制約はアプリケーションレベルで管理し、
-- データベースでは参照整合性のみを保証する
-- （PostgreSQLでは「最低1つの関連レコードが必要」という制約を直接表現できないため）