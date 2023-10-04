SET autocommit=0;

-- Transfer 30 Dollars from Account number 1 to Account number 2. Assuming Business Logic will set status to declined.
START TRANSACTION;

SAVEPOINT before_update_account;
-- Debit account number 1.
UPDATE account
SET acc_balance = acc_balance - 30 WHERE acc_number = 1;

SAVEPOINT before_insert_transfer;
-- Insert a new Transfer record, with waiting status.
INSERT INTO transfer values(435, 1, 2, '2023-11-04', 'Joy', 'waiting');

-- Assuming some business logic sets the transfer status to 'declined'
UPDATE transfer
SET status = 'declined'
WHERE transfer_id=435;

-- Rollback to savepoint, to undo the debit as transfer was declined.
ROLLBACK TO before_update_account;