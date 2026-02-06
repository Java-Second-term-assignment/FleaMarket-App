-- V1__init.sql（統合版）
-- PostgreSQL 13+ 推奨（gen_random_uuid() のため pgcrypto 使用）
-- 方針:
-- - PKはUUID
-- - emailはcitext（大小文字差を吸収）
-- - 金額はbigint（最小通貨単位）
-- - 手数料率はbps（0..10000）
-- - 画像はS3 object keyを保存（URLは保存しない）
-- - 監査/通報は target_type + target_id
-- テーブル定義は最初から完成形（ADD COLUMN による後付けなし）

BEGIN;

-- =========
-- Extensions
-- =========
CREATE EXTENSION IF NOT EXISTS pgcrypto;  -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS citext;    -- case-insensitive email
CREATE EXTENSION IF NOT EXISTS pg_trgm;   -- items の LIKE 検索用

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
  frozen_until    timestamptz NULL,
  profile_image_s3_key text NULL,
  profile_image_url    varchar(500) NULL,
  caption         varchar(200) NULL,
  recipient_name  varchar(100) NULL,
  postal_code     varchar(20) NULL,
  address         varchar(500) NULL,
  phone           varchar(30) NULL,
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_rank   ON users(user_rank_id);
CREATE INDEX idx_users_active ON users(is_active);
CREATE INDEX idx_users_frozen_until ON users(frozen_until) WHERE frozen_until IS NOT NULL;

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
  is_admin       boolean NOT NULL DEFAULT false,
  created_at     timestamptz NOT NULL DEFAULT now(),
  updated_at     timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_auth_users_user_id ON auth_users(user_id);

CREATE TRIGGER trg_auth_users_updated_at
BEFORE UPDATE ON auth_users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

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

CREATE TABLE password_reset_tokens (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  token_hash  text NOT NULL UNIQUE,
  user_id     uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  expires_at  timestamptz NOT NULL,
  created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_password_reset_tokens_token_hash ON password_reset_tokens(token_hash);
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);

-- =========
-- 3) Category / Item (Catalog + Listing を初期は一本化)
-- =========
CREATE TABLE categories (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  parent_id    uuid NULL REFERENCES categories(id) ON DELETE SET NULL,
  name        text NOT NULL,
  code        text NULL,
  sort_order   integer NOT NULL DEFAULT 0,
  is_active    boolean NOT NULL DEFAULT true,
  created_at  timestamptz NOT NULL DEFAULT now(),
  updated_at  timestamptz NOT NULL DEFAULT now(),
  UNIQUE (parent_id, name)
);

CREATE INDEX idx_categories_parent ON categories(parent_id);
CREATE UNIQUE INDEX uq_categories_code ON categories(code) WHERE code IS NOT NULL;
CREATE INDEX idx_categories_active ON categories(is_active);
CREATE INDEX idx_categories_parent_sort ON categories(parent_id, sort_order);

CREATE TRIGGER trg_categories_updated_at
BEFORE UPDATE ON categories
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- items.status: 'DRAFT','PUBLISHED','IN_TRADE','SOLD','SUSPENDED','DELETED'
-- items.condition: 'NEW','LIKE_NEW','USED_GOOD','USED_FAIR','USED_POOR'
-- shipping_fee_payer: 'SELLER','BUYER'
CREATE TABLE items (
  id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
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
  search_tsv          tsvector GENERATED ALWAYS AS (
    setweight(to_tsvector('simple', coalesce(name,'')), 'A')
    || setweight(to_tsvector('simple', coalesce(description,'')), 'B')
  ) STORED,
  created_at          timestamptz NOT NULL DEFAULT now(),
  updated_at          timestamptz NOT NULL DEFAULT now(),
  CHECK (copied_from_item_id IS NULL OR copied_from_item_id <> id)
);

CREATE INDEX idx_items_status_created          ON items(status, created_at DESC);
CREATE INDEX idx_items_category_status_created ON items(category_id, status, created_at DESC);
CREATE INDEX idx_items_seller_created          ON items(seller_id, created_at DESC);
CREATE INDEX idx_items_copied_from             ON items(copied_from_item_id);
CREATE INDEX idx_items_search_tsv              ON items USING GIN (search_tsv);
CREATE INDEX idx_items_name_trgm               ON items USING GIN (name gin_trgm_ops);

CREATE TRIGGER trg_items_updated_at
BEFORE UPDATE ON items
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE item_images (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  item_id        uuid NOT NULL REFERENCES items(id) ON DELETE CASCADE,
  s3_key         text NOT NULL,
  content_type   text NULL,
  byte_size      bigint NULL CHECK (byte_size IS NULL OR byte_size >= 0),
  display_order  smallint NOT NULL DEFAULT 0 CHECK (display_order >= 0),
  created_at     timestamptz NOT NULL DEFAULT now(),
  UNIQUE (item_id, display_order),
  UNIQUE (item_id, s3_key)
);

CREATE INDEX idx_item_images_item_order ON item_images(item_id, display_order);

-- =========
-- 4) Transaction / Chat / Review
-- =========
CREATE TABLE orders (
  id                       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  item_id                   uuid NOT NULL UNIQUE REFERENCES items(id),
  buyer_id                  uuid NOT NULL REFERENCES users(id),
  seller_id                 uuid NOT NULL REFERENCES users(id),
  stripe_payment_intent_id  text NULL UNIQUE,
  status                   text NOT NULL CHECK (status IN ('PAID','AWAITING_SHIPMENT','SHIPPED','COMPLETED','CANCELLED')),
  applied_commission_bps   integer NOT NULL CHECK (applied_commission_bps >= 0 AND applied_commission_bps <= 10000),
  item_price_amount        bigint NOT NULL CHECK (item_price_amount >= 0),
  shipping_fee_amount      bigint NOT NULL DEFAULT 0 CHECK (shipping_fee_amount >= 0),
  total_amount             bigint NOT NULL CHECK (total_amount >= 0),
  currency                 char(3) NOT NULL DEFAULT 'JPY',
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
  ip_address    text NULL,
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

-- =========
-- 6) Prohibited rules (V5)
-- =========
CREATE TABLE prohibited_categories (
  category_id uuid PRIMARY KEY REFERENCES categories(id) ON DELETE CASCADE,
  reason      text NOT NULL DEFAULT '',
  created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE prohibited_terms (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  term        text NOT NULL UNIQUE,
  reason      text NOT NULL DEFAULT '',
  severity    smallint NOT NULL DEFAULT 1 CHECK (severity BETWEEN 1 AND 5),
  created_at  timestamptz NOT NULL DEFAULT now()
);

-- =========
-- 7) Favorites (V8)
-- =========
CREATE TABLE favorites (
  id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id    uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  item_id    uuid NOT NULL REFERENCES items(id) ON DELETE CASCADE,
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE (user_id, item_id)
);

CREATE INDEX idx_favorites_user_created ON favorites(user_id, created_at DESC);
CREATE INDEX idx_favorites_item ON favorites(item_id);

-- =========
-- 8) Board posts (V9)
-- =========
CREATE TABLE board_posts (
  id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  item_id    uuid NOT NULL REFERENCES items(id) ON DELETE CASCADE,
  author_id  uuid NOT NULL REFERENCES users(id),
  content    text NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_board_posts_item_created ON board_posts(item_id, created_at DESC);

-- =========
-- 9) Item views (V10)
-- =========
CREATE TABLE item_views (
  id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id    uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  item_id    uuid NOT NULL REFERENCES items(id) ON DELETE CASCADE,
  viewed_at  timestamptz NOT NULL DEFAULT now(),
  UNIQUE (user_id, item_id)
);

CREATE INDEX idx_item_views_item_id ON item_views(item_id);
CREATE INDEX idx_item_views_user_item ON item_views(user_id, item_id);

-- =========
-- 10) Item moderation (V13)
-- =========
CREATE TABLE item_moderation (
  id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  item_id             uuid NOT NULL REFERENCES items(id) ON DELETE CASCADE,
  rejected            boolean NOT NULL,
  text_flagged        boolean,
  text_negative_score double precision,
  image_adult         boolean,
  image_violence      boolean,
  image_risk_score    double precision,
  created_at          timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_item_moderation_rejected_created ON item_moderation(rejected, created_at DESC);

COMMIT;
