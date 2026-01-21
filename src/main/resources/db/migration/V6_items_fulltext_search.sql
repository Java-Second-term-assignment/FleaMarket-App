-- V6__items_fulltext_search.sql
-- items の全文検索用インデックス（日本語形態素は後で拡張。まずは標準で始める）

-- まずは name + description をまとめた tsvector を用意
ALTER TABLE items
  ADD COLUMN IF NOT EXISTS search_tsv tsvector
  GENERATED ALWAYS AS (
    setweight(to_tsvector('simple', coalesce(name,'')), 'A')
    || setweight(to_tsvector('simple', coalesce(description,'')), 'B')
  ) STORED;

-- GIN index
CREATE INDEX IF NOT EXISTS idx_items_search_tsv ON items USING GIN (search_tsv);

-- 併用でLIKE検索を軽くするなら trigram（任意）
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_items_name_trgm ON items USING GIN (name gin_trgm_ops);
