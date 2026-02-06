-- V80__test_seed_users.sql
-- 統合テスト用のユーザーを投入。V1, V2 の後に適用される。
-- password: "password" でログイン可能（BCrypt ハッシュ固定）

-- users（固定UUIDで安定化）
INSERT INTO users (id, display_name, user_rank_id, identity_status, is_active)
VALUES
  ('40000000-0000-0000-0000-000000000001'::uuid, 'TestUser', 1, 'VERIFIED', true),
  ('40000000-0000-0000-0000-000000000002'::uuid, 'TestAdmin', 1, 'VERIFIED', true)
ON CONFLICT (id) DO UPDATE
SET
  display_name     = EXCLUDED.display_name,
  user_rank_id     = EXCLUDED.user_rank_id,
  identity_status  = EXCLUDED.identity_status,
  is_active        = EXCLUDED.is_active,
  updated_at       = now();

-- auth_users（user_id を軸に Upsert）
-- crypt で固定 salt により "password" の BCrypt ハッシュを deterministically 生成
INSERT INTO auth_users (user_id, email, password_hash, is_admin)
VALUES
  ('40000000-0000-0000-0000-000000000001'::uuid, 'testuser@example.com', crypt('password', '$2a$10$N9qo8uLOickgx2ZMRZoMye'), false),
  ('40000000-0000-0000-0000-000000000002'::uuid, 'admin@example.com', crypt('password', '$2a$10$N9qo8uLOickgx2ZMRZoMye'), true)
ON CONFLICT (user_id) DO UPDATE
SET
  email         = EXCLUDED.email,
  password_hash = EXCLUDED.password_hash,
  is_admin      = EXCLUDED.is_admin,
  updated_at    = now();
