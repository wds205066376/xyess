CREATE TABLE IF NOT EXISTS `user_behavior` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `idle_id` int(11) NOT NULL COMMENT '商品ID',
  `behavior_type` tinyint(4) NOT NULL COMMENT '行为类型：1-浏览，2-收藏，3-购买',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_idle_id` (`idle_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为表'; 