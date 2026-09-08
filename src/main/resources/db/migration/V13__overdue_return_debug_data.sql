-- Reproducible production data for debugging the overdue-return payment flow.
-- The loan is 20 days old; with bookify.overdue.days=14 it is 6 days overdue.
INSERT INTO book (id, name, isbn, available)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 'The Pragmatic Programmer', '9780135957059', false);

INSERT INTO member (id, name, email, password, enabled)
VALUES ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'Overdue Reader', 'overdue.reader@example.com', 'password', true);

INSERT INTO borrowing (id, book_id, requested_book_id, member_id, borrow_date, return_date, status)
VALUES ('550e8400-e29b-41d4-a716-446655440030',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16',
        'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15',
        CURRENT_TIMESTAMP - INTERVAL '20 days', NULL, 'APPROVED');

INSERT INTO book_fine_rate (id, book_id, price_per_day_overdue, effective_date)
VALUES ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a05',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 2.50, CURRENT_DATE - 30);
