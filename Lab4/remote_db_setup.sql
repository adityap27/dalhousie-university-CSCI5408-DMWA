CREATE DATABASE e_commerce;
USE e_commerce;
CREATE TABLE inventory(item_id int primary key, item_name varchar(20), available_quantity int);
INSERT into inventory values(56,'Laptop',10);