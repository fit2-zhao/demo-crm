-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS user
(
    `id`                   VARCHAR(50)  NOT NULL COMMENT '用户ID',
    `name`                 VARCHAR(255) NOT NULL COMMENT '用户名',
    `email`                VARCHAR(64)  NOT NULL COMMENT '用户邮箱',
    `password`             VARCHAR(256) COLLATE utf8mb4_bin COMMENT '用户密码',
    `enable`               BIT          NOT NULL DEFAULT 1 COMMENT '是否启用',
    `create_time`          BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`          BIGINT       NOT NULL COMMENT '更新时间',
    `language`             VARCHAR(30) COMMENT '语言',
    `last_organization_id` VARCHAR(50) COMMENT '当前组织ID',
    `phone`                VARCHAR(50) COMMENT '手机号',
    `source`               VARCHAR(50)  NOT NULL COMMENT '来源：LOCAL OIDC CAS OAUTH2',
    `last_project_id`      VARCHAR(50) COMMENT '当前项目ID',
    `create_user`          VARCHAR(50)  NOT NULL COMMENT '创建人',
    `update_user`          VARCHAR(50)  NOT NULL COMMENT '修改人',
    `deleted`              BIT          NOT NULL DEFAULT 0 COMMENT '是否删除',
    `cft_token`            VARCHAR(255) COMMENT 'CFT Token',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户';



CREATE INDEX idx_name ON user (`name`);
CREATE UNIQUE INDEX idx_email ON user (`email`);
CREATE INDEX idx_create_time ON user (`create_time` desc);
CREATE INDEX idx_update_time ON user (`update_time` desc);
CREATE INDEX idx_organization_id ON user (`last_organization_id`);
CREATE INDEX idx_project_id ON user (`last_project_id`);
CREATE INDEX idx_create_user ON user (`create_user`);
CREATE INDEX idx_update_user ON user (`update_user`);
CREATE INDEX idx_deleted ON user (`deleted`);


CREATE TABLE IF NOT EXISTS user_extend
(
    `id`            VARCHAR(50) NOT NULL COMMENT '用户ID',
    `platform_info` BLOB COMMENT '其他平台对接信息',
    `avatar`        VARCHAR(255) COMMENT '头像',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户扩展';

CREATE TABLE IF NOT EXISTS `user_key`
(
    `id`          varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'user_key ID',
    `create_user` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
    `access_key`  varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'access_key',
    `secret_key`  varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'secret key',
    `create_time` bigint                                 NOT NULL COMMENT '创建时间',
    `enable`      bit(1)                                 NOT NULL DEFAULT b'1' COMMENT '状态',
    `forever`     bit(1)                                 NOT NULL DEFAULT b'1' COMMENT '是否永久有效',
    `expire_time` bigint                                          DEFAULT NULL COMMENT '到期时间',
    `description` varchar(1000) COLLATE utf8mb4_general_ci        DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_ak` (`access_key`),
    KEY `idx_create_user` (`create_user`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户api key';

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;


