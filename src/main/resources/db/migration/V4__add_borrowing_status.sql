-- Add status column to borrowing table
ALTER TABLE borrowing ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';