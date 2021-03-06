# --- !Ups

CREATE TABLE T_KEYWORD (
  id         SERIAL       NOT NULL PRIMARY KEY,
  name       VARCHAR(255) NOT NULL,
  passage_id INTEGER      NOT NULL,
  FOREIGN KEY (passage_id) REFERENCES T_PASSAGE (id) ON DELETE CASCADE,
  UNIQUE (name, passage_id)
);

# --- !Downs

DROP TABLE T_KEYWORD CASCADE;