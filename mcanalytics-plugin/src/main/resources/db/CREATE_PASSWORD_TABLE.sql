CREATE TABLE IF NOT EXISTS Password(
  user_name VARCHAR(255) PRIMARY KEY,
  password_hash CHAR(88),
  salt CHAR(12)
);