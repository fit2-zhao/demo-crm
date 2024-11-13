-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 初始化用户
INSERT INTO `user` (
    `id`,
    `name`,
    `email`,
    `password`,
    `enable`,
    `create_time`,
    `update_time`,
    `language`,
    `last_organization_id`,
    `phone`,
    `source`,
    `last_project_id`,
    `create_user`,
    `update_user`,
    `deleted`,
    `cft_token`
)
VALUES
    (
        'admin',
        'Administrator',
        'admin@demo.io',
        MD5('admin'),
         1,
        1716175907000,
        1729752373360,
        'zh-CN',
        '717345437786112',
        '',
        'LOCAL',
        '718255970852864',
        'admin',
        '731261131825152',
         0,
        '/sU3a0dtnm5Ykmyj8LfCjA=='
    );

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
