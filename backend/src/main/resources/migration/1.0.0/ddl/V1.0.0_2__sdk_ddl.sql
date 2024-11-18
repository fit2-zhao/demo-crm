-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS operation_log
(
    `id`              VARCHAR(50)  NOT NULL COMMENT 'ID',
    `project_id`      VARCHAR(50)  NOT NULL DEFAULT 'NONE' COMMENT '项目id',
    `organization_id` VARCHAR(50)  NOT NULL DEFAULT 'NONE' COMMENT '组织id',
    `create_time`     BIGINT       NOT NULL COMMENT '操作时间',
    `create_user`     VARCHAR(50) COMMENT '操作人',
    `source_id`       VARCHAR(50) COMMENT '资源id',
    `method`          VARCHAR(255) NOT NULL COMMENT '操作方法',
    `type`            VARCHAR(20)  NOT NULL COMMENT '操作类型/add/update/delete',
    `module`          VARCHAR(50) COMMENT '操作模块/api/case/scenario/ui',
    `content`         VARCHAR(500) COMMENT '操作详情',
    `path`            VARCHAR(255) COMMENT '操作路径',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '操作日志';

CREATE INDEX idx_create_time ON operation_log (create_time desc);
CREATE INDEX idx_create_user ON operation_log (create_user);
CREATE INDEX idx_method ON operation_log (method);
CREATE INDEX idx_module ON operation_log (module);
CREATE INDEX idx_project_id ON operation_log (project_id);
CREATE INDEX idx_type ON operation_log (type);
CREATE INDEX idx_organization_id ON operation_log (organization_id);
CREATE INDEX idx_source_id ON operation_log (source_id);


CREATE TABLE IF NOT EXISTS operation_log_blob
(
    `id`             VARCHAR(50) NOT NULL COMMENT '主键,与operation_log表id一致',
    `original_value` LONGBLOB COMMENT '变更前内容',
    `modified_value` LONGBLOB COMMENT '变更后内容',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '操作日志内容详情';


CREATE TABLE IF NOT EXISTS worker_node
(
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'auto increment id',
    host_name   VARCHAR(64) NOT NULL COMMENT 'host name',
    port        VARCHAR(64) NOT NULL COMMENT 'port',
    type        INT         NOT NULL COMMENT 'node type: ACTUAL or CONTAINER',
    launch_date BIGINT      NOT NULL COMMENT 'launch date',
    modified    BIGINT      NOT NULL COMMENT 'modified time',
    created     BIGINT      NOT NULL COMMENT 'created time',
    PRIMARY KEY (ID)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = 'DB WorkerID Assigner for UID Generator';

CREATE TABLE IF NOT EXISTS `schedule`
(
    `id`            varchar(50) COLLATE utf8mb4_general_ci  NOT NULL,
    `key`           varchar(50) COLLATE utf8mb4_general_ci           DEFAULT NULL COMMENT 'qrtz UUID',
    `type`          varchar(50) COLLATE utf8mb4_general_ci  NOT NULL COMMENT '执行类型 cron',
    `value`         varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'cron 表达式',
    `job`           varchar(64) COLLATE utf8mb4_general_ci  NOT NULL COMMENT 'Schedule Job Class Name',
    `resource_type` varchar(50) COLLATE utf8mb4_general_ci  NOT NULL DEFAULT 'NONE' COMMENT '资源类型 API_IMPORT,API_SCENARIO,UI_SCENARIO,LOAD_TEST,TEST_PLAN,CLEAN_REPORT,BUG_SYNC',
    `enable`        bit(1)                                           DEFAULT NULL COMMENT '是否开启',
    `resource_id`   varchar(50) COLLATE utf8mb4_general_ci           DEFAULT NULL COMMENT '资源ID，api_scenario ui_scenario load_test',
    `create_user`   varchar(50) COLLATE utf8mb4_general_ci  NOT NULL COMMENT '创建人',
    `create_time`   bigint                                  NOT NULL COMMENT '创建时间',
    `update_time`   bigint                                  NOT NULL COMMENT '更新时间',
    `project_id`    varchar(50) COLLATE utf8mb4_general_ci           DEFAULT NULL COMMENT '项目ID',
    `name`          varchar(255) COLLATE utf8mb4_general_ci          DEFAULT NULL COMMENT '名称',
    `config`        varchar(1000) COLLATE utf8mb4_general_ci         DEFAULT NULL COMMENT '配置',
    `num`           bigint                                  NOT NULL COMMENT '业务ID',
    PRIMARY KEY (`id`),
    KEY `idx_resource_id` (`resource_id`),
    KEY `idx_create_user` (`create_user`),
    KEY `idx_create_time` (`create_time` DESC),
    KEY `idx_update_time` (`update_time` DESC),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_enable` (`enable`),
    KEY `idx_name` (`name`),
    KEY `idx_type` (`type`),
    KEY `idx_resource_type` (`resource_type`),
    KEY `idx_num` (`num`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='定时任务';

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
