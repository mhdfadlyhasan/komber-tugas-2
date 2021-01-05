/*
SQLyog Community v13.1.5  (64 bit)
MySQL - 10.3.16-MariaDB : Database - database_komber
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`database_komber` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `database_komber`;

/*Table structure for table `coordinate` */

DROP TABLE IF EXISTS `coordinate`;

CREATE TABLE `coordinate` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Activity` varchar(255) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=latin1;

/*Data for the table `coordinate` */

LOCK TABLES `coordinate` WRITE;

insert  into `coordinate`(`id`,`Activity`,`latitude`,`longitude`) values 
(1,'Lompat',0,0),
(2,'Lompat',0,0),
(3,'Lompat',0,0),
(4,'Lompat',0,0),
(5,'Lompat',3.57974,98.6147),
(6,'Lompat',3.57974,98.6147),
(7,'Lompat',3.58011,98.6145),
(8,'Lompat',3.58011,98.6145),
(9,'Lompat',3.57999,98.6145),
(10,'Lompat',3.57991,98.6145),
(11,'Lompat',3.57991,98.6145),
(12,'Lompat',3.57991,98.6145),
(13,'Lompat',3.57982,98.6146),
(14,'Lompat',3.57982,98.6146),
(15,'Lompat',3.57974,98.6147),
(16,'Lompat',3.57974,98.6147),
(17,'Lompat',3.57974,98.6147),
(18,'Lompat',3.57974,98.6147),
(19,'Lompat',3.57974,98.6147),
(20,'Lompat',3.57974,98.6147);

UNLOCK TABLES;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
