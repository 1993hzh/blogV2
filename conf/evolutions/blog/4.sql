# --- !Ups

CREATE TABLE T_PASSAGE (
  id            SERIAL       NOT NULL PRIMARY KEY,
  author_id     INTEGER      NOT NULL,
  author_name   VARCHAR(255) NOT NULL,
  title         VARCHAR(255) NOT NULL,
  content       TEXT,
  createTime    TIMESTAMP DEFAULT current_timestamp,
  viewCount     INTEGER   DEFAULT 0,
  FOREIGN KEY (author_id) REFERENCES T_USER (id)
);

# --- !Downs

DROP TABLE T_PASSAGE CASCADE;