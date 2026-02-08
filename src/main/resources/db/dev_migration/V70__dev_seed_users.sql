-- V70__dev_seed_users.sql
-- dev プロファイルのみ適用（db/dev_migration は dev でのみ読み込まれる）
-- 開発/検証用の admin・テストユーザーを用意

-- users（固定UUIDで安定化・新規登録必須項目まで含む）
INSERT INTO users (id, display_name, user_rank_id, identity_status, is_active, recipient_name, recipient_name_furigana, postal_code, address, phone, date_of_birth, gender)
VALUES
  ('30000000-0000-0000-0000-000000000001'::uuid, 'テストユーザー A', 1, 'VERIFIED',   true, '山田 一郎',   'ヤマダ イチロウ',   '100-0001', '東京都千代田区千代田1-1',     '090-1111-1111', '1990-01-15', 'MALE'),
  ('30000000-0000-0000-0000-000000000002'::uuid, 'テストユーザー B', 1, 'UNVERIFIED', true, '鈴木 花子',   'スズキ ハナコ',    '530-0001', '大阪府大阪市北区梅田1-1',     '080-2222-2222', '1995-05-20', 'FEMALE'),
  ('30000000-0000-0000-0000-000000000003'::uuid, '運営者C', 3, 'VERIFIED',   true, '管理者 太郎', 'カンリシャ タロウ', '150-0000', '東京都渋谷区神宮前1-1',       '070-3333-3333', '1988-12-01', 'MALE')
ON CONFLICT (id) DO UPDATE
SET
  display_name            = EXCLUDED.display_name,
  user_rank_id             = EXCLUDED.user_rank_id,
  identity_status          = EXCLUDED.identity_status,
  is_active                = EXCLUDED.is_active,
  recipient_name           = EXCLUDED.recipient_name,
  recipient_name_furigana  = EXCLUDED.recipient_name_furigana,
  postal_code              = EXCLUDED.postal_code,
  address                  = EXCLUDED.address,
  phone                    = EXCLUDED.phone,
  date_of_birth            = EXCLUDED.date_of_birth,
  gender                   = EXCLUDED.gender,
  updated_at               = now();

-- auth_users（user_id を軸にUpsert：安全）
INSERT INTO auth_users (user_id, email, password_hash, is_admin)
VALUES
  ('30000000-0000-0000-0000-000000000001'::uuid, 'sellerA@example.com', crypt('password',  gen_salt('bf')), false),
  ('30000000-0000-0000-0000-000000000002'::uuid, 'buyerB@example.com',  crypt('password',  gen_salt('bf')), false),
  ('30000000-0000-0000-0000-000000000003'::uuid, 'adminC@example.com',  crypt('password',  gen_salt('bf')), true)
ON CONFLICT (user_id) DO UPDATE
SET
  email         = EXCLUDED.email,
  password_hash = EXCLUDED.password_hash,
  is_admin      = EXCLUDED.is_admin,
  updated_at    = now();
