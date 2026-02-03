-- V10__item_views.sql
-- 商品閲覧履歴（人気ランキング用）

CREATE TABLE item_views (
  id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id    uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  item_id    uuid NOT NULL REFERENCES items(id) ON DELETE CASCADE,
  viewed_at  timestamptz NOT NULL DEFAULT now(),

  UNIQUE (user_id, item_id)
);

CREATE INDEX idx_item_views_item_id ON item_views(item_id);
CREATE INDEX idx_item_views_user_item ON item_views(user_id, item_id);
