-- 既存DBで notification_enabled が無い場合のみ追加（V1 で作成したDBはスキップされる）
ALTER TABLE users ADD COLUMN IF NOT EXISTS notification_enabled boolean NOT NULL DEFAULT true;
