# --- !Ups

CREATE TABLE T_ROLE (
  id       SERIAL       NOT NULL PRIMARY KEY,
  roleType VARCHAR(255) NOT NULL,
  website  VARCHAR(255),
  UNIQUE (roleType, website)
);

INSERT INTO T_ROLE(roletype, website) VALUES ('owner', 'www.huzhonghua.cn');
INSERT INTO T_ROLE(roletype, website) VALUES ('common', 'www.huzhonghua.cn');
INSERT INTO T_ROLE(roletype, website) VALUES ('3rd-party', 'weibo.com');

# --- !Downs

DROP TABLE T_ROLE CASCADE;