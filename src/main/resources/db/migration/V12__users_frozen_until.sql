-- users に凍結期限カラムを追加
-- NULL = 無期限、日時あり = その日時まで凍結
ALTER TABLE users ADD COLUMN frozen_until timestamptz NULL;
CREATE INDEX idx_users_frozen_until ON users(frozen_until) WHERE frozen_until IS NOT NULL;
