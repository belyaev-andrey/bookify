-- The overdue-return debug data from V13 is only meant for local debugging, but V13 sits with the schema
-- migrations, so it was loaded in every profile, prod and test included. Remove it again here;
-- data/V16__overdue_return_debug_data.sql inserts the same rows for the dev profile only.
-- Borrowings go first: any that reference the debug book or member would block deleting them.
DELETE FROM borrowing
WHERE book_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16'
   OR requested_book_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16'
   OR member_id = 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15';

DELETE FROM book_fine_rate WHERE book_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16';

DELETE FROM member WHERE id = 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15';

DELETE FROM book WHERE id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16';
