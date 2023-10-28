CREATE DATABASE e_commerce;
USE e_commerce;
CREATE TABLE user(user_id int primary key, name varchar(20), email varchar(30), phone varchar(10), address varchar(255));
CREATE TABLE order_info(order_id int primary key, user_id int, item_name varchar(20), quantity int, order_date date, FOREIGN KEY (user_id) REFERENCES user(user_id));
INSERT into user values(1,'Aditya','aditya.purohit@dal.ca','123456','Halifax, NS');