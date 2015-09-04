CREATE TABLE IF NOT EXISTS PlayerLogin(
  instant TIMESTAMP,
  id VARCHAR(36),
  name VARCHAR(16),
  PRIMARY KEY(id,instant)
);