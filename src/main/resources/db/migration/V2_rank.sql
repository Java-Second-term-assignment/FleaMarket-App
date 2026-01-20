-- V2__rank.sql
-- ランクマスタの投入/更新（冪等Upsert）

INSERT INTO user_ranks (id, rank_code, rank_name, commission_bps)
VALUES
  (1, 'BRONZE', 'ブロンズ', 1000),
  (2, 'SILVER', 'シルバー', 800),
  (3, 'GOLD',   'ゴールド', 600)
ON CONFLICT (rank_code) DO UPDATE
SET
  rank_code      = EXCLUDED.rank_code,
  rank_name      = EXCLUDED.rank_name,
  commission_bps = EXCLUDED.commission_bps,
  updated_at     = now();