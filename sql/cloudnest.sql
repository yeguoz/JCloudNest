CREATE DATABASE IF NOT EXISTS `cloudnest`;
USE `cloudnest`;

CREATE TABLE IF NOT EXISTS `cn_files` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `size` bigint(20) DEFAULT NULL,
  `file_hash` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `header_hash` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `source_name` text CHARACTER SET utf8 COLLATE utf8_unicode_ci,
  `reference_count` int(11) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `size_file_hash_header_hash` (`size`,`file_hash`,`header_hash`),
  KEY `file_hash` (`file_hash`),
  FULLTEXT KEY `source_name` (`source_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `cn_folders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_parent_id` (`name`,`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


INSERT INTO `cn_folders` (`id`, `user_id`, `name`, `parent_id`, `created_at`, `updated_at`, `deleted_at`) VALUES
	(1, 1, '/', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);

CREATE TABLE IF NOT EXISTS `cn_groups` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `policy_id` int(11) DEFAULT NULL,
  `max_storage` bigint(20) DEFAULT NULL,
  `share_enabled` tinyint(4) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `cn_groups` (`id`, `name`, `policy_id`, `max_storage`, `share_enabled`, `created_at`, `updated_at`, `deleted_at`) VALUES
	(1, 'Admin', 1, 1073741824, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
	(2, 'User', 1, 1073741824, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);

CREATE TABLE IF NOT EXISTS `cn_policies` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `type` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `file_dir_name_rule` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `file_name_rule` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `avatar_file_name_rule` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `chunk_dir_name_rule` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `chunk_file_name_rule` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `empty_file_name_rule` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `cn_policies` (`id`, `name`, `type`, `file_dir_name_rule`, `file_name_rule`, `avatar_file_name_rule`, `chunk_dir_name_rule`, `chunk_file_name_rule`, `empty_file_name_rule`, `created_at`, `updated_at`, `deleted_at`) VALUES
	(1, 'Default', 'local', 'storage/files/{hash2}', '{hash}', 'storage/avatar/{uid}/{uuid}', 'storage/temp/{uploadId}/{fingerprint}', '{index}.part', 'storage/empty/{uid}/{uuid}_{filename}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);

CREATE TABLE IF NOT EXISTS `cn_settings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `value` text COLLATE utf8_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `cn_settings` (`id`, `type`, `name`, `value`, `created_at`, `updated_at`, `deleted_at`) VALUES
	(1, 'auth', 'registerEnabled', 'false', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
	(2, 'auth', 'registerGroup', 'User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
	(3, 'auth', 'registerCaptcha', 'true', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
	(4, 'auth', 'loginCaptcha', 'true', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
	(5, 'mail', 'host', '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
	(6, 'mail', 'port', '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
	(7, 'mail', 'username', '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
	(8, 'mail', 'password', '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
	(9, 'mail', 'registerTemplate', '<h1>欢迎使用Cloudnest</h1><p>这是一封 <b>激活邮件</b> 邮件</p><p>点击链接激活：${link}</p><p>如果不是你本人操作，请忽略此邮件</p>', '2025-04-29 17:22:50', '2025-05-04 17:25:18', NULL),
	(10, 'mail', 'personal', 'Cloudnest', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
	(11, 'mail', 'registerSubject', 'Cloudnest 注册', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
	(12, 'site', 'url', '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
	(13, 'mail', 'forgetTemplate', '<h1>忘记密码</h1><p>这是一封 <b>忘记密码</b> 邮件</p><p>点击链接重置密码：${link}</p><p>如果不是你本人操作，请忽略此邮件</p>', '2025-05-02 18:35:24', '2025-05-04 18:17:17', NULL),
	(14, 'mail', 'forgetSubject', 'Cloudnest 忘记密码', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);

CREATE TABLE IF NOT EXISTS `cn_shares` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `source_id` bigint(20) DEFAULT NULL,
  `user_file_id` bigint(20) DEFAULT NULL,
  `short_id` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `source_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `password_enabled` tinyint(4) DEFAULT NULL,
  `password` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `is_dir` tinyint(4) DEFAULT NULL,
  `visit_count` int(11) DEFAULT NULL,
  `remaining_downloads` int(11) DEFAULT NULL,
  `preview_enabled` tinyint(4) DEFAULT NULL,
  `expire_time_enabled` tinyint(4) DEFAULT NULL,
  `expire_time` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `expire_time_deleted_at` (`expire_time`,`deleted_at`),
  KEY `short_id` (`short_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE IF NOT EXISTS `cn_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_id` int(11) DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `status` tinyint(4) DEFAULT NULL COMMENT '0 正常',
  `avatar` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `used_storage` bigint(20) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `cn_users` (`id`, `group_id`, `name`, `password`, `email`, `status`, `avatar`, `used_storage`, `created_at`, `updated_at`, `deleted_at`) VALUES
	(1, 1, 'admin', '$2a$12$cjtsAdPo26M.r6efDV2iJu7JH28dMgHN93JJTPG0RbQmk0Vr.KH6m', 'admin@cloudnest.org', 0, '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);

CREATE TABLE IF NOT EXISTS `cn_user_files` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `file_id` bigint(20) DEFAULT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `folder_id` bigint(20) DEFAULT NULL,
  `file_type` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `file_id_file_name_folder_id` (`file_id`,`file_name`,`folder_id`),
  KEY `user_id` (`user_id`),
  KEY `folder_id` (`folder_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
