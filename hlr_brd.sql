/*
MySQL Data Transfer
Source Host: localhost
Source Database: hlr_brd
Target Host: localhost
Target Database: hlr_brd
Date: 1/24/2024 6:39:21 PM
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for batch_process
-- ----------------------------
DROP TABLE IF EXISTS `batch_process`;
CREATE TABLE `batch_process` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(20) NOT NULL,
  `batchid` bigint(20) unsigned DEFAULT '0',
  `server_id` int(1) NOT NULL DEFAULT '1',
  `total` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `server_id` (`server_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for hlr_deliver
-- ----------------------------
DROP TABLE IF EXISTS `hlr_deliver`;
CREATE TABLE `hlr_deliver` (
  `hlr_id` bigint(17) NOT NULL,
  `username` varchar(10) DEFAULT NULL,
  `time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `pdu` longtext,
  `destination` bigint(16) DEFAULT '0',
  `flag` tinyint(1) DEFAULT NULL,
  `system_type` varchar(10) DEFAULT 'BULK',
  PRIMARY KEY (`hlr_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for hlr_smsc
-- ----------------------------
DROP TABLE IF EXISTS `hlr_smsc`;
CREATE TABLE `hlr_smsc` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(5) DEFAULT NULL,
  `system_id` varchar(15) DEFAULT NULL,
  `password` varchar(50) DEFAULT NULL,
  `ip` varchar(30) DEFAULT NULL,
  `port` int(5) DEFAULT NULL,
  `system_type` varchar(10) DEFAULT NULL,
  `bindmode` char(2) DEFAULT NULL,
  `sleep` int(3) DEFAULT NULL,
  `bound` tinyint(1) NOT NULL DEFAULT '0',
  `group_id` int(3) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for lookup_result
-- ----------------------------
DROP TABLE IF EXISTS `lookup_result`;
CREATE TABLE `lookup_result` (
  `batch_id` bigint(20) unsigned DEFAULT '0',
  `hlr_id` bigint(17) NOT NULL,
  `resp_id` varchar(50) NOT NULL,
  `number` bigint(15) DEFAULT NULL,
  `network` varchar(6) DEFAULT NULL,
  `status` varchar(8) DEFAULT NULL,
  `createtime` timestamp NULL DEFAULT NULL,
  `statustime` timestamp NULL DEFAULT NULL,
  `errorcode` varchar(5) DEFAULT NULL,
  `error` varchar(50) DEFAULT NULL,
  `username` varchar(10) DEFAULT NULL,
  `cost` double(10,5) DEFAULT '0.00000',
  `system_type` varchar(10) DEFAULT 'BULK',
  `imsi` varchar(20) DEFAULT NULL,
  `msc` varchar(20) DEFAULT NULL,
  `smsc_id` int(2) DEFAULT '0',
  PRIMARY KEY (`hlr_id`),
  KEY `batch_index` (`batch_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for lookup_result_log
-- ----------------------------
DROP TABLE IF EXISTS `lookup_result_log`;
CREATE TABLE `lookup_result_log` (
  `batch_id` bigint(20) unsigned DEFAULT '0',
  `hlr_id` bigint(17) NOT NULL,
  `resp_id` varchar(50) NOT NULL,
  `number` bigint(15) DEFAULT NULL,
  `network` varchar(6) DEFAULT NULL,
  `status` varchar(8) DEFAULT NULL,
  `createtime` timestamp NULL DEFAULT NULL,
  `statustime` timestamp NULL DEFAULT NULL,
  `errorcode` varchar(5) DEFAULT NULL,
  `error` varchar(50) DEFAULT NULL,
  `username` varchar(10) DEFAULT NULL,
  `cost` double(10,5) DEFAULT '0.00000',
  `system_type` varchar(10) DEFAULT 'BULK',
  `imsi` varchar(20) DEFAULT NULL,
  `msc` varchar(20) DEFAULT NULL,
  `smsc_id` int(2) DEFAULT '0',
  PRIMARY KEY (`hlr_id`),
  KEY `batch_index` (`batch_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for mcs_status
-- ----------------------------
DROP TABLE IF EXISTS `mcs_status`;
CREATE TABLE `mcs_status` (
  `msg_id` varchar(30) NOT NULL,
  `destination` bigint(16) DEFAULT NULL,
  `flag` char(1) NOT NULL DEFAULT 'F',
  `remarks` varchar(100) DEFAULT NULL,
  `error` int(3) NOT NULL DEFAULT '0',
  PRIMARY KEY (`msg_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for msgbrd_smsc
-- ----------------------------
DROP TABLE IF EXISTS `msgbrd_smsc`;
CREATE TABLE `msgbrd_smsc` (
  `id` int(3) NOT NULL,
  `name` varchar(3) DEFAULT NULL,
  `key` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for request_log
-- ----------------------------
DROP TABLE IF EXISTS `request_log`;
CREATE TABLE `request_log` (
  `hlr_id` bigint(17) NOT NULL,
  `batch_id` bigint(20) unsigned DEFAULT '0',
  `number` bigint(15) DEFAULT NULL,
  `username` varchar(20) DEFAULT NULL,
  `flag` char(1) DEFAULT 'F',
  `time` varchar(50) DEFAULT NULL,
  `response_id` varchar(50) DEFAULT NULL,
  `system_type` varchar(10) DEFAULT 'BULK',
  `smsc_id` int(2) DEFAULT '0',
  `cost` double(10,5) DEFAULT '0.00000',
  `prefix` varchar(5) DEFAULT '0',
  PRIMARY KEY (`hlr_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for request_log_error
-- ----------------------------
DROP TABLE IF EXISTS `request_log_error`;
CREATE TABLE `request_log_error` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `batch_id` bigint(20) unsigned DEFAULT '0',
  `number` bigint(15) DEFAULT NULL,
  `errorcode` varchar(5) DEFAULT NULL,
  `error` varchar(50) DEFAULT NULL,
  `username` varchar(20) DEFAULT NULL,
  `time` varchar(50) DEFAULT NULL,
  `system_type` varchar(10) DEFAULT 'BULK',
  `smsc_id` int(2) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for status_code
-- ----------------------------
DROP TABLE IF EXISTS `status_code`;
CREATE TABLE `status_code` (
  `code` char(3) NOT NULL,
  `name` varchar(30) DEFAULT NULL,
  `permanent` tinyint(1) DEFAULT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for tmtv_status
-- ----------------------------
DROP TABLE IF EXISTS `tmtv_status`;
CREATE TABLE `tmtv_status` (
  `msg_id` varchar(30) NOT NULL,
  `destination` bigint(16) DEFAULT NULL,
  `flag` char(1) NOT NULL DEFAULT 'F',
  `remarks` varchar(100) DEFAULT NULL,
  `error` int(3) NOT NULL DEFAULT '0',
  PRIMARY KEY (`msg_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `hlr_deliver` VALUES ('23112715404110001', 'super', '2023-11-27 15:41:00', 'id:23112715404110001 sub:001 dlvrd:001 submit date:2311271540 done date:2311271541 stat:DELIVRD err:000 imsi:9374747474 msc:3333 cc:91 nnc:40493 mccmnc:40493 isPorted:false p_nnc: isRoaming:true r_cc:961 r_nnc:415001 text:dlr ', '919589050375', '0', 'BULK');
INSERT INTO `hlr_deliver` VALUES ('23112715443310002', 'super', '2023-11-27 15:44:35', 'id:23112715443310002 sub:001 dlvrd:001 submit date:2311271544 done date:2311271544 stat:DELIVRD err:000 imsi:9374747474 msc:3333 cc:91 nnc:40493 mccmnc:40493 isPorted:false p_nnc: isRoaming:true r_cc:961 r_nnc:415001 text:dlr ', '919589050375', '0', 'BULK');
INSERT INTO `hlr_deliver` VALUES ('23120117582510001', 'super', '2023-12-01 18:00:03', 'id:23120117582510001 sub:001 dlvrd:001 submit date:2312011758 done date:2312011800 stat:DELIVRD err:000 imsi:9374747474 msc:3333 cc:91 nnc:40493 mccmnc:40493 isPorted:false p_nnc: isRoaming:true r_cc:961 r_nnc:415001 text:dlr ', '919589050375', '0', 'BULK');
INSERT INTO `hlr_smsc` VALUES ('1', 'H1', 'test', 'test', 'localhost', '1111', null, 'tr', '0', '1', '0');
INSERT INTO `hlr_smsc` VALUES ('2', 'H2', 'AMITHLR', '@MIt1', 'localhost', '2222', 'BRD_SMPP', 'tr', '0', '0', '0');
INSERT INTO `hlr_smsc` VALUES ('5', 'H5', 'super', '1111111111', 'localhost', '5555', '', 'tr', '0', '0', '0');
INSERT INTO `hlr_smsc` VALUES ('3', 'H3', 'super', '1', 'localhost', '3333', null, 'tr', '0', '0', '0');
INSERT INTO `hlr_smsc` VALUES ('4', 'H4', 'test', 'test', 'localhost', '4444', null, 'tr', '0', '0', '0');
