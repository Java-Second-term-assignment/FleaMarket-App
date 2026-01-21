-- V7__dev_seed_users.sql
-- ⚠️ dev / staging 限定で適用すること（本番では適用しない）
-- 目的：開発/検証用の admin・テストユーザーを用意

--運用アドバイス（重要だけど短く）
--V7は本番に流さない仕組みが必要（Flywayの環境別実行 or 別DBでのみ適用
--もし環境分離できないなら、V7は Flywayではなく data.sql に置くのが安全です

-- users（固定UUIDで安定化）
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

-- auth_users（user_id を軸にUpsert：安全）
INSERT INTO auth_users (user_id, email, password_hash, is_admin)
VALUES
  ('30000000-0000-0000-0000-000000000001'::uuid, 'sellerA@example.com', crypt('password',  gen_salt('bf')), false),
  ('30000000-0000-0000-0000-000000000002'::uuid, 'buyerB@example.com',  crypt('password',  gen_salt('bf')), false),
  ('30000000-0000-0000-0000-000000000003'::uuid, 'adminC@example.com',  crypt('adminpass', gen_salt('bf')), true)
ON CONFLICT (user_id) DO UPDATE
SET
  email         = EXCLUDED.email,
  password_hash = EXCLUDED.password_hash,
  is_admin      = EXCLUDED.is_admin,
  updated_at    = now();
