# --- !Ups

CREATE TABLE T_USER (
  id            SERIAL       NOT NULL PRIMARY KEY,
  userName      VARCHAR(255) NOT NULL UNIQUE,
  password      VARCHAR(255) NOT NULL,
  mail          VARCHAR(255) NOT NULL UNIQUE,
  lastLoginIp   VARCHAR(255),
  lastLoginTime TIMESTAMP,
  role_id       INTEGER,
  binding_id    VARCHAR(255) UNIQUE,
  FOREIGN KEY (role_id) REFERENCES T_Role (id)
);

# --- !Downs

DROP TABLE T_USER;