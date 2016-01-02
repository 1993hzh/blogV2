# --- !Ups

CREATE TABLE T_FILE (
  id          SERIAL  NOT NULL PRIMARY KEY,
  passage_id  INTEGER NOT NULL,
  name        VARCHAR(255) NOT NULL,
  store_id     VARCHAR(255) NOT NULL,
  storeName   VARCHAR(255) NOT NULL,
  createTime  TIMESTAMP DEFAULT current_timestamp,
  fileType    VARCHAR(255) NOT NULL,
  FOREIGN KEY (passage_id) REFERENCES T_PASSAGE (id) ON DELETE CASCADE
);

# --- !Downs

DROP TABLE T_FILE CASCADE;