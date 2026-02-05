ALTER TABLE users ADD COLUMN caption VARCHAR(200) NULL;
./mvnw flyway:migrate