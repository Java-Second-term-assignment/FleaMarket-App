-- V5__prohibited_rules.sql
-- 禁止ルール（最小）：カテゴリ単位・キーワード単位

CREATE TABLE IF NOT EXISTS prohibited_categories (
  category_id uuid PRIMARY KEY REFERENCES categories(id) ON DELETE CASCADE,
  reason      text NOT NULL DEFAULT '',
  created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS prohibited_terms (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  term        text NOT NULL UNIQUE,
  reason      text NOT NULL DEFAULT '',
  severity    smallint NOT NULL DEFAULT 1 CHECK (severity BETWEEN 1 AND 5),
  created_at  timestamptz NOT NULL DEFAULT now()
);

-- 取扱禁止カテゴリ配下を “カテゴリとしては禁止” に登録（存在すれば）
INSERT INTO prohibited_categories (category_id, reason)
SELECT c.id, '取扱禁止カテゴリ'
FROM categories c
JOIN categories p ON p.id = c.parent_id
WHERE p.name = '取扱禁止'
ON CONFLICT (category_id) DO NOTHING;

-- キーワード禁止（例：最低限だけ。運用で増やす）
INSERT INTO prohibited_terms (term, reason, severity)
VALUES
  ('火薬', '危険物', 5),
  ('銃', '武器', 5),
  ('弾薬', '武器', 5),
  ('麻薬', '違法薬物', 5)
ON CONFLICT (term) DO NOTHING;
