CREATE TABLE IF NOT EXISTS WorldChange(
  instant TIMESTAMP,
  fromId VARCHAR(36),
  toId VARCHAR(36),
  fromName VARCHAR(255),
  toName VARCHAR(255),
  playerName VARCHAR(16),
  playerId VARCHAR(36),
  PRIMARY KEY(playerId, instant)
);