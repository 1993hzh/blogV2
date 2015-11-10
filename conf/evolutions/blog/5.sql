# --- !Ups

CREATE TABLE T_COMMENT (
  id          SERIAL  NOT NULL PRIMARY KEY,
  content     VARCHAR(255),
  from_id     INTEGER NOT NULL,
  from_name   VARCHAR(255) NOT NULL,
  status      VARCHAR(255) NOT NULL DEFAULT 'UNREAD',
  to_id       INTEGER,
  to_name     VARCHAR(255),
  createTime  TIMESTAMP DEFAULT current_timestamp,
  passage_id  INTEGER NOT NULL,
  FOREIGN KEY (from_id) REFERENCES T_USER (id),
  FOREIGN KEY (to_id) REFERENCES T_USER (id),
  FOREIGN KEY (passage_id) REFERENCES T_PASSAGE (id) ON DELETE CASCADE
);

# --- !Downs

DROP TABLE T_COMMENT CASCADE;