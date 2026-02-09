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

> **注意（Spring Boot 4.x）:** 自動実行には `spring-boot-starter-flyway` の依存が必要です。`flyway-core` のみではマイグレーションは実行されません。

```bash
./mvnw spring-boot:run
```

起動ログに以下のような出力があれば、マイグレーションは正常に完了しています。

```
Flyway Community Edition ...
Successfully applied X migration(s)
```

**マイグレーション対象:**
- `db/migration/` … V1__init.sql（スキーマ。orders.status に PENDING 含む）, V2__seed.sql（シードデータ）
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
| `db/migration/` | V1__init.sql | スキーマ（テーブル作成。orders.status は PENDING/PAID/...） |
| `db/migration/` | V2__seed.sql | 本番用シードデータ |
| `db/dev_migration/` | V70__dev_seed_users.sql | 開発用ユーザーシード |
| `db/dev_migration/` | V71__dev_seed_items.sql | 開発用商品シード |

`dev_migration` は `dev` プロファイル時のみ適用されます。本番（`pg` プロファイル）では `db/migration` のみが使用されます。

---

## 3.1 マイグレーションをクリーンして再実行する（開発用）

開発中に「マイグレーションを一度全部やり直したい」場合は、次の手順で **clean → migrate** を実行します。**本番DBでは絶対に実行しないでください。**

```bash
# 1. clean でスキーマを削除（要 -Dflyway.cleanDisabled=false）
./mvnw flyway:clean -Dflyway.cleanDisabled=false -Dflyway.password=postgres

# 2. マイグレーションを最初から実行（V1 → V2、dev の場合は V70/V71 は起動時に適用）
./mvnw flyway:migrate -Dflyway.password=postgres
```

パスワードが不要な環境では `-Dflyway.password=postgres` を外してよいです。  
`dev` プロファイルでアプリ起動時には `db/dev_migration` も読み込まれるため、clean + migrate のあと `./mvnw spring-boot:run` で起動すると V70・V71 も適用されます。

**別案（DB ごと作り直す）:** 上記の代わりに、データベースを削除して再作成してから起動しても同じ結果になります。

```bash
psql -U postgres -h localhost -c "DROP DATABASE flea_market;"
psql -U postgres -h localhost -c "CREATE DATABASE flea_market;"
./mvnw spring-boot:run
```

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
- **「Migration checksum mismatch for migration version 1」** … 既に適用済みの V1 を編集した場合に発生します。開発環境では **clean → migrate** で最初からやり直してください（下記コマンド）。
- 開発環境で DB を初期化してよい場合は、データベースを削除して再作成するか、下記「Flyway clean で開発用DBをリセット」を実行してください。

### Flyway clean で開発用DBをリセット
開発用のみで、スキーマごとすべて消してマイグレーションを最初からやり直したい場合に使います。**本番DBでは絶対に実行しないでください。**

Flyway は安全のため `clean` がデフォルトで無効です。有効にして実行するには `-Dflyway.cleanDisabled=false` を付けます。

```bash
# 1. target を消してから（古い dev_migration が target に残っていると migrate が失敗することがあります）
./mvnw clean compile -DskipTests

# 2. clean でスキーマを削除（要 -Dflyway.cleanDisabled=false）
./mvnw flyway:clean -Dflyway.cleanDisabled=false

# 3. マイグレーションを最初から実行
./mvnw flyway:migrate
```

パスワードを指定する場合:
```bash
./mvnw flyway:clean -Dflyway.cleanDisabled=false -Dflyway.password=postgres
./mvnw flyway:migrate -Dflyway.password=postgres
```

### 開発用シード（V70/V71）が反映されない・表示名が「購入者B」のまま

`dev` プロファイルで起動しているのに、ユーザー設定で「テストユーザー B」「鈴木 花子」などではなく「購入者B」「プロフィールはまだ未記入です」と表示される場合、**V70 が適用されていない**か、**V70 適用前に既に buyerB@example.com が登録されていた**可能性があります（auth_users の email UNIQUE により V70 の挿入が失敗している）。

**対処: 開発用 DB を一度リセットし、V1 → V2 → V70 → V71 をすべてやり直す。**

1. **アプリを止める**  
   `./mvnw spring-boot:run` を実行しているターミナルで Ctrl+C。

2. **PostgreSQL で開発用 DB を削除して作り直す**
   ```bash
   psql -U postgres -h localhost -c "DROP DATABASE flea_market;"
   psql -U postgres -h localhost -c "CREATE DATABASE flea_market;"
   ```
   （GUI ツールの場合は `DROP DATABASE flea_market;` → `CREATE DATABASE flea_market;` を実行）

3. **dev プロファイルでアプリを起動する**
   ```bash
   ./mvnw spring-boot:run
   ```
   または明示的に: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`

4. **起動ログで V70・V71 の実行を確認する**  
   `Migrating schema ...` で `V70__dev_seed_users.sql` と `V71__dev_seed_items.sql` が適用されていることを確認。

5. **ログインして表示を確認する**  
   `buyerB@example.com` / `password` でログインし、ユーザー設定で表示名「テストユーザー B」・配送先など「鈴木 花子」など V70 の内容になっていることを確認。

これで DevMigration のテストユーザー・本名が正しく反映されます。

### H2 で試したい場合
本プロジェクトは PostgreSQL を前提としており、H2 ドライバは `pom.xml` に含まれていません。開発・本番ともに PostgreSQL の利用を推奨します。
