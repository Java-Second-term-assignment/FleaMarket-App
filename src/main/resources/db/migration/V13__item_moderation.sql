-- V13__item_moderation.sql
-- AIモデレーション判定結果（違反と判定されたもののみ保存）

CREATE TABLE IF NOT EXISTS item_moderation (
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
