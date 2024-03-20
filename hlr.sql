-- MySQL dump 10.13  Distrib 8.3.0, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: hlr
-- ------------------------------------------------------
-- Server version	8.3.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `batch_process`
--

DROP TABLE IF EXISTS `batch_process`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `batch_process` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(20) NOT NULL,
  `batchid` bigint unsigned DEFAULT '0',
  `server_id` int NOT NULL DEFAULT '1',
  `total` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `server_id` (`server_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `batch_process`
--

LOCK TABLES `batch_process` WRITE;
/*!40000 ALTER TABLE `batch_process` DISABLE KEYS */;
/*!40000 ALTER TABLE `batch_process` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hlr_deliver`
--

DROP TABLE IF EXISTS `hlr_deliver`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hlr_deliver` (
  `hlr_id` bigint NOT NULL,
  `username` varchar(10) DEFAULT NULL,
  `time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `pdu` longtext,
  `destination` bigint DEFAULT '0',
  `flag` tinyint(1) DEFAULT NULL,
  `system_type` varchar(10) DEFAULT 'BULK',
  PRIMARY KEY (`hlr_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hlr_deliver`
--

LOCK TABLES `hlr_deliver` WRITE;
/*!40000 ALTER TABLE `hlr_deliver` DISABLE KEYS */;
INSERT INTO `hlr_deliver` VALUES (23112715404110001,'super','2023-11-27 15:41:00','id:23112715404110001 sub:001 dlvrd:001 submit date:2311271540 done date:2311271541 stat:DELIVRD err:000 imsi:9374747474 msc:3333 cc:91 nnc:40493 mccmnc:40493 isPorted:false p_nnc: isRoaming:true r_cc:961 r_nnc:415001 text:dlr ',919589050375,0,'BULK'),(23112715443310002,'super','2023-11-27 15:44:35','id:23112715443310002 sub:001 dlvrd:001 submit date:2311271544 done date:2311271544 stat:DELIVRD err:000 imsi:9374747474 msc:3333 cc:91 nnc:40493 mccmnc:40493 isPorted:false p_nnc: isRoaming:true r_cc:961 r_nnc:415001 text:dlr ',919589050375,0,'BULK'),(23120117582510001,'super','2023-12-01 18:00:03','id:23120117582510001 sub:001 dlvrd:001 submit date:2312011758 done date:2312011800 stat:DELIVRD err:000 imsi:9374747474 msc:3333 cc:91 nnc:40493 mccmnc:40493 isPorted:false p_nnc: isRoaming:true r_cc:961 r_nnc:415001 text:dlr ',919589050375,0,'BULK');
/*!40000 ALTER TABLE `hlr_deliver` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hlr_smsc`
--

DROP TABLE IF EXISTS `hlr_smsc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hlr_smsc` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(5) DEFAULT NULL,
  `system_id` varchar(15) DEFAULT NULL,
  `password` varchar(50) DEFAULT NULL,
  `ip` varchar(30) DEFAULT NULL,
  `port` int DEFAULT NULL,
  `system_type` varchar(10) DEFAULT NULL,
  `bindmode` char(2) DEFAULT NULL,
  `sleep` int DEFAULT NULL,
  `bound` tinyint(1) NOT NULL DEFAULT '0',
  `group_id` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hlr_smsc`
--

LOCK TABLES `hlr_smsc` WRITE;
/*!40000 ALTER TABLE `hlr_smsc` DISABLE KEYS */;
INSERT INTO `hlr_smsc` VALUES (1,'H1','test','test','localhost',1111,NULL,'tr',0,0,0),(2,'H2','AMITHLR','@MIt1','localhost',2222,'BRD_SMPP','tr',0,0,0),(5,'H5','super','1111111111','localhost',5555,'','tr',0,0,0),(3,'H3','super','1','localhost',3333,NULL,'tr',0,0,0),(4,'H4','test','test','localhost',4444,NULL,'tr',0,0,0);
/*!40000 ALTER TABLE `hlr_smsc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lookup_result`
--

DROP TABLE IF EXISTS `lookup_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lookup_result` (
  `batch_id` bigint unsigned DEFAULT '0',
  `hlr_id` bigint NOT NULL,
  `resp_id` varchar(50) NOT NULL,
  `number` bigint DEFAULT NULL,
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
  `smsc_id` int DEFAULT '0',
  PRIMARY KEY (`hlr_id`),
  KEY `batch_index` (`batch_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lookup_result`
--

LOCK TABLES `lookup_result` WRITE;
/*!40000 ALTER TABLE `lookup_result` DISABLE KEYS */;
/*!40000 ALTER TABLE `lookup_result` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lookup_result_log`
--

DROP TABLE IF EXISTS `lookup_result_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lookup_result_log` (
  `batch_id` bigint unsigned DEFAULT '0',
  `hlr_id` bigint NOT NULL,
  `resp_id` varchar(50) NOT NULL,
  `number` bigint DEFAULT NULL,
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
  `smsc_id` int DEFAULT '0',
  PRIMARY KEY (`hlr_id`),
  KEY `batch_index` (`batch_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lookup_result_log`
--

LOCK TABLES `lookup_result_log` WRITE;
/*!40000 ALTER TABLE `lookup_result_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `lookup_result_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mcs_status`
--

DROP TABLE IF EXISTS `mcs_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mcs_status` (
  `msg_id` varchar(30) NOT NULL,
  `destination` bigint DEFAULT NULL,
  `flag` char(1) NOT NULL DEFAULT 'F',
  `remarks` varchar(100) DEFAULT NULL,
  `error` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`msg_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mcs_status`
--

LOCK TABLES `mcs_status` WRITE;
/*!40000 ALTER TABLE `mcs_status` DISABLE KEYS */;
/*!40000 ALTER TABLE `mcs_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `msgbrd_smsc`
--

DROP TABLE IF EXISTS `msgbrd_smsc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `msgbrd_smsc` (
  `id` int NOT NULL,
  `name` varchar(3) DEFAULT NULL,
  `key` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `msgbrd_smsc`
--

LOCK TABLES `msgbrd_smsc` WRITE;
/*!40000 ALTER TABLE `msgbrd_smsc` DISABLE KEYS */;
/*!40000 ALTER TABLE `msgbrd_smsc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `request_log`
--

DROP TABLE IF EXISTS `request_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `request_log` (
  `hlr_id` bigint NOT NULL,
  `batch_id` bigint unsigned DEFAULT '0',
  `number` bigint DEFAULT NULL,
  `username` varchar(20) DEFAULT NULL,
  `flag` char(1) DEFAULT 'F',
  `time` varchar(50) DEFAULT NULL,
  `response_id` varchar(50) DEFAULT NULL,
  `system_type` varchar(10) DEFAULT 'BULK',
  `smsc_id` int DEFAULT '0',
  `cost` double(10,5) DEFAULT '0.00000',
  `prefix` varchar(5) DEFAULT '0',
  PRIMARY KEY (`hlr_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `request_log`
--

LOCK TABLES `request_log` WRITE;
/*!40000 ALTER TABLE `request_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `request_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `request_log_error`
--

DROP TABLE IF EXISTS `request_log_error`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `request_log_error` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `batch_id` bigint unsigned DEFAULT '0',
  `number` bigint DEFAULT NULL,
  `errorcode` varchar(5) DEFAULT NULL,
  `error` varchar(50) DEFAULT NULL,
  `username` varchar(20) DEFAULT NULL,
  `time` varchar(50) DEFAULT NULL,
  `system_type` varchar(10) DEFAULT 'BULK',
  `smsc_id` int DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `request_log_error`
--

LOCK TABLES `request_log_error` WRITE;
/*!40000 ALTER TABLE `request_log_error` DISABLE KEYS */;
/*!40000 ALTER TABLE `request_log_error` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `status_code`
--

DROP TABLE IF EXISTS `status_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `status_code` (
  `code` char(3) NOT NULL,
  `name` varchar(30) DEFAULT NULL,
  `permanent` tinyint(1) DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `status_code`
--

LOCK TABLES `status_code` WRITE;
/*!40000 ALTER TABLE `status_code` DISABLE KEYS */;
/*!40000 ALTER TABLE `status_code` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tmtv_status`
--

DROP TABLE IF EXISTS `tmtv_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tmtv_status` (
  `msg_id` varchar(30) NOT NULL,
  `destination` bigint DEFAULT NULL,
  `flag` char(1) NOT NULL DEFAULT 'F',
  `remarks` varchar(100) DEFAULT NULL,
  `error` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`msg_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tmtv_status`
--

LOCK TABLES `tmtv_status` WRITE;
/*!40000 ALTER TABLE `tmtv_status` DISABLE KEYS */;
/*!40000 ALTER TABLE `tmtv_status` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-03-20  5:30:11
