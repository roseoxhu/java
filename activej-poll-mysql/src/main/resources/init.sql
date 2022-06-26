CREATE TABLE `poll` IF NOT EXISTS (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '标识',
  `title` varchar(50) DEFAULT NULL COMMENT '标题',
  `message` varchar(255) DEFAULT NULL COMMENT '内容',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='投票';

CREATE TABLE `poll_vote` IF NOT EXISTS (
  `poll_id` int(11) NOT NULL DEFAULT '0' COMMENT '标识=poll.id',
  `option` varchar(50) DEFAULT NULL COMMENT '选项',
  `votes_count` int(11) DEFAULT '0' COMMENT '票数',
  `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='投票计数';
