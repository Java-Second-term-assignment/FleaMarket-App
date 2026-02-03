-- audit_logs.ip_address を inet から text に変更
-- Java の String 型と互換性を持たせる（Hibernate が inet を直接マッピングしないため）
ALTER TABLE audit_logs
  ALTER COLUMN ip_address TYPE text USING (ip_address::text);
