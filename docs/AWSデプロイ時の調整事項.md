# AWS デプロイ時の調整事項

本ドキュメントは、FleaMarket-App を AWS 上にデプロイする際に設定・確認すべき項目をまとめたものです。

---

## 1. プロファイル・基本

| 項目 | 環境変数 | 説明 |
|------|----------|------|
| アクティブプロファイル | `SPRING_PROFILES_ACTIVE` | 本番では **`pg`** を指定すること。未指定時は `dev`（H2）になる。 |

```bash
SPRING_PROFILES_ACTIVE=pg
```

---

## 2. データベース（PostgreSQL）

`pg` プロファイル利用時は PostgreSQL に接続します。以下を環境変数で指定してください。

| 項目 | 環境変数 | 説明 |
|------|----------|------|
| JDBC URL | `DATASOURCE_URL` | 本番の RDS 等の接続文字列。例: `jdbc:postgresql://your-rds.region.rds.amazonaws.com:5432/flea_market` |
| ドライバ | `DATASOURCE_DRIVER` | `pg` プロファイルでは `org.postgresql.Driver` に上書きされる。 |
| ユーザー名 | `SPRING_DATASOURCE_USERNAME` | DB ユーザー名。 |
| パスワード | `SPRING_DATASOURCE_PASSWORD` | DB パスワード。 |

- VPC 内の RDS に接続する場合、アプリは同じ VPC（または VPC ピアリング等）に配置し、セキュリティグループで 5432 を許可すること。
- Flyway が有効なため、初回起動時に `classpath:db/migration` のマイグレーションが実行される。本番 DB には事前に空の DB を作成しておくこと。

---

## 3. AWS S3

プロフィール画像・商品画像の保存先です。

### 3.1 必須設定

| 項目 | 環境変数 | 説明 |
|------|----------|------|
| バケット名 | `AWS_S3_BUCKET_NAME` | **必須。** 実際の S3 バケット名を指定すること。未設定時は `your-bucket-name` のままとなりアップロード失敗する。 |
| リージョン | `AWS_S3_REGION` | 省略時は `ap-northeast-1`。バケットのリージョンに合わせること。 |

### 3.2 IAM

アプリが動く実行環境（ECS タスクロール、EC2 インスタンスロールなど）に、少なくとも次の権限を付与すること。

- `s3:PutObject`
- `s3:GetObject`
- `s3:DeleteObject`

対象リソースは本番用バケット（および必要ならプレフィックス）に限定することを推奨。

### 3.3 認証

- S3 クライアントは **AWS SDK のデフォルト認証チェーン** を使用している。
- ECS/Fargate の場合はタスクロール、EC2 の場合はインスタンスロールが自動で使われる。
- ローカルや CI から接続する場合は、IAM ユーザーのアクセスキーを環境変数（`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`）や `~/.aws/credentials` で設定する。

### 3.4 画像 URL と公開設定

- 現在の実装は **`https://{bucket}.s3.{region}.amazonaws.com/{key}`** 形式の URL を生成している。
- この形式が有効なのは、バケットまたはオブジェクトが **パブリック読み取り可能** な場合。
- 本番でバケットを非公開にする場合は、次のいずれかに変更する必要がある（コード変更が必要）。
  - **CloudFront** をバケットの前に立て、CloudFront の URL を返す。
  - **署名付き URL（Presigned URL）** を発行して返す。

---

## 4. メール（SMTP）

通知等でメール送信を行う場合に設定します。

| 項目 | 環境変数 | 説明 |
|------|----------|------|
| ホスト | `MAIL_HOST` | SMTP サーバー。例: Amazon SES のエンドポイント。 |
| ポート | `MAIL_PORT` | 通常 587（TLS）。 |
| ユーザー名 | `MAIL_USERNAME` | SMTP 認証ユーザー。 |
| パスワード | `MAIL_PASSWORD` | SMTP 認証パスワード。 |
| 送信元 | `MAIL_FROM` | 送信元アドレス。例: `noreply@yourdomain.com` |

- Amazon SES を使う場合は、SES の SMTP 認証情報と送信元の検証（ドメイン or メールアドレス）が必要。

---

## 5. Stripe（決済利用時）

決済機能を有効にする場合は、`application.properties` のコメントを外し、以下を設定します。

| 項目 | プロパティ / 環境変数 | 説明 |
|------|------------------------|------|
| API キー | `app.stripe.api-key` | 本番用は `sk_live_xxx`。環境変数で渡す場合は `APP_STRIPE_API_KEY` 等で設定。 |
| Webhook シークレット | `app.stripe.webhook-secret` | Webhook 署名検証用。`whsec_xxx`。 |
| 通貨 | `app.stripe.currency` | 例: `JPY`。 |

- Webhook のエンドポイントは本番 URL で Stripe ダッシュボードに登録し、HTTPS で受信できるようにすること。

---

## 6. CORS（フロントエンドのオリジン）

API を別オリジン（フロントアプリ）から呼び出す場合、許可するオリジンを本番用に変更してください。

**現在の設定（要変更）:**

- `CorsConfig.java`: `http://localhost:3000`, `http://example.com`
- `WebMvcConfig.java`: 上記と同様（`/api/**`, `/community/**`）

**対応方針:**

- 本番フロントのオリジン（例: `https://your-app.example.com`）を追加する。
- 可能であれば、許可オリジンをプロパティ（例: `app.cors.allowed-origins`）で外部化し、環境変数で本番用に上書きすることを推奨。

---

## 7. その他

### 7.1 サーバーポート

- デフォルトは 8080。`application-dev.properties` では 8082。
- ECS/ALB 等ではコンテナは 8080 で listen し、ALB が 80/443 で受ける構成が一般的。

### 7.2 Flyway

- 本番では `classpath:db/migration` のみが使用される（`pg` プロファイルで `spring.flyway.locations=classpath:db/migration`）。
- `dev_migration` は本番に含めないこと。

### 7.3 商品一覧のページサイズ

- `app.product-list.page-size`（デフォルト 50）で変更可能。必要に応じてプロパティで設定。

---

## 8. 環境変数チェックリスト（本番例）

デプロイ前に、少なくとも以下を確認してください。

```bash
# 必須
SPRING_PROFILES_ACTIVE=pg
DATASOURCE_URL=jdbc:postgresql://your-rds-host:5432/flea_market
SPRING_DATASOURCE_USERNAME=***
SPRING_DATASOURCE_PASSWORD=***
AWS_S3_BUCKET_NAME=your-production-bucket

# 推奨（リージョンを変える場合）
AWS_S3_REGION=ap-northeast-1

# メール送信を使う場合
MAIL_HOST=email-smtp.ap-northeast-1.amazonaws.com
MAIL_PORT=587
MAIL_USERNAME=***
MAIL_PASSWORD=***
MAIL_FROM=noreply@yourdomain.com

# Stripe を使う場合（プロパティまたは環境変数）
# app.stripe.api-key, app.stripe.webhook-secret, app.stripe.currency
```

---

## 9. 参照

- アプリ設定: `src/main/resources/application.properties`
- 本番用 DB 設定: `src/main/resources/application-pg.properties`
- S3 URL 生成: `S3ImageServiceImpl.generateImageUrl`（CloudFront/署名付き URL の検討はコード内コメント参照）
