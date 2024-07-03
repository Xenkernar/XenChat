DROP DATABASE IF EXISTS `xenchatclient` ;

CREATE DATABASE IF NOT EXISTS `xenchatclient` DEFAULT CHARACTER SET utf8 COLLATE utf8_bin;
USE `xenchatclient`;
CREATE TABLE message(
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        username VARCHAR(20) NOT NULL,
                        sender VARCHAR(20) NOT NULL,
                        receiver VARCHAR(20) NOT NULL,
                        time DATETIME NOT NULL,
                        type INT NOT NULL,
                        data BLOB,
                        INDEX idx_username (username),
                        INDEX idx_receiver (receiver),
                        INDEX inx_sender (sender)
);

CREATE TABLE sendFileInfo(
                             id INT PRIMARY KEY AUTO_INCREMENT,
                             username VARCHAR(20) NOT NULL,
                             receiver VARCHAR(20) NOT NULL,
                             time DATETIME NOT NULL,
                             fileName VARCHAR(100) NOT NULL,
                             MD5 CHAR(32) NOT NULL,
                             totalSize INT NOT NULL,
                             sentSize INT NOT NULL,
                             INDEX idx_username (username),
                             INDEX idx_receiver (receiver),
                             INDEX idx_MD5 (MD5)
);

CREATE TABLE receiveFileInfo(
                                id INT PRIMARY KEY AUTO_INCREMENT,
                                username VARCHAR(20) NOT NULL,
                                sender VARCHAR(20) NOT NULL,
                                time DATETIME NOT NULL,
                                fileName VARCHAR(100) NOT NULL,
                                MD5 CHAR(32) NOT NULL,
                                totalSize INT NOT NULL,
                                receivedSize INT NOT NULL,
                                INDEX idx_username (username),
                                INDEX idx_sender (sender),
                                INDEX idx_MD5 (MD5)
);

CREATE TABLE sendFiles(
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          username VARCHAR(20) NOT NULL,
                          receiver VARCHAR(20) NOT NULL,
                          MD5 CHAR(32) NOT NULL,
                          chunkIndex INT NOT NULL,
                          data BLOB,
                          INDEX idx_username (username),
                          INDEX idx_receiver (receiver),
                          INDEX idx_MD5 (MD5)
);

CREATE TABLE receiveFiles(
                             id INT PRIMARY KEY AUTO_INCREMENT,
                             username VARCHAR(20) NOT NULL,
                             sender VARCHAR(20) NOT NULL,
                             MD5 CHAR(32) NOT NULL,
                             chunkIndex INT NOT NULL,
                             data BLOB,
                             INDEX idx_username (username),
                             INDEX idx_sender (sender),
                             INDEX inx_MD5 (MD5)
);
