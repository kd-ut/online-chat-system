-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: chat_db
-- ------------------------------------------------------
-- Server version	8.0.41

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
-- Table structure for table `chat_group`
--

DROP TABLE IF EXISTS `chat_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_group` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `avatar` varchar(500) DEFAULT NULL,
  `owner_id` bigint NOT NULL,
  `notice` varchar(200) DEFAULT NULL,
  `member_count` int DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_group`
--

LOCK TABLES `chat_group` WRITE;
/*!40000 ALTER TABLE `chat_group` DISABLE KEYS */;
INSERT INTO `chat_group` VALUES (1,'王者开黑群',NULL,7,'hellosdfasdfsdfagdfgsdgdfgsdfgsdfg',4,'2026-05-08 16:39:00','2026-05-08 16:38:59'),(3,'111',NULL,9,'dd',3,'2026-05-08 18:03:34','2026-05-08 18:03:34');
/*!40000 ALTER TABLE `chat_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `emoji`
--

DROP TABLE IF EXISTS `emoji`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `emoji` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '表情名称',
  `url` varchar(500) NOT NULL COMMENT '表情图片URL',
  `category` varchar(50) DEFAULT 'default' COMMENT '分类',
  `user_id` bigint DEFAULT NULL COMMENT '上传者ID（用户自定义表情）',
  `is_system` tinyint DEFAULT '1' COMMENT '是否系统表情 0:用户自定义 1:系统',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='表情包表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `emoji`
--

LOCK TABLES `emoji` WRITE;
/*!40000 ALTER TABLE `emoji` DISABLE KEYS */;
INSERT INTO `emoji` VALUES (1,'微笑','/emoji/smile.png','default',NULL,1,'2026-05-08 19:43:00'),(2,'大笑','/emoji/laugh.png','default',NULL,1,'2026-05-08 19:43:00'),(3,'哭','/emoji/cry.png','default',NULL,1,'2026-05-08 19:43:00'),(4,'生气','/emoji/angry.png','default',NULL,1,'2026-05-08 19:43:00'),(5,'惊讶','/emoji/surprised.png','default',NULL,1,'2026-05-08 19:43:00'),(6,'爱心','/emoji/heart.png','default',NULL,1,'2026-05-08 19:43:00'),(7,'微笑','/emoji/smile.png','default',NULL,1,'2026-05-08 19:43:14'),(8,'大笑','/emoji/laugh.png','default',NULL,1,'2026-05-08 19:43:14'),(9,'哭','/emoji/cry.png','default',NULL,1,'2026-05-08 19:43:14'),(10,'生气','/emoji/angry.png','default',NULL,1,'2026-05-08 19:43:14'),(11,'惊讶','/emoji/surprised.png','default',NULL,1,'2026-05-08 19:43:14'),(12,'爱心','/emoji/heart.png','default',NULL,1,'2026-05-08 19:43:14'),(13,'111','https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/1aafeee4-7b89-4d04-9797-dd80f86398a6.jpeg','custom',7,0,'2026-05-08 21:34:39'),(15,'jjdsjfse','https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/5b71c951-03d0-412d-88fe-4e9f15dc6c32.jpeg','custom',7,0,'2026-05-10 09:45:22'),(16,'ghfhgf','https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/bd0c35f5-9065-486d-849f-7035afaf54a7.png','custom',8,0,'2026-05-10 09:46:49'),(17,'00','https://dengya.oss-cn-beijing.aliyuncs.com/avatar.png','custom',7,0,'2026-05-12 08:05:11'),(18,'11111','https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/f2ae869c-3b9b-40cc-96c1-a21cc0b31540.jpg','custom',7,0,'2026-05-12 12:08:08'),(19,'h','https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/ab23d433-9135-424d-8335-7b3a8fda9a2a.jpg','custom',8,0,'2026-05-12 13:34:47'),(20,'fg','https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/379a9f70-9d0d-4fcd-874d-9cc9958acf0f.jpg','custom',8,0,'2026-05-12 13:35:00'),(21,'dsfsdf','https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/38c1c507-b868-45ed-a7cc-ac8b64d4be11.jpg','custom',7,0,'2026-05-12 19:17:27'),(22,'fdf','https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/6881f102-78fe-47b2-8d4a-90c26b29590f.jpeg','custom',7,0,'2026-05-13 09:14:13');
/*!40000 ALTER TABLE `emoji` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `friend`
--

DROP TABLE IF EXISTS `friend`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `friend` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `friend_id` bigint NOT NULL COMMENT '好友ID',
  `group_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '我的好友' COMMENT '分组名称',
  `remark` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '好友备注',
  `is_top` tinyint NOT NULL DEFAULT '0' COMMENT '是否置顶:0否,1是',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '成为好友时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_friend` (`user_id`,`friend_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_friend_id` (`friend_id`),
  CONSTRAINT `fk_friend_friend` FOREIGN KEY (`friend_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_friend_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `friend`
--

LOCK TABLES `friend` WRITE;
/*!40000 ALTER TABLE `friend` DISABLE KEYS */;
INSERT INTO `friend` VALUES (1,2,3,'同学','老李',0,'2026-05-07 17:07:06'),(2,3,2,'同学','老张',0,'2026-05-07 17:07:06'),(3,2,4,'同事','王工',0,'2026-05-07 17:07:06'),(4,4,2,'同事','张工',0,'2026-05-07 17:07:06'),(5,3,4,'球友',NULL,0,'2026-05-07 17:07:06'),(6,4,3,'球友',NULL,0,'2026-05-07 17:07:06'),(9,7,9,'篮球','篮球',0,'2026-05-08 08:09:55'),(10,9,7,'我的好友',NULL,0,'2026-05-08 08:09:55'),(11,7,10,'篮球',NULL,0,'2026-05-08 08:17:14'),(12,10,7,'我的好友',NULL,0,'2026-05-08 08:17:14'),(13,8,10,'我的好友',NULL,0,'2026-05-08 09:04:31'),(14,10,8,'我的好友',NULL,0,'2026-05-08 09:04:31'),(15,8,9,'我的好友',NULL,0,'2026-05-08 09:04:33'),(16,9,8,'我的好友',NULL,0,'2026-05-08 09:04:33'),(19,11,7,'我的好友',NULL,0,'2026-05-08 23:45:13'),(20,7,11,'ss',NULL,0,'2026-05-08 23:45:13'),(21,10,9,'我的好友',NULL,0,'2026-05-09 00:13:14'),(22,9,10,'我的好友',NULL,0,'2026-05-09 00:13:14'),(23,7,8,'gfdg','ff',0,'2026-05-10 10:09:21'),(24,8,7,'dad','hh',0,'2026-05-10 10:09:21'),(25,11,8,'我的好友',NULL,0,'2026-05-10 17:12:46'),(26,8,11,'我的好友',NULL,0,'2026-05-10 17:12:46');
/*!40000 ALTER TABLE `friend` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `friend_request`
--

DROP TABLE IF EXISTS `friend_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `friend_request` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `from_user_id` bigint NOT NULL COMMENT '申请者ID',
  `to_user_id` bigint NOT NULL COMMENT '接收者ID',
  `message` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '验证消息',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态:0待处理,1已同意,2已拒绝,3已过期',
  `handled_time` datetime DEFAULT NULL COMMENT '处理时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  PRIMARY KEY (`id`),
  KEY `idx_to_status` (`to_user_id`,`status`),
  KEY `idx_from_user` (`from_user_id`),
  CONSTRAINT `fk_request_from` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_request_to` FOREIGN KEY (`to_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `friend_request`
--

LOCK TABLES `friend_request` WRITE;
/*!40000 ALTER TABLE `friend_request` DISABLE KEYS */;
INSERT INTO `friend_request` VALUES (1,5,2,'你好，我是赵六，认识一下',0,NULL,'2026-05-14 17:07:06','2026-05-07 17:07:06'),(2,4,5,'一起打球吗？',1,'2026-05-07 17:07:06',NULL,'2026-05-07 17:07:06'),(3,8,7,NULL,1,'2026-05-07 23:07:10','2026-05-14 23:06:40','2026-05-07 23:06:40'),(4,9,8,NULL,1,'2026-05-08 09:04:33','2026-05-15 08:08:46','2026-05-08 08:08:46'),(5,9,7,NULL,2,'2026-05-08 08:09:28','2026-05-15 08:09:05','2026-05-08 08:09:05'),(6,9,7,NULL,1,'2026-05-08 08:09:55','2026-05-15 08:09:47','2026-05-08 08:09:47'),(7,10,8,NULL,1,'2026-05-08 09:04:31','2026-05-15 08:16:09','2026-05-08 08:16:09'),(8,10,7,NULL,1,'2026-05-08 08:17:14','2026-05-15 08:16:20','2026-05-08 08:16:20'),(9,7,1,NULL,0,NULL,'2026-05-15 23:09:25','2026-05-08 23:09:25'),(10,11,7,NULL,1,'2026-05-08 23:42:27','2026-05-15 23:41:43','2026-05-08 23:41:43'),(11,7,11,NULL,1,'2026-05-08 23:45:13','2026-05-15 23:45:08','2026-05-08 23:45:08'),(12,9,10,NULL,1,'2026-05-09 00:13:14','2026-05-16 00:12:54','2026-05-09 00:12:54'),(13,8,11,NULL,1,'2026-05-10 17:12:46','2026-05-17 10:06:40','2026-05-10 10:06:40'),(14,8,7,NULL,1,'2026-05-10 10:09:21','2026-05-17 10:09:11','2026-05-10 10:09:11');
/*!40000 ALTER TABLE `friend_request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_member`
--

DROP TABLE IF EXISTS `group_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `nickname` varchar(50) DEFAULT NULL,
  `role` tinyint DEFAULT '0',
  `unread_count` int DEFAULT '0',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `last_read_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_member`
--

LOCK TABLES `group_member` WRITE;
/*!40000 ALTER TABLE `group_member` DISABLE KEYS */;
INSERT INTO `group_member` VALUES (1,1,7,NULL,2,0,'2026-05-08 16:39:00','2026-05-13 09:32:40'),(2,1,8,NULL,1,1,'2026-05-08 16:39:00','2026-05-13 09:06:22'),(4,1,10,NULL,0,18,'2026-05-08 16:39:00','2026-05-08 16:38:59'),(9,3,9,NULL,2,1,'2026-05-08 18:03:34','2026-05-09 10:02:25'),(10,3,7,NULL,0,0,'2026-05-08 18:03:34','2026-05-13 09:32:39'),(11,3,8,NULL,0,0,'2026-05-08 18:03:34','2026-05-13 08:37:01'),(21,1,9,NULL,0,6,'2026-05-12 15:42:04','2026-05-12 15:42:03');
/*!40000 ALTER TABLE `group_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_message`
--

DROP TABLE IF EXISTS `group_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint NOT NULL,
  `from_user_id` bigint NOT NULL,
  `message_type` tinyint DEFAULT '1',
  `content` text NOT NULL,
  `send_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `recall_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_message`
--

LOCK TABLES `group_message` WRITE;
/*!40000 ALTER TABLE `group_message` DISABLE KEYS */;
INSERT INTO `group_message` VALUES (1,1,7,1,'hello\n','2026-05-08 16:39:08',NULL),(2,1,8,1,'hello','2026-05-08 16:40:12',NULL),(3,1,7,1,'111','2026-05-08 16:46:43',NULL),(4,1,7,1,'hello','2026-05-08 17:06:24',NULL),(5,1,9,1,'hello','2026-05-08 17:06:51',NULL),(6,1,7,1,'hello','2026-05-08 17:07:13',NULL),(7,1,7,1,'1111','2026-05-08 23:08:50',NULL),(8,1,7,1,'111','2026-05-08 23:08:52',NULL),(9,5,7,1,'gdfgdfg','2026-05-10 08:10:06',NULL),(10,1,7,1,'gdfgsg','2026-05-10 10:19:41',NULL),(11,1,7,1,'gdfgsfg','2026-05-10 10:19:45',NULL),(12,1,8,1,'sdfsdf','2026-05-10 10:20:06',NULL),(13,1,7,1,'1111111111','2026-05-12 15:40:30',NULL),(14,6,7,1,'11111','2026-05-12 16:34:42',NULL),(15,6,7,1,'1111111','2026-05-12 16:34:47',NULL),(16,1,7,1,'11111','2026-05-12 16:35:43',NULL),(17,1,8,1,'111111111111','2026-05-12 16:35:49',NULL),(18,1,8,1,'11111111111111','2026-05-12 16:35:58',NULL),(19,1,7,1,'111','2026-05-13 07:53:31',NULL),(20,1,7,1,'fsdfsdf','2026-05-13 08:00:28',NULL),(21,3,7,1,'11','2026-05-13 08:36:45',NULL),(22,1,7,1,'11','2026-05-13 09:16:34',NULL);
/*!40000 ALTER TABLE `group_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `impression`
--

DROP TABLE IF EXISTS `impression`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `impression` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `from_user_id` bigint NOT NULL COMMENT '评价者ID',
  `to_user_id` bigint NOT NULL COMMENT '被评价者ID',
  `content` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评价内容',
  `is_delete` tinyint NOT NULL DEFAULT '0' COMMENT '软删除:0正常,1删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`),
  KEY `idx_to_user` (`to_user_id`),
  KEY `idx_from_user` (`from_user_id`),
  CONSTRAINT `fk_impression_from` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_impression_to` FOREIGN KEY (`to_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友印象评价表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `impression`
--

LOCK TABLES `impression` WRITE;
/*!40000 ALTER TABLE `impression` DISABLE KEYS */;
INSERT INTO `impression` VALUES (1,2,3,'靠谱',0,'2026-05-07 17:07:06'),(2,2,3,'技术大牛',0,'2026-05-07 17:07:06'),(3,3,2,'热心肠',0,'2026-05-07 17:07:06'),(4,3,2,'工作认真',0,'2026-05-07 17:07:06'),(5,4,2,'好大哥',0,'2026-05-07 17:07:06'),(6,7,8,'shigay',0,'2026-05-08 14:57:03'),(7,8,7,'大傻子',0,'2026-05-08 14:57:55'),(8,8,7,'有点村',1,'2026-05-08 14:58:20'),(9,7,8,'ddd',0,'2026-05-08 15:06:48'),(10,7,8,'11',0,'2026-05-08 15:12:06'),(11,7,8,'111',0,'2026-05-08 15:16:51'),(12,7,8,'gdgdf',0,'2026-05-08 15:17:30'),(13,7,8,'erewr',0,'2026-05-08 15:19:07'),(14,7,8,'fgfdg',0,'2026-05-08 15:28:56'),(15,7,8,'fgdf',0,'2026-05-08 15:29:01'),(16,7,8,'212',0,'2026-05-08 15:29:53'),(17,7,8,'zdfgsdf',0,'2026-05-08 15:30:37'),(18,7,8,'tert',0,'2026-05-08 15:31:14'),(19,7,8,'gdfg',0,'2026-05-08 15:36:33'),(20,7,8,'retr',0,'2026-05-08 15:45:22'),(21,7,8,'22',0,'2026-05-08 15:48:33'),(22,7,8,'eee',0,'2026-05-08 15:49:20'),(23,7,8,'eee',0,'2026-05-08 15:49:56'),(24,7,8,'ee',0,'2026-05-08 15:53:03'),(25,7,8,'dddd',0,'2026-05-08 19:19:54'),(26,8,7,'0000',0,'2026-05-08 19:20:34'),(27,7,8,'111',1,'2026-05-08 21:32:19'),(28,7,8,'dfsafdas',0,'2026-05-08 23:09:39'),(29,7,11,'gay',0,'2026-05-08 23:46:38'),(30,9,7,'111',0,'2026-05-09 10:02:49'),(31,8,7,'ddd',0,'2026-05-09 10:06:08'),(32,7,8,'tertr',0,'2026-05-10 08:10:19'),(33,7,8,'ertger',1,'2026-05-10 09:27:30'),(34,8,7,'ewryeyrtyrtyrtuftyrtyr',1,'2026-05-10 10:56:04'),(35,7,8,'fsdfa',0,'2026-05-10 11:04:56'),(36,7,8,'sdfsdf',0,'2026-05-11 09:29:46'),(37,7,8,'sfsdfaffsdaddddddddddddddddddddddddddddddddddddddffffffff',1,'2026-05-11 09:30:49'),(38,7,8,'gdfgsdgsd',1,'2026-05-11 11:53:02'),(39,8,7,'dsfafasd',0,'2026-05-12 12:45:14'),(40,7,8,'fsdfasd',0,'2026-05-12 12:45:32'),(41,8,7,'fffffffffff',1,'2026-05-12 15:42:28');
/*!40000 ALTER TABLE `impression` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `from_user_id` bigint NOT NULL COMMENT '发送者ID',
  `to_user_id` bigint NOT NULL,
  `message_type` tinyint DEFAULT '1',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读:0未读,1已读',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `recall_time` datetime DEFAULT NULL COMMENT '撤回时间(不为空表示已撤回)',
  `send_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_from_to_time` (`from_user_id`,`to_user_id`,`send_time`),
  KEY `idx_to_read` (`to_user_id`,`is_read`),
  KEY `idx_send_time` (`send_time`),
  CONSTRAINT `fk_message_from` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_message_to` FOREIGN KEY (`to_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=203 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message`
--

LOCK TABLES `message` WRITE;
/*!40000 ALTER TABLE `message` DISABLE KEYS */;
INSERT INTO `message` VALUES (1,2,3,1,'李四，下午开会记得来',1,NULL,NULL,'2026-05-07 09:00:00'),(2,3,2,1,'好的，收到！',1,NULL,NULL,'2026-05-07 09:05:00'),(3,2,3,1,'项目进度怎么样了？',1,NULL,NULL,'2026-05-07 10:00:00'),(4,3,2,1,'完成了80%，明天可以提交',0,NULL,NULL,'2026-05-07 10:30:00'),(5,3,2,1,'对了，晚上一起吃饭？',0,NULL,NULL,'2026-05-07 11:00:00'),(6,2,4,1,'王工，代码review一下',1,NULL,NULL,'2026-05-07 08:00:00'),(7,4,2,1,'好的，我看看',1,NULL,NULL,'2026-05-07 08:15:00'),(8,4,2,1,'整体不错，有几个小问题改一下',0,NULL,NULL,'2026-05-07 08:30:00'),(9,7,8,1,'hello',1,'2026-05-08 07:05:21',NULL,'2026-05-08 07:04:15'),(10,7,8,1,'你好',1,'2026-05-08 07:07:31',NULL,'2026-05-08 07:07:14'),(11,7,8,1,'111',1,'2026-05-08 07:07:47',NULL,'2026-05-08 07:07:46'),(12,7,8,1,'hello',1,'2026-05-08 07:13:08',NULL,'2026-05-08 07:13:06'),(13,7,8,1,'hello',1,'2026-05-08 07:15:59',NULL,'2026-05-08 07:15:58'),(14,7,8,1,'hello',1,'2026-05-08 07:26:32',NULL,'2026-05-08 07:26:30'),(15,7,8,1,'hello',1,'2026-05-08 07:46:02',NULL,'2026-05-08 07:39:48'),(16,7,8,1,'hello',1,'2026-05-08 07:46:02',NULL,'2026-05-08 07:45:56'),(17,7,10,1,'hello\n',1,'2026-05-08 08:18:47',NULL,'2026-05-08 08:18:11'),(18,7,10,1,'xixi',1,'2026-05-08 08:18:47',NULL,'2026-05-08 08:18:33'),(19,7,10,1,'haha',1,'2026-05-08 08:18:47',NULL,'2026-05-08 08:18:37'),(20,7,8,1,'heelo',1,'2026-05-08 09:04:07',NULL,'2026-05-08 08:25:33'),(21,7,10,1,'111',1,'2026-05-08 08:25:47',NULL,'2026-05-08 08:25:45'),(22,10,7,1,'111',1,'2026-05-08 08:25:52',NULL,'2026-05-08 08:25:52'),(23,8,7,1,'hehe',1,'2026-05-08 09:10:12',NULL,'2026-05-08 09:05:56'),(24,7,8,1,'ddd\n',1,'2026-05-08 14:47:37',NULL,'2026-05-08 14:47:35'),(25,7,8,1,'fdfds',1,'2026-05-08 14:47:43',NULL,'2026-05-08 14:47:41'),(26,7,8,1,'1111',1,'2026-05-08 14:47:57',NULL,'2026-05-08 14:47:56'),(27,7,8,1,'111',1,'2026-05-08 14:47:58',NULL,'2026-05-08 14:47:59'),(28,7,8,1,'fdsfsdfdsf',1,'2026-05-08 14:48:38',NULL,'2026-05-08 14:48:36'),(29,7,8,1,'fsdfsdf',1,'2026-05-08 14:48:40',NULL,'2026-05-08 14:48:40'),(30,8,7,1,'234w34w',1,'2026-05-08 14:49:08',NULL,'2026-05-08 14:49:09'),(31,8,7,1,'dasfdfsdfsdfsdf',1,'2026-05-08 14:49:29',NULL,'2026-05-08 14:49:29'),(32,7,8,1,'qqqq',1,'2026-05-08 14:50:10',NULL,'2026-05-08 14:50:09'),(33,7,8,1,'ssss',1,'2026-05-08 14:50:19',NULL,'2026-05-08 14:50:17'),(34,7,8,1,'hello',1,'2026-05-08 15:12:23',NULL,'2026-05-08 15:06:39'),(35,7,8,1,'请求',1,'2026-05-08 15:12:23',NULL,'2026-05-08 15:11:54'),(36,7,8,1,'111',1,'2026-05-08 15:16:46',NULL,'2026-05-08 15:16:44'),(37,7,8,1,'gdfgfdg',1,'2026-05-08 15:17:41',NULL,'2026-05-08 15:17:39'),(38,7,8,1,'fgdfgsd',1,'2026-05-08 15:17:53',NULL,'2026-05-08 15:17:53'),(39,7,8,1,'111',1,'2026-05-08 15:18:41',NULL,'2026-05-08 15:18:35'),(40,7,8,1,'111',1,'2026-05-08 15:28:48',NULL,'2026-05-08 15:28:47'),(41,7,8,1,'222',1,'2026-05-08 15:28:48',NULL,'2026-05-08 15:28:49'),(42,7,8,1,'11',1,'2026-05-08 15:29:48',NULL,'2026-05-08 15:29:47'),(43,7,8,1,'11',1,'2026-05-08 15:30:44',NULL,'2026-05-08 15:30:17'),(44,7,8,1,'1232',1,'2026-05-08 15:31:02',NULL,'2026-05-08 15:31:00'),(45,7,8,1,'yeryr',1,'2026-05-08 15:36:24',NULL,'2026-05-08 15:36:24'),(46,7,8,1,'gfgd',1,'2026-05-08 15:36:28',NULL,'2026-05-08 15:36:28'),(47,7,8,3,'爸爸 给你添加了一条评价：gdfg',1,'2026-05-08 15:36:43',NULL,'2026-05-08 15:36:34'),(48,7,8,1,'gdfgfd',1,'2026-05-08 15:37:01',NULL,'2026-05-08 15:37:02'),(49,8,7,1,'gfdgdf',1,'2026-05-08 15:37:17',NULL,'2026-05-08 15:37:18'),(50,7,8,1,'11',1,'2026-05-08 15:41:46',NULL,'2026-05-08 15:41:47'),(51,7,8,1,'dsfsdf',1,'2026-05-08 15:45:05',NULL,'2026-05-08 15:45:04'),(52,7,8,3,'爸爸 给你添加了一条评价：retr',1,'2026-05-08 15:45:59',NULL,'2026-05-08 15:45:22'),(53,7,8,1,'11',1,'2026-05-08 15:46:41',NULL,'2026-05-08 15:46:40'),(54,7,8,1,'111',1,'2026-05-08 15:48:23',NULL,'2026-05-08 15:48:01'),(55,7,8,1,'1111',1,'2026-05-08 15:48:28',NULL,'2026-05-08 15:48:28'),(56,7,8,3,'爸爸 给你添加了一条评价：22',1,'2026-05-08 15:48:46',NULL,'2026-05-08 15:48:33'),(57,7,8,3,'爸爸 给你添加了一条评价：eee',1,'2026-05-08 15:49:23',NULL,'2026-05-08 15:49:20'),(58,7,8,1,'eee',1,'2026-05-08 15:49:23',NULL,'2026-05-08 15:49:24'),(59,7,8,1,'111',1,'2026-05-08 15:53:18',NULL,'2026-05-08 15:49:44'),(60,7,8,3,'爸爸 给你添加了一条评价：eee',1,'2026-05-08 15:53:18',NULL,'2026-05-08 15:49:56'),(61,7,8,3,'爸爸 给你添加了一条评价：ee',1,'2026-05-08 15:53:18',NULL,'2026-05-08 15:53:03'),(62,9,7,1,'hello',1,'2026-05-08 18:04:41',NULL,'2026-05-08 18:03:17'),(63,7,9,1,'hello',1,'2026-05-08 18:10:11',NULL,'2026-05-08 18:10:11'),(64,9,8,1,'hello',1,'2026-05-08 18:11:00',NULL,'2026-05-08 18:10:49'),(65,7,9,1,'hello',1,'2026-05-08 18:11:23',NULL,'2026-05-08 18:11:11'),(66,7,9,1,'hello',1,'2026-05-08 18:12:28',NULL,'2026-05-08 18:11:44'),(67,9,8,1,'hello',1,'2026-05-08 18:12:53',NULL,'2026-05-08 18:12:41'),(68,8,9,1,'hello',1,'2026-05-08 18:13:00',NULL,'2026-05-08 18:13:01'),(69,8,7,1,'111',1,'2026-05-08 19:19:33',NULL,'2026-05-08 19:19:26'),(70,7,8,3,'爸爸 给你添加了一条评价：dddd',1,'2026-05-08 19:20:05',NULL,'2026-05-08 19:19:54'),(71,8,7,3,'gn 给你添加了一条评价：0000',1,'2026-05-08 19:20:41',NULL,'2026-05-08 19:20:34'),(72,8,7,2,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/images/2026/05/7d4f5599-49e4-4022-bca5-262e7d964d47.jpg',1,'2026-05-08 20:10:56',NULL,'2026-05-08 20:10:15'),(73,8,7,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/e4292322-1d07-4c55-822b-cf6ffd5b1536.webm',1,'2026-05-08 20:10:56',NULL,'2026-05-08 20:10:29'),(74,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/d03129ee-3adc-4179-a7dd-1e4463a0a4fa.webm',1,'2026-05-08 20:44:59',NULL,'2026-05-08 20:44:52'),(75,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/cfff7b8d-d15c-443e-8565-f66a908b1418.webm',1,'2026-05-08 20:46:25',NULL,'2026-05-08 20:46:26'),(76,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/5b1ab1a5-9d39-4e30-8fe5-429be66aaa30.webm',1,'2026-05-08 20:54:09',NULL,'2026-05-08 20:53:37'),(77,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/bd182665-8517-4dbf-8ad9-a7315bcd15dd.webm',1,'2026-05-08 21:05:37',NULL,'2026-05-08 21:00:08'),(78,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/58b8fb66-ce77-44b8-ab5f-4bec55a1ddae.webm',1,'2026-05-08 21:05:37',NULL,'2026-05-08 21:00:23'),(79,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/191df504-713d-422d-8205-94a2eb728ea8.webm|1',1,'2026-05-08 21:05:37',NULL,'2026-05-08 21:05:02'),(80,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/85074714-5e81-4c10-9e86-6e45cd3c86ad.webm|10',1,'2026-05-08 21:05:37',NULL,'2026-05-08 21:05:23'),(81,7,8,3,'爸爸 给你添加了一条评价：111',1,'2026-05-08 21:32:25',NULL,'2026-05-08 21:32:19'),(82,7,8,2,'/emoji/cry.png',1,'2026-05-08 21:34:03',NULL,'2026-05-08 21:33:41'),(83,7,8,2,'https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/1aafeee4-7b89-4d04-9797-dd80f86398a6.jpeg',1,'2026-05-08 21:40:53',NULL,'2026-05-08 21:34:55'),(84,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/19a6841e-9dfc-4439-8c9e-562d32ab542b.webm|3',1,'2026-05-08 21:44:21',NULL,'2026-05-08 21:43:52'),(85,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/5459d6e5-8a5c-43ca-ae0d-aa2664b50a5b.webm|4',1,'2026-05-08 22:14:16',NULL,'2026-05-08 22:05:14'),(86,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/677d1978-45b3-4dd5-9332-03e88e0c3b12.webm|3',1,'2026-05-08 22:14:16',NULL,'2026-05-08 22:14:03'),(87,8,7,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/36da7660-0412-432c-bdb5-c19bd2d69af1.webm|3',1,'2026-05-08 22:14:55',NULL,'2026-05-08 22:14:32'),(88,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/28a4e20a-9c10-4bcf-80d0-17c3a5464bf7.webm|2',1,'2026-05-08 22:26:45',NULL,'2026-05-08 22:26:32'),(89,7,8,1,'1111',1,'2026-05-08 22:39:49',NULL,'2026-05-08 22:38:55'),(90,7,8,1,'12312312',1,'2026-05-08 22:39:49',NULL,'2026-05-08 22:39:07'),(91,7,8,1,'123123',1,'2026-05-08 22:39:49',NULL,'2026-05-08 22:39:28'),(92,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/f0e4405a-80d9-49a0-8b28-5e9dd5d63763.webm|3',1,'2026-05-08 22:39:49',NULL,'2026-05-08 22:39:39'),(93,7,8,1,'11',1,'2026-05-08 22:40:09',NULL,'2026-05-08 22:40:02'),(94,7,8,3,'爸爸 给你添加了一条评价：dfsafdas',1,'2026-05-08 23:11:28',NULL,'2026-05-08 23:09:39'),(95,7,8,1,'fsadggdfg',1,'2026-05-08 23:11:28',NULL,'2026-05-08 23:10:19'),(96,7,8,2,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/images/2026/05/ddd17dc1-392b-431d-8a9c-26bb6550df51.jpeg',1,'2026-05-08 23:11:28',NULL,'2026-05-08 23:10:49'),(97,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/03b37939-afe3-4b13-9dd9-3723633b780a.webm|4',1,'2026-05-08 23:11:28',NULL,'2026-05-08 23:11:00'),(98,8,11,2,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/images/2026/05/9682021b-540b-410b-bcfb-5477b2429abd.png',1,'2026-05-08 23:43:34',NULL,'2026-05-08 23:43:15'),(99,8,11,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/19cd8e15-8cad-42f2-8109-d42671779a35.webm|3',1,'2026-05-08 23:43:34',NULL,'2026-05-08 23:43:21'),(100,7,11,2,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/images/2026/05/80caeacf-105c-4110-8dcd-0fe34038c237.png',1,'2026-05-08 23:45:33',NULL,'2026-05-08 23:45:32'),(101,7,11,3,'爸爸 给你添加了一条评价：gay',1,'2026-05-08 23:46:52',NULL,'2026-05-08 23:46:38'),(102,9,7,3,'ml 给你添加了一条评价：111',1,'2026-05-09 10:03:35',NULL,'2026-05-09 10:02:49'),(103,7,7,1,'早上好啊',1,'2026-05-09 10:07:19',NULL,'2026-05-09 10:05:48'),(104,7,9,1,'早上好啊',1,'2026-05-12 17:29:06',NULL,'2026-05-09 10:05:57'),(105,8,7,3,'gn 给你添加了一条评价：ddd',1,'2026-05-09 10:07:12',NULL,'2026-05-09 10:06:08'),(106,7,11,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/9c5f61e6-b928-4a6a-9209-729073c41d42.webm|5',1,'2026-05-09 10:13:34',NULL,'2026-05-09 10:12:44'),(107,7,11,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/f0e0e7fc-0741-414f-adb4-9b2066f049c9.webm|1',1,'2026-05-09 10:13:34',NULL,'2026-05-09 10:13:06'),(108,7,8,3,'爸爸 给你添加了一条评价：tertr',1,'2026-05-10 08:13:12',NULL,'2026-05-10 08:10:19'),(109,7,8,1,'123123',1,'2026-05-10 08:15:10',NULL,'2026-05-10 08:14:42'),(110,7,8,1,'ryrtyrtyrtyy',1,'2026-05-10 08:15:10',NULL,'2026-05-10 08:14:54'),(111,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/5897e6c9-f31a-4c0a-bf08-92e24cb4e83b.webm|4',1,'2026-05-10 08:16:35',NULL,'2026-05-10 08:16:10'),(112,7,8,2,'/emoji/laugh.png',1,'2026-05-10 08:55:53',NULL,'2026-05-10 08:16:45'),(113,7,8,2,'https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/9ecfa37d-59e2-4b5b-8d80-97d24669fe70.png',1,'2026-05-10 08:55:53',NULL,'2026-05-10 08:17:22'),(114,7,8,2,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/images/2026/05/634e3396-7669-46c0-b3c4-97a32cd0eaf2.jpg',1,'2026-05-10 08:55:53',NULL,'2026-05-10 08:17:59'),(115,7,8,1,'hello',1,'2026-05-10 09:16:53',NULL,'2026-05-10 09:14:29'),(116,7,9,1,'sha',1,'2026-05-12 17:29:06',NULL,'2026-05-10 09:15:01'),(117,7,8,1,'gaona',1,'2026-05-10 09:16:53',NULL,'2026-05-10 09:15:44'),(118,8,7,1,'笨蛋',1,'2026-05-10 09:17:27',NULL,'2026-05-10 09:17:05'),(119,7,8,1,'呵呵',1,'2026-05-10 09:23:01',NULL,'2026-05-10 09:22:41'),(120,7,8,3,'爸爸 给你添加了一条评价：ertger',1,'2026-05-10 09:27:35',NULL,'2026-05-10 09:27:30'),(121,7,8,2,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/images/2026/05/67938bd5-580e-4324-9a70-19ee4a6e4ab3.jpeg',1,'2026-05-10 09:44:26',NULL,'2026-05-10 09:44:13'),(122,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/db0e06b6-d9fe-4750-9d6f-17dd2122d632.webm|4',1,'2026-05-10 09:44:26',NULL,'2026-05-10 09:44:20'),(123,7,8,2,'https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/5b71c951-03d0-412d-88fe-4e9f15dc6c32.jpeg',1,'2026-05-10 09:47:10',NULL,'2026-05-10 09:45:30'),(124,7,8,2,'https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/5b71c951-03d0-412d-88fe-4e9f15dc6c32.jpeg',1,'2026-05-10 09:47:10',NULL,'2026-05-10 09:45:39'),(125,7,8,2,'https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/5b71c951-03d0-412d-88fe-4e9f15dc6c32.jpeg',1,'2026-05-10 09:47:10',NULL,'2026-05-10 09:45:40'),(126,7,8,2,'https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/5b71c951-03d0-412d-88fe-4e9f15dc6c32.jpeg',1,'2026-05-10 09:47:10',NULL,'2026-05-10 09:45:40'),(127,7,8,2,'https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/5b71c951-03d0-412d-88fe-4e9f15dc6c32.jpeg',1,'2026-05-10 09:47:10',NULL,'2026-05-10 09:45:41'),(128,7,8,2,'/emoji/smile.png',1,'2026-05-10 09:47:10',NULL,'2026-05-10 09:45:43'),(129,7,8,2,'/emoji/smile.png',1,'2026-05-10 09:47:10',NULL,'2026-05-10 09:45:44'),(130,7,8,2,'https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/5b71c951-03d0-412d-88fe-4e9f15dc6c32.jpeg',1,'2026-05-10 09:47:10',NULL,'2026-05-10 09:46:02'),(131,7,8,2,'https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/1aafeee4-7b89-4d04-9797-dd80f86398a6.jpeg',1,'2026-05-10 09:47:10',NULL,'2026-05-10 09:46:07'),(132,8,7,2,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/images/2026/05/24a842e2-2361-4b35-b9b8-030030ac25bc.jpg',1,'2026-05-10 09:50:31',NULL,'2026-05-10 09:46:31'),(133,8,7,2,'https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/bd0c35f5-9065-486d-849f-7035afaf54a7.png',1,'2026-05-10 09:50:31',NULL,'2026-05-10 09:46:52'),(134,8,7,2,'https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/bd0c35f5-9065-486d-849f-7035afaf54a7.png',1,'2026-05-10 09:50:31',NULL,'2026-05-10 09:47:04'),(135,7,8,2,'/emoji/angry.png',1,'2026-05-10 10:03:00',NULL,'2026-05-10 09:50:22'),(136,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/66372e1b-e137-4238-8ee3-1440727e6c7b.webm|1',1,'2026-05-10 10:03:00',NULL,'2026-05-10 09:50:41'),(137,8,7,1,'dfsfd',1,'2026-05-10 10:09:25',NULL,'2026-05-10 10:08:04'),(138,8,7,1,'dfsdf',1,'2026-05-10 10:09:25',NULL,'2026-05-10 10:08:09'),(139,8,7,1,'fdsfsdf',1,'2026-05-10 10:55:16',NULL,'2026-05-10 10:54:58'),(140,8,7,1,'gasdgdfhfdgjghjfghfggggggggggggggggggggggggggggggggggggg',1,'2026-05-10 10:55:16',NULL,'2026-05-10 10:55:06'),(141,8,7,3,'妈妈 给你添加了一条评价：ewryeyrtyrtyrtuftyrtyr',1,'2026-05-10 10:56:18',NULL,'2026-05-10 10:56:04'),(142,7,8,3,'爸爸 给你添加了一条评价：fsdfa',1,'2026-05-10 11:05:04',NULL,'2026-05-10 11:04:56'),(143,8,7,1,'fsdaffsd',1,'2026-05-10 11:05:25',NULL,'2026-05-10 11:05:09'),(144,8,7,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/45231de6-dcc4-4d5f-bee5-41fdd877c11d.webm|3',1,'2026-05-10 11:25:59',NULL,'2026-05-10 11:25:42'),(145,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/b061f8e8-c0bd-4a3f-b60e-3c23a29fbb13.webm|3',1,'2026-05-10 11:26:19',NULL,'2026-05-10 11:26:09'),(146,8,7,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/e4501e37-c7d3-4f23-ae0e-56dc826487d3.webm|2',1,'2026-05-10 11:26:41',NULL,'2026-05-10 11:26:33'),(147,8,7,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/5e8820e6-3df3-466f-8e64-df566d55b6f1.webm|4',1,'2026-05-10 11:30:48',NULL,'2026-05-10 11:30:38'),(148,7,9,1,'111',1,'2026-05-12 17:29:06',NULL,'2026-05-10 15:28:23'),(149,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/0ed589cf-e3dc-4d4b-83dc-c51ce126ca44.webm|2',1,'2026-05-10 16:16:40',NULL,'2026-05-10 16:16:35'),(150,8,11,1,'ggug',1,'2026-05-10 17:14:43',NULL,'2026-05-10 17:14:27'),(151,8,11,2,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/images/2026/05/4a5ad63f-570c-45f1-ab77-6762edd03d08.png',1,'2026-05-10 17:14:43',NULL,'2026-05-10 17:14:39'),(152,7,11,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/760a44fa-7fef-4ef1-85cc-3c570705659c.webm|3',1,'2026-05-10 17:15:19',NULL,'2026-05-10 17:15:18'),(153,7,8,3,'爸爸 给你添加了一条评价：sdfsdf',1,'2026-05-11 09:29:52',NULL,'2026-05-11 09:29:46'),(154,7,8,1,'sdafsfsd',1,'2026-05-11 09:30:23',NULL,'2026-05-11 09:30:07'),(155,7,8,3,'爸爸 给你添加了一条评价：sfsdfaffsdaddddddddddddddddddddddddddddddddddddddffffffff',1,'2026-05-11 09:44:49',NULL,'2026-05-11 09:30:49'),(156,7,8,1,'111',1,'2026-05-11 11:52:48',NULL,'2026-05-11 11:52:41'),(157,7,8,3,'爸爸 给你添加了一条评价：gdfgsdgsd',1,'2026-05-11 11:53:10',NULL,'2026-05-11 11:53:02'),(158,7,8,2,'https://dengya.oss-cn-beijing.aliyuncs.com/avatar.png',1,'2026-05-12 08:05:54',NULL,'2026-05-12 08:05:17'),(159,8,7,4,'https://dengya.oss-cn-beijing.aliyuncs.com/avatar.png|2',1,'2026-05-12 08:06:14',NULL,'2026-05-12 08:06:07'),(160,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/avatar.png|7',1,'2026-05-12 08:13:05',NULL,'2026-05-12 08:06:31'),(161,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/avatar.png|1',1,'2026-05-12 08:24:33',NULL,'2026-05-12 08:24:28'),(162,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/avatar.png|3',1,'2026-05-12 09:30:21',NULL,'2026-05-12 09:26:24'),(163,8,7,4,'https://dengya.oss-cn-beijing.aliyuncs.com/avatar.png|2',1,'2026-05-12 09:39:01',NULL,'2026-05-12 09:36:22'),(164,8,7,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/3a80318e-bdd3-40ca-87ab-c3237b6e4496.webm|2',1,'2026-05-12 12:08:26',NULL,'2026-05-12 12:07:19'),(165,7,8,2,'https://dengya.oss-cn-beijing.aliyuncs.com/emoji/2026/05/f2ae869c-3b9b-40cc-96c1-a21cc0b31540.jpg',1,'2026-05-12 12:08:24',NULL,'2026-05-12 12:08:12'),(166,7,8,2,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/images/2026/05/cdbd4565-3481-474b-be4a-6e83134cd1f5.jpeg',1,'2026-05-12 12:08:24',NULL,'2026-05-12 12:08:21'),(167,8,7,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/60bcc1f5-a77e-4407-8cb6-665a76866d4c.webm|2',1,'2026-05-12 12:18:18',NULL,'2026-05-12 12:13:37'),(168,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/0d1b8237-c5df-4727-ab0f-4620efb45024.webm|1',1,'2026-05-12 12:30:58',NULL,'2026-05-12 12:24:58'),(169,8,7,3,'妈妈 给你添加了一条评价：dsfafasd',1,'2026-05-12 12:45:55',NULL,'2026-05-12 12:45:14'),(170,7,8,3,'爸爸 给你添加了一条评价：fsdfasd',1,'2026-05-12 12:45:38',NULL,'2026-05-12 12:45:32'),(171,7,9,1,'111',1,'2026-05-12 17:29:06',NULL,'2026-05-12 13:07:56'),(172,7,9,1,'dfsdfsdf\n',1,'2026-05-12 17:29:06',NULL,'2026-05-12 13:08:10'),(173,7,8,1,'fsdfsdf',1,'2026-05-12 13:16:00',NULL,'2026-05-12 13:08:22'),(174,8,9,1,'12323',1,'2026-05-12 17:29:06',NULL,'2026-05-12 13:14:52'),(175,7,8,1,'werwer',1,'2026-05-12 13:16:00',NULL,'2026-05-12 13:15:01'),(176,7,8,1,'rwerwe',1,'2026-05-12 13:16:00',NULL,'2026-05-12 13:15:05'),(177,8,9,1,'ewrwer',1,'2026-05-12 17:29:06',NULL,'2026-05-12 13:15:09'),(178,8,7,1,'111',1,'2026-05-12 13:36:24',NULL,'2026-05-12 13:35:29'),(179,8,7,1,'111',1,'2026-05-12 13:36:24',NULL,'2026-05-12 13:35:37'),(180,8,7,2,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/images/2026/05/3e31f795-1893-4431-953e-96de680344d8.png',1,'2026-05-12 13:36:24',NULL,'2026-05-12 13:35:52'),(181,8,7,3,'妈妈 给你添加了一条评价：fffffffffff',1,'2026-05-12 15:42:35',NULL,'2026-05-12 15:42:28'),(182,8,7,1,'111',1,'2026-05-12 16:40:44',NULL,'2026-05-12 16:35:27'),(183,7,11,1,'fdsfaaf',1,'2026-05-12 17:31:43',NULL,'2026-05-12 17:31:17'),(184,7,11,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/a70ab37e-88f9-4073-87e4-44b7590c8261.webm|1',0,NULL,NULL,'2026-05-12 17:33:00'),(185,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/fb73fa04-d681-49e0-9065-3476b55f4768.webm|1',1,'2026-05-12 17:35:08',NULL,'2026-05-12 17:35:01'),(186,7,8,2,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/images/2026/05/29705368-e972-4f7b-aed6-d4cbd84f7d3c.jpeg',1,'2026-05-12 19:16:31',NULL,'2026-05-12 19:15:25'),(187,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/39a3259a-882d-433f-abdd-ec968f0ad9e9.webm|2',1,'2026-05-12 19:16:31',NULL,'2026-05-12 19:15:35'),(188,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/19cfef02-4c6b-4429-a32b-31e607f2aefb.webm|1',1,'2026-05-12 19:16:31',NULL,'2026-05-12 19:15:42'),(189,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/ec4cbf09-dcb6-464a-b0e0-e2d173d2f17f.webm|1',1,'2026-05-12 20:12:02',NULL,'2026-05-12 20:11:29'),(190,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/3c1d62bd-165a-4c08-baf6-d240a04d64d6.webm|2',1,'2026-05-12 20:12:02',NULL,'2026-05-12 20:11:37'),(191,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/61754ec2-03c1-4bd3-acfd-5ad0c0c00db3.webm|2',1,'2026-05-12 21:02:50',NULL,'2026-05-12 21:01:03'),(192,7,8,2,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/images/2026/05/43d53af5-bd9a-4710-a2f8-b1838818ac60.jpg',1,'2026-05-12 21:47:20',NULL,'2026-05-12 21:28:55'),(193,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/68e95fb9-6035-4983-a993-18330c3367ae.webm|1',1,'2026-05-12 23:10:16',NULL,'2026-05-12 23:09:40'),(194,7,8,1,'11111',1,'2026-05-12 23:54:40',NULL,'2026-05-12 23:52:27'),(195,7,8,2,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/images/2026/05/e4e7ba75-c7df-4f7c-a740-3b8857a0c35a.png',1,'2026-05-12 23:54:40',NULL,'2026-05-12 23:53:59'),(196,7,8,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/0a20489d-b49a-4268-8646-dfe105a09d09.webm|1',1,'2026-05-12 23:54:40',NULL,'2026-05-12 23:54:06'),(197,8,7,1,'dgfgdfg',1,'2026-05-13 07:51:12',NULL,'2026-05-13 07:51:05'),(198,7,8,1,'fgfdg',1,'2026-05-13 07:53:47',NULL,'2026-05-13 07:51:28'),(199,8,7,1,'1212',1,'2026-05-13 08:15:44',NULL,'2026-05-13 08:15:38'),(200,8,7,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/95150524-8c2b-4ede-89d4-a63afe446859.webm|12',1,'2026-05-13 08:41:35',NULL,'2026-05-13 08:40:35'),(201,7,11,4,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/voice/2026/05/b600cb90-5375-4957-acbc-faa68f9b4f66.webm|1',0,NULL,NULL,'2026-05-13 13:58:00'),(202,7,11,2,'https://dengya.oss-cn-beijing.aliyuncs.com/chat/images/2026/05/e1575ffb-1c3d-490d-97ad-165a82350c2d.jpeg',0,NULL,NULL,'2026-05-13 13:58:06');
/*!40000 ALTER TABLE `message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_read`
--

DROP TABLE IF EXISTS `notification_read`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_read` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `notification_id` bigint NOT NULL COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `read_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notification_user` (`notification_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知已读记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_read`
--

LOCK TABLES `notification_read` WRITE;
/*!40000 ALTER TABLE `notification_read` DISABLE KEYS */;
INSERT INTO `notification_read` VALUES (1,1,8,'2026-05-12 09:18:08'),(2,2,7,'2026-05-12 12:02:14'),(3,1,7,'2026-05-12 12:02:17'),(4,2,8,'2026-05-12 12:45:45'),(5,3,8,'2026-05-12 12:46:27'),(6,3,7,'2026-05-12 13:08:35'),(7,4,7,'2026-05-12 14:09:37'),(8,4,8,'2026-05-12 14:09:49'),(9,4,9,'2026-05-12 17:34:45'),(10,3,9,'2026-05-12 17:34:47'),(11,5,7,'2026-05-12 19:19:09'),(12,5,8,'2026-05-12 22:07:02'),(13,6,8,'2026-05-13 07:50:51'),(14,6,7,'2026-05-13 07:51:19'),(15,7,7,'2026-05-13 08:15:08'),(16,7,8,'2026-05-13 08:15:25'),(17,8,7,'2026-05-13 08:16:05'),(18,9,7,'2026-05-13 08:16:58'),(19,9,8,'2026-05-13 08:17:10'),(20,8,8,'2026-05-13 08:17:24'),(21,10,8,'2026-05-13 14:02:55');
/*!40000 ALTER TABLE `notification_read` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_notification`
--

DROP TABLE IF EXISTS `system_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知内容',
  `admin_id` bigint NOT NULL COMMENT '发布管理员ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_notification`
--

LOCK TABLES `system_notification` WRITE;
/*!40000 ALTER TABLE `system_notification` DISABLE KEYS */;
INSERT INTO `system_notification` VALUES (1,'fdsf','dfsdfsdf',7,'2026-05-12 09:17:54'),(2,'hgfhx','gfdgdfg',7,'2026-05-12 12:01:54'),(3,'fdsfd','sdfafasdf',7,'2026-05-12 12:46:15'),(4,'1111111111111111111','11111111111111111111111111111111111',7,'2026-05-12 14:09:20'),(5,'eee','gfddddddddddddddddddddddddd',7,'2026-05-12 19:18:58'),(6,'rerwe','dfffffffffffffffffffffffffa',7,'2026-05-13 07:50:42'),(7,'fgdfg','dfgdfgdfgdfggd\nhfghf',7,'2026-05-13 08:15:01'),(8,'erwe','fsdfsdf',7,'2026-05-13 08:15:55'),(9,'gg','gfdgdfg',7,'2026-05-13 08:16:52'),(10,'11111111','11111111111',7,'2026-05-13 14:02:09');
/*!40000 ALTER TABLE `system_notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `unread_message_stats`
--

DROP TABLE IF EXISTS `unread_message_stats`;
/*!50001 DROP VIEW IF EXISTS `unread_message_stats`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `unread_message_stats` AS SELECT 
 1 AS `user_id`,
 1 AS `friend_id`,
 1 AS `unread_count`,
 1 AS `last_send_time`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(BCrypt加密)',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
  `avatar` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT 'https://dengya.oss-cn-beijing.aliyuncs.com/avatar.png',
  `signature` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '个性签名',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `last_login_ip` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后登录IP',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user' COMMENT '角色: user-普通用户, admin-管理员',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态:0禁用,1正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','$2a$10$NkMJU3xZJ2QJ2QJ2QJ2QuJ2QJ2QJ2QJ2QJ2Q','系统管理员','https://dengya.oss-cn-beijing.aliyuncs.com/avatar.png',NULL,NULL,NULL,NULL,'admin',1,'2026-05-07 17:07:06','2026-05-08 07:25:19'),(2,'zhangsan','$2a$10$NkMJU3xZJ2QJ2QJ2QJ2QuJ2QJ2QJ2QJ2QJ2Q','张三','https://dengya.oss-cn-beijing.aliyuncs.com/avatar.png','好好学习，天天向上','zhangsan@example.com',NULL,NULL,'user',1,'2026-05-07 17:07:06','2026-05-08 07:25:19'),(3,'lisi','$2a$10$NkMJU3xZJ2QJ2QJ2QJ2QuJ2QJ2QJ2QJ2QJ2Q','李四','https://dengya.oss-cn-beijing.aliyuncs.com/avatar.png','代码改变世界','lisi@example.com',NULL,NULL,'user',1,'2026-05-07 17:07:06','2026-05-08 07:25:19'),(4,'wangwu','$2a$10$NkMJU3xZJ2QJ2QJ2QJ2QuJ2QJ2QJ2QJ2QJ2Q','王五','https://dengya.oss-cn-beijing.aliyuncs.com/avatar.png','热爱生活','wangwu@example.com',NULL,NULL,'user',1,'2026-05-07 17:07:06','2026-05-08 07:25:19'),(5,'zhaoliu','$2a$10$NkMJU3xZJ2QJ2QJ2QJ2QuJ2QJ2QJ2QJ2QJ2Q','赵六','https://dengya.oss-cn-beijing.aliyuncs.com/avatar.png','健身爱好者','zhaoliu@example.com',NULL,NULL,'user',1,'2026-05-07 17:07:06','2026-05-08 07:25:19'),(6,'dengyta','$2a$10$cbeqH07lMCBmphbARsvg3.WkiGCdnkwW1B5.K.kGYHMyRWVpquGLS','dy','https://dengya.oss-cn-beijing.aliyuncs.com/avatar.png',NULL,NULL,NULL,NULL,'user',1,'2026-05-07 22:23:29','2026-05-08 07:25:19'),(7,'dengya','$2a$10$HyJqmsqzzOzwj38t0jvn5OgUbB5Z4/jrbEwo6.lkaxSBV9EKyJR8q','爸爸','https://dengya.oss-cn-beijing.aliyuncs.com/avatar/2026/05/3be61c0d-727b-45d4-9653-33ab6576d460.jpg','哈哈',NULL,NULL,'2026-05-12 23:04:33','admin',1,'2026-05-07 22:24:15','2026-05-13 09:10:30'),(8,'gaona','$2a$10$zEAOqyO.r94NLJad43RkUuvCEr2uGC.NBpMLMkf.ilfQImpdTEdCq','妈妈','https://dengya.oss-cn-beijing.aliyuncs.com/avatar/2026/05/f41f8c6d-0e32-4e98-8d7a-20656fc43a51.jpeg',NULL,NULL,NULL,'2026-05-13 08:58:50','user',1,'2026-05-07 23:04:21','2026-05-12 14:09:03'),(9,'molun','$2a$10$UMNOOX7gCyNIc/7rsq3fpObRNafFZwfoz8R4l5dvCh46VyoHssORu','ml','https://dengya.oss-cn-beijing.aliyuncs.com/avatar.png','阳光爱笑',NULL,NULL,'2026-05-12 17:29:01','user',1,'2026-05-08 08:07:45','2026-05-08 18:02:21'),(10,'xiaoming','$2a$10$Vhh4.Jt76UdLdJe2KHzzLOqIsZkmkfg/JGFFzPYsZ8sVQ/9ZTAf5G','xm','https://dengya.oss-cn-beijing.aliyuncs.com/avatar.png',NULL,NULL,NULL,'2026-05-12 16:54:27','user',1,'2026-05-08 08:15:08','2026-05-08 08:15:08'),(11,'AAAA','$2a$10$O/.PzEd4udfq4/xVZ/Ywz.dwK8ZWB7O6d/.SVhNjKMH/jRm.u3uaC','AA','https://dengya.oss-cn-beijing.aliyuncs.com/avatar.png',NULL,NULL,NULL,'2026-05-12 17:29:21','user',0,'2026-05-08 23:41:11','2026-05-08 23:41:11');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Final view structure for view `unread_message_stats`
--

/*!50001 DROP VIEW IF EXISTS `unread_message_stats`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `unread_message_stats` AS select `message`.`to_user_id` AS `user_id`,`message`.`from_user_id` AS `friend_id`,count(0) AS `unread_count`,max(`message`.`send_time`) AS `last_send_time` from `message` where ((`message`.`is_read` = 0) and (`message`.`recall_time` is null)) group by `message`.`to_user_id`,`message`.`from_user_id` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-13 16:25:36
