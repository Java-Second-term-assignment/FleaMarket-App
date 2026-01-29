-- V9__board_posts.sql
-- 商品ごとの掲示板投稿（board_id = item_id）

BEGIN;

CREATE TABLE board_posts (
  id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  item_id    uuid NOT NULL REFERENCES items(id) ON DELETE CASCADE,
  author_id  uuid NOT NULL REFERENCES users(id),
  content    text NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_board_posts_item_created ON board_posts(item_id, created_at DESC);

COMMIT;
