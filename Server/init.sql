DROP DATABASE IF EXISTS `xenchatserver` ;
CREATE DATABASE IF NOT EXISTS `xenchatserver` DEFAULT CHARACTER SET utf8 COLLATE utf8_bin;
USE `xenchatserver`;
CREATE TABLE user (
    username VARCHAR(20) PRIMARY KEY NOT NULL,
    passwordMD5 CHAR(32) NOT NULL,
    status INT NOT NULL,
    lastLoginTime DATETIME NOT NULL,
    IP CHAR(15) NOT NULL,
    serverPort INT NOT NULL,
    fileServerPort INT NOT NULL
);
CREATE TABLE friend (
    username VARCHAR(20) NOT NULL,
    friendName VARCHAR(20) NOT NULL,
    PRIMARY KEY (username, friendName)
);

CREATE TABLE message(
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender VARCHAR(20) NOT NULL,
    receiver VARCHAR(20) NOT NULL,
    time DATETIME NOT NULL,
    type INT NOT NULL,
    data BLOB,
    INDEX idx_receiver (receiver),
    INDEX inx_sender (sender)
);

CREATE TABLE fileInfos(
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender VARCHAR(20) NOT NULL,
    receiver VARCHAR(20) NOT NULL,
    time DATETIME NOT NULL,
    fileName VARCHAR(100) NOT NULL,
    MD5 CHAR(32) NOT NULL,
    totalSize INT NOT NULL,
    sentSize INT NOT NULL,
    receivedSize INT NOT NULL,
    INDEX idx_sender (sender),
    INDEX idx_receiver (receiver),
    INDEX idx_MD5 (MD5)
);

CREATE TABLE fileChunks(
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender VARCHAR(20) NOT NULL,
    receiver VARCHAR(20) NOT NULL,
    MD5 CHAR(32) NOT NULL,
    chunkIndex INT NOT NULL,
    data BLOB,
    INDEX idx_sender (sender),
    INDEX idx_receiver (receiver),
    INDEX idx_MD5 (MD5)
);