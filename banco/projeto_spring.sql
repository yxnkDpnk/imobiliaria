-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: projeto_spring
-- ------------------------------------------------------
-- Server version	8.0.46-0ubuntu0.24.04.4

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
-- Table structure for table `bairros`
--

DROP TABLE IF EXISTS `bairros`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bairros` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(255) NOT NULL,
  `cidade` varchar(255) NOT NULL,
  `estado` varchar(2) DEFAULT NULL,
  `cep_inicial` varchar(255) NOT NULL,
  `cep_final` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bairros`
--

LOCK TABLES `bairros` WRITE;
/*!40000 ALTER TABLE `bairros` DISABLE KEYS */;
INSERT INTO `bairros` VALUES (1,'Centro','Porto Alegre','RS','90010-000','90010-999'),(2,'Moinhos de Vento','Porto Alegre','RS','90570-000','90570-999'),(3,'Cidade Baixa','Porto Alegre','RS','90050-000','90050-999'),(4,'Centro','Canoas','RS','92010-000','92010-999'),(5,'Centro','Gramado','RS','95670-000','95670-999');
/*!40000 ALTER TABLE `bairros` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fotos_imoveis`
--

DROP TABLE IF EXISTS `fotos_imoveis`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fotos_imoveis` (
  `id` int NOT NULL AUTO_INCREMENT,
  `caminho` varchar(255) DEFAULT NULL,
  `capa` bit(1) DEFAULT NULL,
  `nome_arquivo` varchar(255) DEFAULT NULL,
  `ordem` int DEFAULT NULL,
  `imovel_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlxjlrrwj621jii9i201qr6x3d` (`imovel_id`),
  CONSTRAINT `FKlxjlrrwj621jii9i201qr6x3d` FOREIGN KEY (`imovel_id`) REFERENCES `imoveis` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fotos_imoveis`
--

LOCK TABLES `fotos_imoveis` WRITE;
/*!40000 ALTER TABLE `fotos_imoveis` DISABLE KEYS */;
/*!40000 ALTER TABLE `fotos_imoveis` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `imoveis`
--

DROP TABLE IF EXISTS `imoveis`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `imoveis` (
  `id` int NOT NULL AUTO_INCREMENT,
  `titulo` varchar(255) NOT NULL,
  `descricao` text,
  `preco_venda` decimal(15,2) DEFAULT NULL,
  `preco_aluguel` decimal(15,2) DEFAULT NULL,
  `finalidade` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `dormitorios` int DEFAULT NULL,
  `banheiros` int DEFAULT NULL,
  `garagem` int DEFAULT NULL,
  `area_total` decimal(10,2) DEFAULT NULL,
  `area_construida` decimal(10,2) DEFAULT NULL,
  `endereco` varchar(255) DEFAULT NULL,
  `numero` varchar(255) DEFAULT NULL,
  `complemento` varchar(255) DEFAULT NULL,
  `cep` varchar(255) DEFAULT NULL,
  `caracteristicas` text,
  `destaque` tinyint(1) DEFAULT '0',
  `tipo_imovel_id` int DEFAULT NULL,
  `bairro_id` int DEFAULT NULL,
  `usuario_id` int DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_imoveis_tipo_imovel` (`tipo_imovel_id`),
  KEY `fk_imoveis_bairro` (`bairro_id`),
  KEY `fk_imoveis_usuario` (`usuario_id`),
  CONSTRAINT `fk_imoveis_bairro` FOREIGN KEY (`bairro_id`) REFERENCES `bairros` (`id`),
  CONSTRAINT `fk_imoveis_tipo_imovel` FOREIGN KEY (`tipo_imovel_id`) REFERENCES `tipo_imovel` (`id`),
  CONSTRAINT `fk_imoveis_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `imoveis`
--

LOCK TABLES `imoveis` WRITE;
/*!40000 ALTER TABLE `imoveis` DISABLE KEYS */;
INSERT INTO `imoveis` VALUES (1,'Casa ampla com piscina','Excelente casa em bairro tranquilo, próximo a escolas e comércios.',850000.00,3500.00,'Residencial','Disponível',3,2,2,300.50,180.75,'Rua das Flores','123','Casa 02','12345-678','Piscina, Churrasqueira, Jardim',1,1,1,NULL,'2026-09-30 00:31:02','2026-09-30 00:31:02');
/*!40000 ALTER TABLE `imoveis` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tipo_imovel`
--

DROP TABLE IF EXISTS `tipo_imovel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tipo_imovel` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(255) NOT NULL,
  `descricao` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tipo_imovel_nome` (`nome`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tipo_imovel`
--

LOCK TABLES `tipo_imovel` WRITE;
/*!40000 ALTER TABLE `tipo_imovel` DISABLE KEYS */;
INSERT INTO `tipo_imovel` VALUES (1,'Casa','Casa residencial térrea ou sobrado'),(2,'Apartamento','Unidade em prédio residencial'),(3,'Terreno','Lote sem construção'),(4,'Sala comercial','Espaço para escritório ou comércio'),(5,'Cobertura','Apartamento no último andar, geralmente com área externa');
/*!40000 ALTER TABLE `tipo_imovel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `senha` varchar(255) DEFAULT NULL,
  `tipo` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKkfsp0s1tflm1cwlj8idhqsad0` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'Administrador','admin@imobiliaria.com','$2a$10$/TcbMx5MdPcJy7.qqvDyIuIGfy2HR8LTRi33VdWNQqi2WT6UmiYma','ADMIN'),(2,'Maria Silva','cliente@imobiliaria.com','$2a$10$wqPxfMVJr0lpqqSY2KtJ6Oz9JgIaHU2ntbXFaO.JIwfrEcsKuXUd2','USER'),(3,'João Souza','usuario3@imobiliaria.com','$2a$10$mAehenbY/v6mV0cCKztEUuLFszVLfyJVksidACOm5Oly7Q0FR6hJi','USER');
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-10 19:55:19
