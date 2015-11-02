# --- !Ups

CREATE TABLE T_TAG (
  id          SERIAL       NOT NULL PRIMARY KEY,
  name        VARCHAR(255) NOT NULL UNIQUE,
  description VARCHAR(255)
);

# --- !Downs

DROP TABLE T_TAG CASCADE;