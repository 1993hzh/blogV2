# --- !Ups

CREATE TABLE TestModel (
  id   SERIAL       NOT NULL PRIMARY KEY,
  NAME VARCHAR(255) NOT NULL UNIQUE,
  description VARCHAR(255)
);

# --- !Downs

DROP TABLE TestModel;