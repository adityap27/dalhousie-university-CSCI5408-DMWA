DROP DATABASE lab3_transactions;
CREATE DATABASE lab3_transactions;
USE lab3_transactions;

CREATE TABLE customer(
    c_id INT PRIMARY KEY,
    name VARCHAR(30),
    mailing_address VARCHAR(200),
    permanent_address VARCHAR(200),
    email VARCHAR(50) NOT NULL UNIQUE,
    primary_phone_number VARCHAR(10) NOT NULL UNIQUE);

CREATE TABLE account(
    acc_number INT PRIMARY KEY,
    c_id INT,
    acc_balance DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (c_id) REFERENCES customer(c_id));

CREATE TABLE transfer(
    transfer_id INT PRIMARY KEY,
    sender_acc_number INT,
    receiver_acc_number INT,
    date_of_transfer DATE,
    recipient_name VARCHAR(100),
    status VARCHAR(10),
    FOREIGN KEY (sender_acc_number) REFERENCES account(acc_number),
    FOREIGN KEY (receiver_acc_number) REFERENCES account(acc_number));
    
INSERT INTO customer VALUES(1, 'Aditya', 'Halifax', 'Halifax', 'aditya.purohit@dal.ca', 9876543219);
INSERT INTO account VALUES(1, 1, 100.00);
INSERT INTO customer VALUES(2, 'Joy', 'Halifax', 'Halifax', 'joy@gmail.com', 7776543219);
INSERT INTO account VALUES(2, 2, 50.00);

SELECT * FROM CUSTOMER;
SELECT * FROM ACCOUNT;
