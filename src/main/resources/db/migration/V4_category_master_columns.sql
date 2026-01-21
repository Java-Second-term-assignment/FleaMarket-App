-- V4__category_master_columns.sql
-- categories を運用マスタとして扱うための列追加

ALTER TABLE categories
  ADD COLUMN IF NOT EXISTS code text,
  ADD COLUMN IF NOT EXISTS sort_order integer NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS is_active boolean NOT NULL DEFAULT true;

-- code はルート配下で一意、など運用を決めやすい。ここでは "全体一意" にしておく（後々楽）
CREATE UNIQUE INDEX IF NOT EXISTS uq_categories_code ON categories(code) WHERE code IS NOT NULL;

-- 取得パターンに効く
CREATE INDEX IF NOT EXISTS idx_categories_active ON categories(is_active);
CREATE INDEX IF NOT EXISTS idx_categories_parent_sort ON categories(parent_id, sort_order);

-- 既存データへの暫定コード付与（固定UUID運用なら deterministic にできる）
-- ルート
UPDATE categories SET code='FASHION'     WHERE parent_id IS NULL AND name='ファッション' AND code IS NULL;
UPDATE categories SET code='FUN'        WHERE parent_id IS NULL AND name='娯楽'       AND code IS NULL;
UPDATE categories SET code='BOOKS'      WHERE parent_id IS NULL AND name='本'         AND code IS NULL;
UPDATE categories SET code='FURNITURE'  WHERE parent_id IS NULL AND name='家具'       AND code IS NULL;
UPDATE categories SET code='LIFE'       WHERE parent_id IS NULL AND name='生活用品'   AND code IS NULL;
UPDATE categories SET code='GOODS'      WHERE parent_id IS NULL AND name='雑貨'       AND code IS NULL;
UPDATE categories SET code='PROHIBITED' WHERE parent_id IS NULL AND name='取扱禁止'   AND code IS NULL;
