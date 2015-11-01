# --- !Ups

CREATE TABLE T_COMMENT (
  id         SERIAL  NOT NULL PRIMARY KEY,
  content    VARCHAR(255),
  from_id    INTEGER NOT NULL,
  to_id      INTEGER NOT NULL,
  time       TIMESTAMP DEFAULT current_timestamp,
  passage_id INTEGER NOT NULL,
  FOREIGN KEY (from_id) REFERENCES T_USER (id),
  FOREIGN KEY (to_id) REFERENCES T_USER (id),
  FOREIGN KEY (passage_id) REFERENCES T_PASSAGE (id)
);

# --- !Downs

DROP TABLE T_COMMENT CASCADE;