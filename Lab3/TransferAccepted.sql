SET autocommit=0;

-- Transfer 30 Dollars from Account number 1 to Account number 2. Assuming Business Logic will set status to accepted.
START TRANSACTION;

SAVEPOINT before_update_account;
-- Debit account number 1.
UPDATE account
SET acc_balance = acc_balance - 30 WHERE acc_number = 1;

SAVEPOINT before_insert_transfer;
-- Insert a new Transfer record, with waiting status.
INSERT INTO transfer values(435, 1, 2, '2023-11-04', 'Joy', 'waiting');

-- Assuming some business logic sets the transfer status to 'accepted'
UPDATE transfer
SET status = 'accepted'
WHERE transfer_id=435;

-- Credit receiver with 30 dollars and commit Transaction as transfer status is accepted.
UPDATE account
SET acc_balance = acc_balance + 30 WHERE acc_number = 2;

-- Commit these all updates, as transfer was accepted.
COMMIT;