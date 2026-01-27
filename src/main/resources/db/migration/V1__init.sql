-- V1__init.sql
-- PostgreSQL 13+ 推奨（gen_random_uuid() のため pgcrypto 使用）
-- 方針:
-- - PKはUUID
-- - emailはcitext（大小文字差を吸収）
-- - 金額はbigint（最小通貨単位）
-- - 手数料率はbps（0..10000）
-- - 画像はS3 object keyを保存（URLは保存しない）
-- - 監査/通報は target_type + target_id

BEGIN;

-- =========
-- Extensions
-- =========
CREATE EXTENSION IF NOT EXISTS pgcrypto;  -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS citext;    -- case-insensitive email

-- =========
-- Common: updated_at自動更新
-- =========
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS trigger AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =========
-- 1) User / Rank (業務ユーザー)
-- =========
CREATE TABLE user_ranks (
  id              smallint PRIMARY KEY,  -- 1:BRONZE, 2:SILVER, 3:GOLD...
  rank_code       text NOT NULL UNIQUE,  -- 'BRONZE' 等
  rank_name       text NOT NULL,
  commission_bps  integer NOT NULL CHECK (commission_bps >= 0 AND commission_bps <= 10000),
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_user_ranks_updated_at
BEFORE UPDATE ON user_ranks
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE users (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  display_name    text NOT NULL,
  user_rank_id    smallint NOT NULL REFERENCES user_ranks(id),
  identity_status text NOT NULL CHECK (identity_status IN ('UNVERIFIED','PENDING','VERIFIED','REJECTED')),
  is_active       boolean NOT NULL DEFAULT true,
  -- プロフィール画像: S3のobject keyを保存（URLは保存しない）
  -- 命名規則: profiles/{userId}/{uuid}.{ext}
  profile_image_s3_key text NULL,
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_rank   ON users(user_rank_id);
CREATE INDEX idx_users_active ON users(is_active);

CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =========
-- 2) Auth (認証情報をUserから分離)
-- =========
CREATE TABLE auth_users (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id        uuid NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  email          citext NOT NULL UNIQUE,
  password_hash  text NOT NULL,
  is_admin       boolean NOT NULL DEFAULT false, -- 最小。将来 roles 化可
  created_at     timestamptz NOT NULL DEFAULT now(),
  updated_at     timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_auth_users_user_id ON auth_users(user_id);

CREATE TRIGGER trg_auth_users_updated_at
BEFORE UPDATE ON auth_users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- RefreshTokenは漏洩対策で生トークンを保存しない（hash推奨）
CREATE TABLE refresh_tokens (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash   text NOT NULL UNIQUE,
  expires_at   timestamptz NOT NULL,
  revoked_at   timestamptz NULL,
  created_at   timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_user_id  ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires  ON refresh_tokens(expires_at);

-- =========
-- 3) Category / Item (Catalog + Listing を初期は一本化)
-- =========
CREATE TABLE categories (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  parent_id    uuid NULL REFERENCES categories(id) ON DELETE SET NULL,
  name        text NOT NULL,
  created_at  timestamptz NOT NULL DEFAULT now(),
  updated_at  timestamptz NOT NULL DEFAULT now(),
  UNIQUE (parent_id, name)
);

CREATE INDEX idx_categories_parent ON categories(parent_id);

CREATE TRIGGER trg_categories_updated_at
BEFORE UPDATE ON categories
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- items.status:
-- 'DRAFT','PUBLISHED','IN_TRADE','SOLD','SUSPENDED','DELETED'
-- items.condition:
-- 'NEW','LIKE_NEW','USED_GOOD','USED_FAIR','USED_POOR'
-- shipping_fee_payer:
-- 'SELLER','BUYER'
CREATE TABLE items (
  id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),

  -- コピー出品対応：元の出品を辿れる
  copied_from_item_id  uuid NULL REFERENCES items(id) ON DELETE SET NULL,
  copy_source          text NULL CHECK (copy_source IS NULL OR copy_source IN ('USER_COPY','RELIST','ADMIN_COPY')),

  seller_id            uuid NOT NULL REFERENCES users(id),
  category_id          uuid NOT NULL REFERENCES categories(id),
  name                text NOT NULL,
  description         text NOT NULL DEFAULT '',
  price_amount        bigint NOT NULL CHECK (price_amount >= 0),
  currency            char(3) NOT NULL DEFAULT 'JPY',
  status              text NOT NULL CHECK (status IN ('DRAFT','PUBLISHED','IN_TRADE','SOLD','SUSPENDED','DELETED')),
  condition           text NOT NULL CHECK (condition IN ('NEW','LIKE_NEW','USED_GOOD','USED_FAIR','USED_POOR')),
  shipping_fee_payer  text NOT NULL CHECK (shipping_fee_payer IN ('SELLER','BUYER')),

  created_at          timestamptz NOT NULL DEFAULT now(),
  updated_at          timestamptz NOT NULL DEFAULT now(),

  CHECK (copied_from_item_id IS NULL OR copied_from_item_id <> id)
);

CREATE INDEX idx_items_status_created          ON items(status, created_at DESC);
CREATE INDEX idx_items_category_status_created ON items(category_id, status, created_at DESC);
CREATE INDEX idx_items_seller_created          ON items(seller_id, created_at DESC);
CREATE INDEX idx_items_copied_from             ON items(copied_from_item_id);

CREATE TRIGGER trg_items_updated_at
BEFORE UPDATE ON items
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 画像：S3のobject key を保存する（URL直保存しない）
-- 例: bucketは環境変数/設定で管理し、ここには key のみ
-- s3_key: "items/{itemId}/{uuid}.jpg" のようなパス
CREATE TABLE item_images (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  item_id        uuid NOT NULL REFERENCES items(id) ON DELETE CASCADE,
  s3_key         text NOT NULL,
  content_type   text NULL,           -- image/jpeg など（任意）
  byte_size      bigint NULL CHECK (byte_size IS NULL OR byte_size >= 0),
  display_order  smallint NOT NULL DEFAULT 0 CHECK (display_order >= 0),
  created_at     timestamptz NOT NULL DEFAULT now(),

  -- 同一item内で順序が被らないようにする
  UNIQUE (item_id, display_order),

  -- 同一item内で同じキーを二重登録しない
  UNIQUE (item_id, s3_key)
);

CREATE INDEX idx_item_images_item_order ON item_images(item_id, display_order);

-- =========
-- 4) Transaction / Chat / Review
-- =========
-- orders.status:
-- 'PAID','AWAITING_SHIPMENT','SHIPPED','COMPLETED','CANCELLED'
CREATE TABLE orders (
  id                       uuid PRIMARY KEY DEFAULT gen_random_uuid(),

  -- 1出品1取引（再出品は item をコピーして新しい id を発行するため成立）
  item_id                   uuid NOT NULL UNIQUE REFERENCES items(id),
  buyer_id                  uuid NOT NULL REFERENCES users(id),
  seller_id                 uuid NOT NULL REFERENCES users(id),

  stripe_payment_intent_id  text NULL UNIQUE,
  status                   text NOT NULL CHECK (status IN ('PAID','AWAITING_SHIPMENT','SHIPPED','COMPLETED','CANCELLED')),

  -- 取引時点の手数料率を固定（bps）
  applied_commission_bps   integer NOT NULL CHECK (applied_commission_bps >= 0 AND applied_commission_bps <= 10000),

  -- 金額（最小通貨単位）
  item_price_amount        bigint NOT NULL CHECK (item_price_amount >= 0),
  shipping_fee_amount      bigint NOT NULL DEFAULT 0 CHECK (shipping_fee_amount >= 0),
  total_amount             bigint NOT NULL CHECK (total_amount >= 0),
  currency                 char(3) NOT NULL DEFAULT 'JPY',

  -- 住所スナップショット：NOT NULL問題回避のため DEFAULT を付与
  -- 例: { "postalCode": "...", "pref": "...", "city": "...", "line1": "...", "name": "...", "phone": "..." }
  shipping_address_snapshot jsonb NOT NULL DEFAULT '{}'::jsonb,

  created_at               timestamptz NOT NULL DEFAULT now(),
  updated_at               timestamptz NOT NULL DEFAULT now(),

  CHECK (buyer_id <> seller_id)
);

CREATE INDEX idx_orders_buyer_created  ON orders(buyer_id, created_at DESC);
CREATE INDEX idx_orders_seller_created ON orders(seller_id, created_at DESC);
CREATE INDEX idx_orders_status_created ON orders(status, created_at DESC);

CREATE TRIGGER trg_orders_updated_at
BEFORE UPDATE ON orders
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 取引チャット：編集不可運用が前提
CREATE TABLE order_messages (
  id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id      uuid NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  sender_id     uuid NOT NULL REFERENCES users(id),
  content       text NOT NULL CHECK (length(content) <= 4000),
  is_template    boolean NOT NULL DEFAULT false,
  created_at    timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_order_messages_order_created  ON order_messages(order_id, created_at ASC);
CREATE INDEX idx_order_messages_sender_created ON order_messages(sender_id, created_at DESC);

-- 評価：1取引につき reviewer は1回まで
CREATE TABLE reviews (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id     uuid NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  reviewer_id  uuid NOT NULL REFERENCES users(id),
  reviewee_id  uuid NOT NULL REFERENCES users(id),
  rating       text NOT NULL CHECK (rating IN ('GOOD','BAD')),
  comment      text NULL CHECK (length(comment) <= 2000),
  created_at   timestamptz NOT NULL DEFAULT now(),

  CHECK (reviewer_id <> reviewee_id),
  UNIQUE (order_id, reviewer_id)
);

CREATE INDEX idx_reviews_reviewee_created ON reviews(reviewee_id, created_at DESC);
CREATE INDEX idx_reviews_order           ON reviews(order_id);

-- =========
-- 5) Audit / Report (管理・監査)
-- =========
CREATE TABLE audit_logs (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  admin_user_id  uuid NOT NULL REFERENCES auth_users(id),
  action        text NOT NULL,
  target_type   text NOT NULL,
  target_id     uuid NOT NULL,
  reason        text NOT NULL CHECK (length(reason) <= 2000),
  occurred_at   timestamptz NOT NULL DEFAULT now(),
  request_id    text NULL,
  ip_address    inet NULL,
  user_agent    text NULL
);

CREATE INDEX idx_audit_logs_target     ON audit_logs(target_type, target_id, occurred_at DESC);
CREATE INDEX idx_audit_logs_admin_time ON audit_logs(admin_user_id, occurred_at DESC);

CREATE TABLE reports (
  id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  reporter_id   uuid NOT NULL REFERENCES users(id),
  target_type  text NOT NULL,
  target_id    uuid NOT NULL,
  report_type  text NOT NULL CHECK (report_type IN ('COUNTERFEIT','HARASSMENT','SPAM','INAPPROPRIATE','OTHER')),
  description  text NULL CHECK (length(description) <= 2000),
  created_at   timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_reports_target_created   ON reports(target_type, target_id, created_at DESC);
CREATE INDEX idx_reports_reporter_created ON reports(reporter_id, created_at DESC);


COMMIT;
