# Library Management API Documentation

## 概要
図書館システムの書籍・著者管理API。書籍と著者情報の作成、読み取り、更新、削除（CRUD）機能とページネーション検索機能を提供します。書籍と著者は多対多の関係で管理されます。

## Base URLs
```
/api/books    # 書籍管理
/api/authors  # 著者管理
```

# 書籍管理API

## 1. 書籍一覧取得 (ページネーション対応)
**GET** `/api/books`

書籍の一覧をページネーション形式で取得します。タイトル検索、出版状況フィルター、著者IDでの絞り込みに対応。

### クエリパラメータ
| パラメータ | 型 | 必須 | デフォルト値 | 説明 |
|-----------|----|----|------------|------|
| `page` | int | ❌ | 0 | ページ番号（0ベース） |
| `size` | int | ❌ | 20 | ページサイズ |
| `title` | string | ❌ | - | 書籍タイトルでの部分検索 |
| `status` | enum | ❌ | - | 出版状況（PUBLISHED, UNPUBLISHED） |
| `authorId` | long | ❌ | - | 著者IDでの絞り込み |

### レスポンス例
```json
{
  "content": [
    {
      "id": 1,
      "title": "ノルウェイの森",
      "price": 1800.00,
      "formattedPrice": "¥1800.00",
      "publicationStatus": "PUBLISHED",
      "isPublished": true,
      "authors": [
        {
          "id": 2,
          "name": "村上春樹",
          "birthDate": "1949-01-12",
          "createdAt": "2025-01-01T10:00:00",
          "updatedAt": "2025-01-01T10:00:00"
        }
      ],
      "authorNames": "村上春樹",
      "createdAt": "2025-01-01T10:00:00",
      "updatedAt": "2025-01-01T10:00:00"
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 1,
  "totalPages": 1,
  "isFirst": true,
  "isLast": true,
  "isEmpty": false
}
```

## 2. 書籍詳細取得
**GET** `/api/books/{id}`

指定されたIDの書籍詳細情報を取得します。

### パスパラメータ
| パラメータ | 型 | 必須 | 説明 |
|-----------|----|----|------|
| `id` | long | ✅ | 書籍ID |

### レスポンス例
```json
{
  "id": 1,
  "title": "ノルウェイの森",
  "price": 1800.00,
  "formattedPrice": "¥1800.00",
  "publicationStatus": "PUBLISHED",
  "isPublished": true,
  "authors": [
    {
      "id": 2,
      "name": "村上春樹",
      "birthDate": "1949-01-12",
      "createdAt": "2025-01-01T10:00:00",
      "updatedAt": "2025-01-01T10:00:00"
    }
  ],
  "authorNames": "村上春樹",
  "createdAt": "2025-01-01T10:00:00",
  "updatedAt": "2025-01-01T10:00:00"
}
```

## 3. 書籍登録
**POST** `/api/books`

新しい書籍を登録します。

### リクエストボディ
```json
{
  "title": "海辺のカフカ",
  "price": 2200.00,
  "publicationStatus": "PUBLISHED",
  "authorIds": [2]
}
```

### バリデーションルール
- `title`: 必須、空白不可
- `price`: 必須、0以上の数値
- `publicationStatus`: 任意（デフォルト: UNPUBLISHED）
- `authorIds`: 必須、最低1人の著者IDが必要

### レスポンス例
**ステータス**: `201 Created`
```json
{
  "id": 2,
  "title": "海辺のカフカ",
  "price": 2200.00,
  "formattedPrice": "¥2200.00",
  "publicationStatus": "PUBLISHED",
  "isPublished": true,
  "authors": [
    {
      "id": 2,
      "name": "村上春樹",
      "birthDate": "1949-01-12",
      "createdAt": "2025-01-01T10:00:00",
      "updatedAt": "2025-01-01T10:00:00"
    }
  ],
  "authorNames": "村上春樹",
  "createdAt": "2025-01-01T15:00:00",
  "updatedAt": "2025-01-01T15:00:00"
}
```

## 4. 書籍情報更新
**PUT** `/api/books/{id}`

既存の書籍情報を更新します。

### パスパラメータ
| パラメータ | 型 | 必須 | 説明 |
|-----------|----|----|------|
| `id` | long | ✅ | 書籍ID |

### リクエストボディ
```json
{
  "title": "海辺のカフカ（改訂版）",
  "price": 2400.00,
  "publicationStatus": "PUBLISHED",
  "authorIds": [2]
}
```

### バリデーションルール
- `title`: 必須、空白不可
- `price`: 必須、0以上の数値
- `publicationStatus`: 必須
- `authorIds`: 必須、最低1人の著者IDが必要

### レスポンス例
```json
{
  "id": 2,
  "title": "海辺のカフカ（改訂版）",
  "price": 2400.00,
  "formattedPrice": "¥2400.00",
  "publicationStatus": "PUBLISHED",
  "isPublished": true,
  "authors": [
    {
      "id": 2,
      "name": "村上春樹",
      "birthDate": "1949-01-12",
      "createdAt": "2025-01-01T10:00:00",
      "updatedAt": "2025-01-01T10:00:00"
    }
  ],
  "authorNames": "村上春樹",
  "createdAt": "2025-01-01T15:00:00",
  "updatedAt": "2025-01-01T16:00:00"
}
```

## 5. 書籍削除
**DELETE** `/api/books/{id}`

指定されたIDの書籍を削除します。

### パスパラメータ
| パラメータ | 型 | 必須 | 説明 |
|-----------|----|----|------|
| `id` | long | ✅ | 書籍ID |

### レスポンス
**ステータス**: `204 No Content`

## 6. 書籍存在チェック
**GET** `/api/books/{id}/exists`

指定されたIDの書籍が存在するかチェックします。

### パスパラメータ
| パラメータ | 型 | 必須 | 説明 |
|-----------|----|----|------|
| `id` | long | ✅ | 書籍ID |

### レスポンス例
```json
{
  "exists": true
}
```

## 7. 出版状況更新
**PATCH** `/api/books/{id}/publication-status`

書籍の出版状況を更新します。ビジネスルール：出版済み→未出版への変更は不可。

### パスパラメータ
| パラメータ | 型 | 必須 | 説明 |
|-----------|----|----|------|
| `id` | long | ✅ | 書籍ID |

### クエリパラメータ
| パラメータ | 型 | 必須 | 説明 |
|-----------|----|----|------|
| `status` | enum | ✅ | 新しい出版状況（PUBLISHED, UNPUBLISHED） |

### レスポンス例
```json
{
  "id": 2,
  "title": "海辺のカフカ",
  "publicationStatus": "PUBLISHED",
  "isPublished": true,
  "authors": [...],
  "updatedAt": "2025-01-01T17:00:00"
}
```

## 8. 書籍に著者追加
**POST** `/api/books/{bookId}/authors/{authorId}`

書籍に著者を追加します。

### パスパラメータ
| パラメータ | 型 | 必須 | 説明 |
|-----------|----|----|------|
| `bookId` | long | ✅ | 書籍ID |
| `authorId` | long | ✅ | 追加する著者ID |

### レスポンス例
```json
{
  "id": 2,
  "title": "海辺のカフカ",
  "authors": [
    {"id": 2, "name": "村上春樹", ...},
    {"id": 3, "name": "新しい著者", ...}
  ],
  "authorNames": "村上春樹, 新しい著者",
  "updatedAt": "2025-01-01T17:30:00"
}
```

## 9. 書籍から著者削除
**DELETE** `/api/books/{bookId}/authors/{authorId}`

書籍から著者を削除します。最低1人の著者が必要です。

### パスパラメータ
| パラメータ | 型 | 必須 | 説明 |
|-----------|----|----|------|
| `bookId` | long | ✅ | 書籍ID |
| `authorId` | long | ✅ | 削除する著者ID |

### レスポンス例
```json
{
  "id": 2,
  "title": "海辺のカフカ",
  "authors": [
    {"id": 2, "name": "村上春樹", ...}
  ],
  "authorNames": "村上春樹",
  "updatedAt": "2025-01-01T17:45:00"
}
```

## 10. 出版済み書籍一覧
**GET** `/api/books/published`

出版済みの書籍一覧を取得します。

### クエリパラメータ
| パラメータ | 型 | 必須 | デフォルト値 | 説明 |
|-----------|----|----|------------|------|
| `page` | int | ❌ | 0 | ページ番号（0ベース） |
| `size` | int | ❌ | 20 | ページサイズ |

## 11. 未出版書籍一覧
**GET** `/api/books/unpublished`

未出版の書籍一覧を取得します。

### クエリパラメータ
| パラメータ | 型 | 必須 | デフォルト値 | 説明 |
|-----------|----|----|------------|------|
| `page` | int | ❌ | 0 | ページ番号（0ベース） |
| `size` | int | ❌ | 20 | ページサイズ |

# 著者管理API

## 1. 著者一覧取得 (ページネーション対応)
**GET** `/api/authors`

著者の一覧をページネーション形式で取得します。名前による部分検索にも対応。

#### クエリパラメータ
| パラメータ | 型 | 必須 | デフォルト値 | 説明 |
|-----------|----|----|------------|------|
| `page` | int | ❌ | 0 | ページ番号（0ベース） |
| `size` | int | ❌ | 20 | ページサイズ |
| `name` | string | ❌ | - | 著者名での部分検索 |

#### レスポンス例
```json
{
  "content": [
    {
      "id": 1,
      "name": "夏目漱石",
      "birthDate": "1867-02-09",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 1,
  "totalPages": 1,
  "isFirst": true,
  "isLast": true,
  "isEmpty": false
}
```

### 2. 著者詳細取得
**GET** `/api/authors/{id}`

指定されたIDの著者詳細情報を取得します。

#### パスパラメータ
| パラメータ | 型 | 必須 | 説明 |
|-----------|----|----|------|
| `id` | long | ✅ | 著者ID |

#### レスポンス例
```json
{
  "id": 1,
  "name": "夏目漱石",
  "birthDate": "1867-02-09",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

### 3. 著者登録
**POST** `/api/authors`

新しい著者を登録します。

#### リクエストボディ
```json
{
  "name": "芥川龍之介",
  "birthDate": "1892-03-01"
}
```

#### バリデーションルール
- `name`: 必須、空白不可
- `birthDate`: 必須、過去の日付である必要があります

#### レスポンス例
**ステータス**: `201 Created`
```json
{
  "id": 2,
  "name": "芥川龍之介",
  "birthDate": "1892-03-01",
  "createdAt": "2024-01-01T15:00:00",
  "updatedAt": "2024-01-01T15:00:00"
}
```

### 4. 著者情報更新
**PUT** `/api/authors/{id}`

既存の著者情報を更新します。

#### パスパラメータ
| パラメータ | 型 | 必須 | 説明 |
|-----------|----|----|------|
| `id` | long | ✅ | 著者ID |

#### リクエストボディ
```json
{
  "name": "芥川龍之介",
  "birthDate": "1892-03-01"
}
```

#### バリデーションルール
- `name`: 必須、空白不可
- `birthDate`: 必須、過去の日付である必要があります

#### レスポンス例
```json
{
  "id": 2,
  "name": "芥川龍之介",
  "birthDate": "1892-03-01",
  "createdAt": "2024-01-01T15:00:00",
  "updatedAt": "2024-01-01T16:00:00"
}
```

### 5. 著者削除
**DELETE** `/api/authors/{id}`

指定されたIDの著者を削除します。

#### パスパラメータ
| パラメータ | 型 | 必須 | 説明 |
|-----------|----|----|------|
| `id` | long | ✅ | 著者ID |

#### レスポンス
**ステータス**: `204 No Content`

### 6. 著者存在チェック
**GET** `/api/authors/{id}/exists`

指定されたIDの著者が存在するかチェックします。

#### パスパラメータ
| パラメータ | 型 | 必須 | 説明 |
|-----------|----|----|------|
| `id` | long | ✅ | 著者ID |

#### レスポンス例
```json
{
  "exists": true
}
```

## 共通レスポンス形式

### PagedResponse<T>
ページネーション対応のレスポンス形式
```json
{
  "content": [], // データ配列
  "pageNumber": 0, // 現在ページ番号（0ベース）
  "pageSize": 20, // ページサイズ
  "totalElements": 100, // 全要素数
  "totalPages": 5, // 全ページ数
  "isFirst": true, // 最初のページか
  "isLast": false, // 最後のページか
  "isEmpty": false // 空のページか
}
```

### BookResponse
書籍情報のレスポンス形式
```json
{
  "id": 1, // 書籍ID
  "title": "ノルウェイの森", // 書籍タイトル
  "price": 1800.00, // 価格
  "formattedPrice": "¥1800.00", // フォーマット済み価格
  "publicationStatus": "PUBLISHED", // 出版状況
  "isPublished": true, // 出版済みかどうか
  "authors": [...], // 著者リスト（AuthorResponse配列）
  "authorNames": "村上春樹", // 著者名（カンマ区切り）
  "createdAt": "2025-01-01T10:00:00", // 作成日時
  "updatedAt": "2025-01-01T10:00:00" // 更新日時
}
```

### AuthorResponse
著者情報のレスポンス形式
```json
{
  "id": 1, // 著者ID
  "name": "夏目漱石", // 著者名
  "birthDate": "1867-02-09", // 生年月日
  "age": 156, // 年齢（計算値）
  "createdAt": "2025-01-01T10:00:00", // 作成日時
  "updatedAt": "2025-01-01T10:00:00" // 更新日時
}
```

## エラーレスポンス

### バリデーションエラー
**ステータス**: `400 Bad Request`
```json
{
  "error": "Validation failed",
  "message": "著者名は必須です"
}
```

### リソースが見つからない
**ステータス**: `404 Not Found`
```json
{
  "error": "Not Found",
  "message": "指定された著者が見つかりません"
}
```

## 使用例

### 書籍管理API cURLコマンド例

#### 書籍一覧取得（タイトル検索）
```bash
curl -X GET "http://localhost:8080/api/books?title=ノルウェイ&page=0&size=10"
```

#### 出版済み書籍一覧取得
```bash
curl -X GET "http://localhost:8080/api/books?status=PUBLISHED"
```

#### 著者IDで書籍検索
```bash
curl -X GET "http://localhost:8080/api/books?authorId=2"
```

#### 書籍登録
```bash
curl -X POST "http://localhost:8080/api/books" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "海辺のカフカ",
    "price": 2200.00,
    "publicationStatus": "PUBLISHED",
    "authorIds": [2]
  }'
```

#### 書籍更新
```bash
curl -X PUT "http://localhost:8080/api/books/1" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "ノルウェイの森（改訂版）",
    "price": 1900.00,
    "publicationStatus": "PUBLISHED",
    "authorIds": [2]
  }'
```

#### 出版状況更新
```bash
curl -X PATCH "http://localhost:8080/api/books/1/publication-status?status=PUBLISHED"
```

#### 書籍に著者追加
```bash
curl -X POST "http://localhost:8080/api/books/1/authors/3"
```

#### 書籍から著者削除
```bash
curl -X DELETE "http://localhost:8080/api/books/1/authors/3"
```

#### 書籍削除
```bash
curl -X DELETE "http://localhost:8080/api/books/1"
```

### 著者管理API cURLコマンド例

#### 著者一覧取得（名前検索）
```bash
curl -X GET "http://localhost:8080/api/authors?name=村上&page=0&size=10"
```

#### 著者登録
```bash
curl -X POST "http://localhost:8080/api/authors" \
  -H "Content-Type: application/json" \
  -d '{"name": "太宰治", "birthDate": "1909-06-19"}'
```

#### 著者更新
```bash
curl -X PUT "http://localhost:8080/api/authors/1" \
  -H "Content-Type: application/json" \
  -d '{"name": "夏目漱石", "birthDate": "1867-02-09"}'
```

#### 著者削除
```bash
curl -X DELETE "http://localhost:8080/api/authors/1"
```

## ビジネスルール

### 書籍管理
- 書籍には最低1人の著者が必要
- 出版済み（PUBLISHED）から未出版（UNPUBLISHED）への変更は不可
- 書籍タイトルの重複は不可
- 価格は0以上の数値

### 著者管理
- 著者名の重複は不可
- 生年月日は過去の日付である必要がある

### 書籍と著者の関係
- 多対多関係：1つの書籍に複数の著者、1人の著者が複数の書籍を持てる
- 著者削除時は関連する書籍との関係も削除される
