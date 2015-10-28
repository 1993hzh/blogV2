# --- !Ups

CREATE TABLE T_ROLE (
  id   SERIAL       NOT NULL PRIMARY KEY,
  roleType VARCHAR(255) NOT NULL,
  website VARCHAR(255)
);

# --- !Downs

DROP TABLE T_ROLE cascade;