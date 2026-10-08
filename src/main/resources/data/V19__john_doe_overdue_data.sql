-- Dev-only: John Doe holds three overdue books, the same rows as the spring-data-jpa branch's
-- 016-john-doe-overdue-data.xml. The overdue check in BorrowingService.findIneligibilityReason
-- (member_id = ? AND status = 'APPROVED' AND borrow_date < now - bookify.overdue.days) finds rows, and his borrow
-- requests are rejected with HAS_OVERDUE_BOOKS. Three active loans stay below the borrowing limit (5), so the
-- overdue check is the one that rejects him. Borrow dates are relative, so the loans stay overdue.
INSERT INTO book (id, name, isbn, available)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 'Dune', '9780441172719', false),
       ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'Clean Code', '9780132350884', false),
       ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', 'Refactoring', '9780134757599', false);

INSERT INTO borrowing (id, book_id, requested_book_id, member_id, borrow_date, return_date, status)
VALUES ('550e8400-e29b-41d4-a716-446655440031',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17',
        'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        CURRENT_TIMESTAMP - INTERVAL '30 days', NULL, 'APPROVED'),
       ('550e8400-e29b-41d4-a716-446655440032',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18',
        'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        CURRENT_TIMESTAMP - INTERVAL '25 days', NULL, 'APPROVED'),
       ('550e8400-e29b-41d4-a716-446655440033',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19',
        'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        CURRENT_TIMESTAMP - INTERVAL '20 days', NULL, 'APPROVED');
