CREATE USER 'mcanalytics'@'localhost' IDENTIFIED BY 'password';
CREATE DATABASE mcanalytics;
GRANT ALL PRIVILEGES ON mcanalytics.* TO 'mcanalytics'@'localhost' WITH GRANT OPTION;