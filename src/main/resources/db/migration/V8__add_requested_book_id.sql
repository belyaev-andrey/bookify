-- Add requested_book_id column to borrowing table without a foreign key
ALTER TABLE borrowing ADD COLUMN requested_book_id UUID;