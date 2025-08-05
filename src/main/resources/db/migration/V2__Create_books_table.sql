-- 出版状況のENUM型を定義
CREATE TYPE publication_status AS ENUM ('UNPUBLISHED', 'PUBLISHED');

-- 書籍テーブルの作成
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    publication_status publication_status NOT NULL DEFAULT 'UNPUBLISHED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- ビジネスルール制約: 価格は0以上である必要がある
    CONSTRAINT chk_price_non_negative CHECK (price >= 0)
);

-- 書籍タイトルでの検索を高速化するためのインデックス
CREATE INDEX idx_books_title ON books(title);

-- 出版状況での検索を高速化するためのインデックス
CREATE INDEX idx_books_publication_status ON books(publication_status);

-- 価格範囲での検索を高速化するためのインデックス
CREATE INDEX idx_books_price ON books(price);

-- updated_at列の自動更新トリガーを設定
CREATE TRIGGER update_books_updated_at
    BEFORE UPDATE ON books
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();