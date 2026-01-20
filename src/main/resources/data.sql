-- data.sql（開発用サンプルデータ / 冪等）
-- 前提：flywayを適用しない
-- 前提：pgcrypto が有効（gen_random_uuid不要、固定UUIDで安定運用）

-- =========================
-- 0) rank初期データ
-- =========================

INSERT INTO user_ranks (id, rank_code, rank_name, commission_bps)
VALUES
  (1, 'BRONZE', 'ブロンズ', 1000),
  (2, 'SILVER', 'シルバー', 800),
  (3, 'GOLD',   'ゴールド', 600)
ON CONFLICT (id) DO UPDATE
SET
  rank_code      = EXCLUDED.rank_code,
  rank_name      = EXCLUDED.rank_name,
  commission_bps = EXCLUDED.commission_bps,
  updated_at     = now();

-- =========================
-- 0) categories(カテゴリ)
-- ==========================

--ルートカテゴリ
INSERT INTO categories (id, parent_id, name)
VALUES
  ('10000000-0000-0000-0000-000000000001'::uuid, NULL, 'ファッション'),
  ('10000000-0000-0000-0000-000000000002'::uuid, NULL, '娯楽'),
  ('10000000-0000-0000-0000-000000000003'::uuid, NULL, '本'),
  ('10000000-0000-0000-0000-000000000004'::uuid, NULL, '家具'),
  ('10000000-0000-0000-0000-000000000005'::uuid, NULL, '生活用品'),
  ('10000000-0000-0000-0000-000000000006'::uuid, NULL, '雑貨'),
  ('10000000-0000-0000-0000-000000000099'::uuid, NULL, '取扱禁止')
ON CONFLICT (id) DO NOTHING;

-- ファッションカテゴリ
INSERT INTO categories (id, parent_id, name)
VALUES
  ('20000000-0000-0000-0000-000000000101'::uuid, '10000000-0000-0000-0000-000000000001'::uuid, 'メンズ'),
  ('20000000-0000-0000-0000-000000000102'::uuid, '10000000-0000-0000-0000-000000000001'::uuid, 'レディース'),
  ('20000000-0000-0000-0000-000000000103'::uuid, '10000000-0000-0000-0000-000000000001'::uuid, 'キッズ')
ON CONFLICT (id) DO NOTHING;

-- 娯楽カテゴリ
INSERT INTO categories (id, parent_id, name)
VALUES
  ('20000000-0000-0000-0000-000000000201'::uuid, '10000000-0000-0000-0000-000000000002'::uuid, 'スマホ'),
  ('20000000-0000-0000-0000-000000000202'::uuid, '10000000-0000-0000-0000-000000000002'::uuid, 'ゲーム'),
  ('20000000-0000-0000-0000-000000000203'::uuid, '10000000-0000-0000-0000-000000000002'::uuid, 'CD・DVD')
ON CONFLICT (id) DO NOTHING;

-- 本カテゴリ
INSERT INTO categories (id, parent_id, name)
VALUES
  ('20000000-0000-0000-0000-000000000301'::uuid, '10000000-0000-0000-0000-000000000003'::uuid, '本'),
  ('20000000-0000-0000-0000-000000000302'::uuid, '10000000-0000-0000-0000-000000000003'::uuid, '漫画'),
  ('20000000-0000-0000-0000-000000000303'::uuid, '10000000-0000-0000-0000-000000000003'::uuid, '雑誌')
ON CONFLICT (id) DO NOTHING;

-- 家具カテゴリ
INSERT INTO categories (id, parent_id, name)
VALUES
  ('20000000-0000-0000-0000-000000000401'::uuid, '10000000-0000-0000-0000-000000000004'::uuid, '家電'),
  ('20000000-0000-0000-0000-000000000402'::uuid, '10000000-0000-0000-0000-000000000004'::uuid, '椅子'),
  ('20000000-0000-0000-0000-000000000403'::uuid, '10000000-0000-0000-0000-000000000004'::uuid, 'テーブル'),
  ('20000000-0000-0000-0000-000000000404'::uuid, '10000000-0000-0000-0000-000000000004'::uuid, '寝具'),
  ('20000000-0000-0000-0000-000000000405'::uuid, '10000000-0000-0000-0000-000000000004'::uuid, '照明'),
  ('20000000-0000-0000-0000-000000000406'::uuid, '10000000-0000-0000-0000-000000000004'::uuid, '収納'),
  ('20000000-0000-0000-0000-000000000407'::uuid, '10000000-0000-0000-0000-000000000004'::uuid, '電池')
ON CONFLICT (id) DO NOTHING;

-- 生活用品カテゴリ
INSERT INTO categories (id, parent_id, name)
VALUES
  ('20000000-0000-0000-0000-000000000501'::uuid, '10000000-0000-0000-0000-000000000005'::uuid, '文房具'),
  ('20000000-0000-0000-0000-000000000502'::uuid, '10000000-0000-0000-0000-000000000005'::uuid, '掃除用具'),
  ('20000000-0000-0000-0000-000000000503'::uuid, '10000000-0000-0000-0000-000000000005'::uuid, 'バス用品'),
  ('20000000-0000-0000-0000-000000000504'::uuid, '10000000-0000-0000-0000-000000000005'::uuid, 'ペット用品')
ON CONFLICT (id) DO NOTHING;

-- 雑貨カテゴリ
INSERT INTO categories (id, parent_id, name)
VALUES
  ('20000000-0000-0000-0000-000000000601'::uuid, '10000000-0000-0000-0000-000000000006'::uuid, 'スポーツ'),
  ('20000000-0000-0000-0000-000000000602'::uuid, '10000000-0000-0000-0000-000000000006'::uuid, 'アウトドア'),
  ('20000000-0000-0000-0000-000000000603'::uuid, '10000000-0000-0000-0000-000000000006'::uuid, 'ガーデニング'),
  ('20000000-0000-0000-0000-000000000604'::uuid, '10000000-0000-0000-0000-000000000006'::uuid, 'DIY')
ON CONFLICT (id) DO NOTHING;

-- 取り扱い禁止カテゴリ
INSERT INTO categories (id, parent_id, name)
VALUES
  ('20000000-0000-0000-0000-000000000701'::uuid, '10000000-0000-0000-0000-000000000099'::uuid, '食品全般'),
  ('20000000-0000-0000-0000-000000000702'::uuid, '10000000-0000-0000-0000-000000000099'::uuid, '火薬'),
  ('20000000-0000-0000-0000-000000000703'::uuid, '10000000-0000-0000-0000-000000000099'::uuid, '銃火器'),
  ('20000000-0000-0000-0000-000000000704'::uuid, '10000000-0000-0000-0000-000000000099'::uuid, '医薬品')
ON CONFLICT (id) DO NOTHING;




-- =========================
-- 1) users（業務ユーザー）固定UUID
-- =========================
INSERT INTO users (id, display_name, user_rank_id, identity_status, is_active)
VALUES
  ('30000000-0000-0000-0000-000000000001'::uuid, '出品者A', 1, 'VERIFIED',   true),
  ('30000000-0000-0000-0000-000000000002'::uuid, '購入者B', 1, 'UNVERIFIED', true),
  ('30000000-0000-0000-0000-000000000003'::uuid, '運営者C', 3, 'VERIFIED',   true)
ON CONFLICT (id) DO UPDATE
SET
  display_name     = EXCLUDED.display_name,
  user_rank_id     = EXCLUDED.user_rank_id,
  identity_status  = EXCLUDED.identity_status,
  is_active        = EXCLUDED.is_active,
  updated_at       = now();

-- =========================
-- 2) auth_users（認証） email UNIQUE を軸にUpsert
-- =========================
INSERT INTO auth_users (user_id, email, password_hash, is_admin)
VALUES
  ('30000000-0000-0000-0000-000000000001'::uuid, 'sellerA@example.com', crypt('password',  gen_salt('bf')), false),
  ('30000000-0000-0000-0000-000000000002'::uuid, 'buyerB@example.com',  crypt('password',  gen_salt('bf')), false),
  ('30000000-0000-0000-0000-000000000003'::uuid, 'adminC@example.com',  crypt('adminpass', gen_salt('bf')), true)
ON CONFLICT (email) DO UPDATE
SET
  user_id        = EXCLUDED.user_id,
  password_hash  = EXCLUDED.password_hash,
  is_admin       = EXCLUDED.is_admin,
  updated_at     = now();

-- =========================
-- 3) items（出品）固定UUID
-- category_id は V3 の固定UUIDを直参照
-- 例：
--   ルート「本」      = 10000000...0003
--   子「本（本配下）」= 20000000...0201
--   ルート「家具」    = 10000000...0004
--   子「家電」        = 20000000...0301
-- =========================
INSERT INTO items (
  id, copied_from_item_id, copy_source,
  seller_id, category_id,
  name, description,
  price_amount, currency,
  status, condition, shipping_fee_payer
)
VALUES
  (
    '40000000-0000-0000-0000-000000000001'::uuid,
    NULL, NULL,
    '30000000-0000-0000-0000-000000000001'::uuid,
    '20000000-0000-0000-0000-000000000301'::uuid, -- 本 > 本
    'Javaプログラミング入門',
    '初心者向けのJava入門書です。',
    1500, 'JPY',
    'PUBLISHED', 'USED_GOOD', 'BUYER'
  ),
  (
    '40000000-0000-0000-0000-000000000002'::uuid,
    NULL, NULL,
    '30000000-0000-0000-0000-000000000001'::uuid,
    '20000000-0000-0000-0000-000000000401'::uuid, -- 家具 > 家電
    'ワイヤレスイヤホン',
    'ノイズキャンセリング機能付き。',
    8000, 'JPY',
    'PUBLISHED', 'LIKE_NEW', 'SELLER'
  )
ON CONFLICT (id) DO UPDATE
SET
  seller_id          = EXCLUDED.seller_id,
  category_id        = EXCLUDED.category_id,
  name               = EXCLUDED.name,
  description        = EXCLUDED.description,
  price_amount       = EXCLUDED.price_amount,
  currency           = EXCLUDED.currency,
  status             = EXCLUDED.status,
  condition          = EXCLUDED.condition,
  shipping_fee_payer = EXCLUDED.shipping_fee_payer,
  updated_at         = now();

-- =========================
-- 4) item_images（画像はS3keyで保存)
-- =========================
INSERT INTO item_images (id, item_id, s3_key, content_type, byte_size, display_order)
VALUES
  ('50000000-0000-0000-0000-000000000001'::uuid, '40000000-0000-0000-0000-000000000001'::uuid, 'items/sample-java-book/cover.jpg', 'image/jpeg',123456,0),
  ('50000000-0000-0000-0000-000000000002'::uuid, '40000000-0000-0000-0000-000000000002'::uuid, 'items/sample-earbuds/1.jpg', 'image/jpeg',234567, 0),
  ('50000000-0000-0000-0000-000000000003'::uuid, '40000000-0000-0000-0000-000000000002'::uuid, 'items/sample-earbuds/2.jpg','image/jpeg',345678, 1)
ON CONFLICT (id) DO UPDATE
SET
  item_id       = EXCLUDED.item_id,
  s3_key        = EXCLUDED.s3_key,
  content_type  = EXCLUDED.content_type,
  byte_size     = EXCLUDED.byte_size,
  display_order = EXCLUDED.display_order;

-- =========================
-- 5) コピー出品サンプル（再出品）
-- copied_from_item_id を設定して新規IDで作る
-- =========================
INSERT INTO items (
  id, copied_from_item_id, copy_source,
  seller_id, category_id,
  name, description,
  price_amount, currency,
  status, condition, shipping_fee_payer
)
VALUES
  (
    '40000000-0000-0000-0000-000000000003'::uuid,
    '40000000-0000-0000-0000-000000000001'::uuid,
    'USER_COPY',
    '30000000-0000-0000-0000-000000000001'::uuid,
    '20000000-0000-0000-0000-000000000301'::uuid,
    'Javaプログラミング入門（再出品）',
    '初心者向けのJava入門書です。',
    1500, 'JPY',
    'DRAFT', 'USED_GOOD', 'BUYER'
  )
ON CONFLICT (id) DO UPDATE
SET
  copied_from_item_id = EXCLUDED.copied_from_item_id,
  copy_source         = EXCLUDED.copy_source,
  seller_id           = EXCLUDED.seller_id,
  category_id         = EXCLUDED.category_id,
  name                = EXCLUDED.name,
  description         = EXCLUDED.description,
  price_amount        = EXCLUDED.price_amount,
  currency            = EXCLUDED.currency,
  status              = EXCLUDED.status,
  condition           = EXCLUDED.condition,
  shipping_fee_payer  = EXCLUDED.shipping_fee_payer,
  updated_at          = now();
