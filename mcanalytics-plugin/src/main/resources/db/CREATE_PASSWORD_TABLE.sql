CREATE TABLE IF NOT EXISTS Password(
  user_name VARCHAR(255) PRIMARY KEY,
  password_hash BINARY(64),
  salt BINARY(8)
);