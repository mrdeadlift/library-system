# 図書館管理システム API ドキュメント

## 概要

図書館管理システムは、著者（Author）と書籍（Book）を管理するRESTful APIを提供します。
Spring Boot 3.5.4とKotlinで構築されており、jOOQを使用してPostgreSQLデータベースにアクセスします。

## 基本情報

- **ベースURL**: `http://localhost:8080`
- **コンテンツタイプ**: `application/json`
- **認証**: なし（開発環境）

## エラーレスポンス

APIエラーは標準的なHTTPステータスコードと共に返されます：

- `400 Bad Request`: 不正なリクエストパラメータやバリデーションエラー
- `404 Not Found`: 指定されたリソースが見つからない
- `500 Internal Server Error`: サーバー内部エラー

---

## 著者管理 API

### 著者一覧取得

著者の一覧をページネーション付きで取得します。

**エンドポイント**: `GET /api/authors`

**クエリパラメータ**:
- `page` (integer, optional): ページ番号（0ベース、デフォルト: 0）
- `size` (integer, optional): ページサイズ（デフォルト: 20）
- `name` (string, optional): 著者名での部分一致検索

**レスポンス例**:
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

### 著者詳細取得

指定されたIDの著者詳細を取得します。

**エンドポイント**: `GET /api/authors/{id}`

**パスパラメータ**:
- `id` (long): 著者ID

**レスポンス例**:
```json
{
  "id": 1,
  "name": "夏目漱石",
  "birthDate": "1867-02-09",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

### 著者登録

新しい著者を登録します。

**エンドポイント**: `POST /api/authors`

**リクエストボディ**:
```json
{
  "name": "夏目漱石",
  "birthDate": "1867-02-09"
}
```

**バリデーション**:
- `name`: 必須、空白不可
- `birthDate`: 必須、過去の日付

**レスポンス**: `201 Created`
```json
{
  "id": 1,
  "name": "夏目漱石",
  "birthDate": "1867-02-09",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

### 著者更新

既存の著者情報を更新します。

**エンドポイント**: `PUT /api/authors/{id}`

**パスパラメータ**:
- `id` (long): 著者ID

**リクエストボディ**:
```json
{
  "name": "夏目漱石",
  "birthDate": "1867-02-09"
}
```

**バリデーション**:
- `name`: 必須、空白不可
- `birthDate`: 必須、過去の日付

**レスポンス**: `200 OK`
```json
{
  "id": 1,
  "name": "夏目漱石",
  "birthDate": "1867-02-09",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T12:00:00"
}
```

---

## 書籍管理 API

### 書籍一覧取得

書籍の一覧をページネーション付きで取得します。

**エンドポイント**: `GET /api/books`

**クエリパラメータ**:
- `page` (integer, optional): ページ番号（0ベース、デフォルト: 0）
- `size` (integer, optional): ページサイズ（デフォルト: 20）
- `title` (string, optional): 書籍タイトルでの部分一致検索
- `status` (string, optional): 出版状況での絞り込み（`PUBLISHED`, `UNPUBLISHED`）
- `authorId` (long, optional): 著者IDでの絞り込み

**レスポンス例**:
```json
{
  "content": [
    {
      "id": 1,
      "title": "吾輩は猫である",
      "price": 1200,
      "formattedPrice": "¥1,200",
      "publicationStatus": "PUBLISHED",
      "isPublished": true,
      "authors": [
        {
          "id": 1,
          "name": "夏目漱石",
          "birthDate": "1867-02-09",
          "createdAt": "2024-01-01T10:00:00",
          "updatedAt": "2024-01-01T10:00:00"
        }
      ],
      "authorNames": "夏目漱石",
      "createdAt": "2024-01-01T11:00:00",
      "updatedAt": "2024-01-01T11:00:00"
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

### 書籍詳細取得

指定されたIDの書籍詳細を取得します。

**エンドポイント**: `GET /api/books/{id}`

**パスパラメータ**:
- `id` (long): 書籍ID

**レスポンス例**:
```json
{
  "id": 1,
  "title": "吾輩は猫である",
  "price": 1200,
  "formattedPrice": "¥1,200",
  "publicationStatus": "PUBLISHED",
  "isPublished": true,
  "authors": [
    {
      "id": 1,
      "name": "夏目漱石",
      "birthDate": "1867-02-09",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ],
  "authorNames": "夏目漱石",
  "createdAt": "2024-01-01T11:00:00",
  "updatedAt": "2024-01-01T11:00:00"
}
```

### 書籍登録

新しい書籍を登録します。

**エンドポイント**: `POST /api/books`

**リクエストボディ**:
```json
{
  "title": "吾輩は猫である",
  "price": 1200,
  "publicationStatus": "UNPUBLISHED",
  "authorIds": [1]
}
```

**バリデーション**:
- `title`: 必須、空白不可
- `price`: 必須、0以上の数値
- `publicationStatus`: 省略可（デフォルト: `UNPUBLISHED`）
- `authorIds`: 必須、最低1つの著者ID

**レスポンス**: `201 Created`
```json
{
  "id": 1,
  "title": "吾輩は猫である",
  "price": 1200,
  "formattedPrice": "¥1,200",
  "publicationStatus": "UNPUBLISHED",
  "isPublished": false,
  "authors": [
    {
      "id": 1,
      "name": "夏目漱石",
      "birthDate": "1867-02-09",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ],
  "authorNames": "夏目漱石",
  "createdAt": "2024-01-01T11:00:00",
  "updatedAt": "2024-01-01T11:00:00"
}
```

### 書籍更新

既存の書籍情報を更新します。

**エンドポイント**: `PUT /api/books/{id}`

**パスパラメータ**:
- `id` (long): 書籍ID

**リクエストボディ**:
```json
{
  "title": "吾輩は猫である（改訂版）",
  "price": 1300,
  "publicationStatus": "PUBLISHED",
  "authorIds": [1]
}
```

**バリデーション**:
- `title`: 必須、空白不可
- `price`: 必須、0以上の数値
- `publicationStatus`: 必須
- `authorIds`: 必須、最低1つの著者ID

**ビジネスルール**:
- 出版済み（`PUBLISHED`）から未出版（`UNPUBLISHED`）への変更は不可

**レスポンス**: `200 OK`
```json
{
  "id": 1,
  "title": "吾輩は猫である（改訂版）",
  "price": 1300,
  "formattedPrice": "¥1,300",
  "publicationStatus": "PUBLISHED",
  "isPublished": true,
  "authors": [
    {
      "id": 1,
      "name": "夏目漱石",
      "birthDate": "1867-02-09",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ],
  "authorNames": "夏目漱石",
  "createdAt": "2024-01-01T11:00:00",
  "updatedAt": "2024-01-01T12:00:00"
}
```

### 出版状況更新

書籍の出版状況のみを更新します。

**エンドポイント**: `PATCH /api/books/{id}/publication-status`

**パスパラメータ**:
- `id` (long): 書籍ID

**クエリパラメータ**:
- `status` (string): 新しい出版状況（`PUBLISHED`, `UNPUBLISHED`）

**ビジネスルール**:
- 出版済み（`PUBLISHED`）から未出版（`UNPUBLISHED`）への変更は不可

**レスポンス**: `200 OK`
```json
{
  "id": 1,
  "title": "吾輩は猫である",
  "price": 1200,
  "formattedPrice": "¥1,200",
  "publicationStatus": "PUBLISHED",
  "isPublished": true,
  "authors": [
    {
      "id": 1,
      "name": "夏目漱石",
      "birthDate": "1867-02-09",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ],
  "authorNames": "夏目漱石",
  "createdAt": "2024-01-01T11:00:00",
  "updatedAt": "2024-01-01T12:00:00"
}
```

### 書籍への著者追加

書籍に著者を追加します。

**エンドポイント**: `POST /api/books/{bookId}/authors/{authorId}`

**パスパラメータ**:
- `bookId` (long): 書籍ID
- `authorId` (long): 追加する著者ID

**レスポンス**: `200 OK`
```json
{
  "id": 1,
  "title": "吾輩は猫である",
  "price": 1200,
  "formattedPrice": "¥1,200",
  "publicationStatus": "PUBLISHED",
  "isPublished": true,
  "authors": [
    {
      "id": 1,
      "name": "夏目漱石",
      "birthDate": "1867-02-09",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    },
    {
      "id": 2,
      "name": "森鴎外",
      "birthDate": "1862-02-17",
      "createdAt": "2024-01-01T09:00:00",
      "updatedAt": "2024-01-01T09:00:00"
    }
  ],
  "authorNames": "夏目漱石, 森鴎外",
  "createdAt": "2024-01-01T11:00:00",
  "updatedAt": "2024-01-01T13:00:00"
}
```

### 書籍からの著者削除

書籍から著者を削除します。

**エンドポイント**: `DELETE /api/books/{bookId}/authors/{authorId}`

**パスパラメータ**:
- `bookId` (long): 書籍ID
- `authorId` (long): 削除する著者ID

**ビジネスルール**:
- 書籍には最低1人の著者が必要（最後の著者は削除不可）

**レスポンス**: `200 OK`
```json
{
  "id": 1,
  "title": "吾輩は猫である",
  "price": 1200,
  "formattedPrice": "¥1,200",
  "publicationStatus": "PUBLISHED",
  "isPublished": true,
  "authors": [
    {
      "id": 1,
      "name": "夏目漱石",
      "birthDate": "1867-02-09",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ],
  "authorNames": "夏目漱石",
  "createdAt": "2024-01-01T11:00:00",
  "updatedAt": "2024-01-01T13:00:00"
}
```

---

## データ型定義

### 出版状況（PublicationStatus）

書籍の出版状況を表す列挙型：

- `UNPUBLISHED`: 未出版
- `PUBLISHED`: 出版済み

**状態遷移ルール**:
- 未出版 → 出版済み: 可能
- 出版済み → 未出版: 不可（ビジネスルールにより制限）

### ページネーション（PagedResponse）

一覧APIで使用される共通のページネーション形式：

```json
{
  "content": [],           // データ配列
  "pageNumber": 0,         // 現在ページ番号（0ベース）
  "pageSize": 20,          // ページサイズ
  "totalElements": 100,    // 全要素数
  "totalPages": 5,         // 全ページ数
  "isFirst": true,         // 最初のページかどうか
  "isLast": false,         // 最後のページかどうか
  "isEmpty": false         // 空のページかどうか
}
```

---

## 使用例

### 基本的なワークフロー

1. **著者を登録する**:
   ```bash
   curl -X POST http://localhost:8080/api/authors \
     -H "Content-Type: application/json" \
     -d '{"name": "夏目漱石", "birthDate": "1867-02-09"}'
   ```

2. **書籍を登録する**:
   ```bash
   curl -X POST http://localhost:8080/api/books \
     -H "Content-Type: application/json" \
     -d '{"title": "吾輩は猫である", "price": 1200, "authorIds": [1]}'
   ```

3. **書籍を出版する**:
   ```bash
   curl -X PATCH "http://localhost:8080/api/books/1/publication-status?status=PUBLISHED"
   ```

4. **書籍一覧を取得する**:
   ```bash
   curl http://localhost:8080/api/books
   ```

### 検索例

- **著者名で検索**:
  ```bash
  curl "http://localhost:8080/api/authors?name=夏目"
  ```

- **出版済み書籍のみ取得**:
  ```bash
  curl "http://localhost:8080/api/books?status=PUBLISHED"
  ```

- **特定著者の書籍を取得**:
  ```bash
  curl "http://localhost:8080/api/books?authorId=1"
  ```