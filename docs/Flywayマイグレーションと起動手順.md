# Flywayマイグレーションとアプリケーション起動手順

本ドキュメントでは、Flywayによるマイグレーション実行から Spring Boot アプリケーション起動までの手順を説明します。

## 前提条件

### 必要な環境
- **Java 17** 以上
- **Maven**（プロジェクトには Maven Wrapper `mvnw` が含まれているため、Mavenの事前インストールは不要）
- **PostgreSQL**（開発時 `dev` プロファイル使用時）

### 開発プロファイル（dev）のデフォルト設定
- プロファイル: `dev`（未指定時）
- DB: PostgreSQL `localhost:5432/flea_market`
- ユーザー: `postgres` / パスワード: `postgres`

---

## 1. データベースの準備（PostgreSQL 使用時）

開発環境で PostgreSQL を使う場合、事前に以下を実施してください。

### 1.1 PostgreSQL の起動
```bash
# macOS (Homebrew)
brew services start postgresql@14   # またはインストール済みのバージョン

# または Docker の場合
docker run -d --name postgres-flea -p 5432:5432 \
  -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
  postgres:14
```

### 1.2 データベースの作成
```bash
# PostgreSQL に接続
psql -U postgres -h localhost

# データベース作成
CREATE DATABASE flea_market;

# 終了
\q
```

---

## 2. マイグレーションの実行方法

Flyway のマイグレーションは **2通りの実行方法** があります。

### 方法A: Spring Boot 起動時に自動実行（推奨）

Flyway は Spring Boot に組み込まれており、`spring-boot:run` 実行時に **自動でマイグレーションが実行** されます。

```bash
./mvnw spring-boot:run
```

起動ログに以下のような出力があれば、マイグレーションは正常に完了しています。

```
Flyway Community Edition ...
Successfully applied X migration(s)
```

**マイグレーション対象:**
- `db/migration/` … V1__init.sql（スキーマ）, V2__seed.sql（シードデータ）
- `db/dev_migration/` … V70__dev_seed_users.sql, V71__dev_seed_items.sql（dev プロファイル時のみ）

---

### 方法B: Flyway Maven プラグインで手動実行してから起動

マイグレーションだけを先に実行したい場合、Flyway Maven プラグインを使用します。

#### 2.1 Flyway マイグレーションの実行
```bash
./mvnw flyway:migrate
```

**注意:** `pom.xml` の flyway-maven-plugin は以下の設定です。
- URL: `jdbc:postgresql://localhost:5432/flea_market`
- ユーザー: `postgres`
- パスワード: （空）※必要に応じて `pom.xml` または `-Dflyway.password=xxx` で指定

パスワードを指定する例:
```bash
./mvnw flyway:migrate -Dflyway.password=postgres
```

#### 2.2 マイグレーション状態の確認（任意）
```bash
./mvnw flyway:info
```

#### 2.3 アプリケーションの起動
```bash
./mvnw spring-boot:run
```

既にマイグレーション済みのため、起動時はマイグレーションのスキップまたは未適用分のみの適用が行われます。

---

## 3. マイグレーションファイルの構成

| 場所 | ファイル | 内容 |
|------|----------|------|
| `db/migration/` | V1__init.sql | スキーマ（テーブル作成など） |
| `db/migration/` | V2__seed.sql | 本番用シードデータ |
| `db/dev_migration/` | V70__dev_seed_users.sql | 開発用ユーザーシード |
| `db/dev_migration/` | V71__dev_seed_items.sql | 開発用商品シード |

`dev_migration` は `dev` プロファイル時のみ適用されます。本番（`pg` プロファイル）では `db/migration` のみが使用されます。

---

## 4. 起動手順のまとめ（チェックリスト）

1. [ ] PostgreSQL を起動する（開発時）
2. [ ] データベース `flea_market` を作成する
3. [ ] プロジェクトルートで `./mvnw spring-boot:run` を実行する
4. [ ] ログでマイグレーション成功を確認する
5. [ ] アプリケーションにアクセスする（例: http://localhost:8082）

**開発時デフォルト:** サーバーポートは `8082`（`application-dev.properties` で設定）

---

## 5. 環境変数によるカスタマイズ

必要に応じて以下の環境変数で設定を上書きできます。

| 環境変数 | 説明 | デフォルト（dev） |
|----------|------|-------------------|
| `SPRING_PROFILES_ACTIVE` | アクティブなプロファイル | `dev` |
| `DATASOURCE_URL` | データソースURL | `jdbc:postgresql://localhost:5432/flea_market` |
| `SPRING_DATASOURCE_USERNAME` | DBユーザー名 | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | DBパスワード | `postgres` |

例: 本番プロファイルで起動する場合
```bash
SPRING_PROFILES_ACTIVE=pg ./mvnw spring-boot:run
```

---

## 6. トラブルシューティング

### マイグレーションが失敗する
- PostgreSQL が起動しているか確認する
- データベース `flea_market` が存在するか確認する
- 認証情報（ユーザー名・パスワード）が正しいか確認する

### Flyway の「Validate failed」エラー
- 既存の `flyway_schema_history` テーブルとマイグレーションファイルの整合性が取れていない可能性があります
- 開発環境で DB を初期化してよい場合は、データベースを削除して再作成し、マイグレーションをやり直してください

### H2 で試したい場合
本プロジェクトは PostgreSQL を前提としており、H2 ドライバは `pom.xml` に含まれていません。開発・本番ともに PostgreSQL の利用を推奨します。
