# --- !Ups

CREATE TABLE TestModel (
  id   SERIAL       NOT NULL PRIMARY KEY,
  NAME VARCHAR(255) NOT NULL UNIQUE
);

# --- !Downs

DROP TABLE TestModel;