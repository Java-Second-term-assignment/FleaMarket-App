-- V4: 通知を受け取る設定（ユーザー設定画面のチェックボックス用）
ALTER TABLE users ADD COLUMN notification_enabled boolean NOT NULL DEFAULT true;
