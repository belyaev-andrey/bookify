-- Allow null book_id in borrowing table
ALTER TABLE borrowing ALTER COLUMN book_id DROP NOT NULL;