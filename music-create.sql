CREATE SCHEMA IF NOT EXISTS study;
CREATE TABLE IF NOT EXISTS study.music (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);
INSERT INTO study.music (name) VALUES ('Smells Like Teen Spirit');
INSERT INTO study.music (name) VALUES ('Imagine');
INSERT INTO study.music (name) VALUES ('Hotel California');