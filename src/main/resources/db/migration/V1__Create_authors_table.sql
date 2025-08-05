-- 著者テーブルの作成
CREATE TABLE authors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- ビジネスルール制約: 生年月日は過去の日付である必要がある
    CONSTRAINT chk_birth_date_past CHECK (birth_date < CURRENT_DATE)
);

-- 著者名での検索を高速化するためのインデックス
CREATE INDEX idx_authors_name ON authors(name);

-- 作成・更新日時の自動更新用トリガー関数の準備
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- updated_at列の自動更新トリガーを設定
CREATE TRIGGER update_authors_updated_at
    BEFORE UPDATE ON authors
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();