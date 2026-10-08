-- Dev-only bulk data for observing long transactions in queries that have no index to use, the same rows as the
-- spring-data-jpa branch's 015-long-transaction-demo-data.xml. Only the primary keys are indexed, so with these
-- volumes the following run as sequential scans:
-- - book search (upper(name) LIKE '%...%') over ~300,000 books;
-- - member search by name or email over ~100,000 members;
-- - the borrow eligibility checks (borrowing.member_id) inside BorrowingService.borrowBook's write transaction,
--   and the outstanding-loan lookup (borrowing.book_id) inside returnBook's, over ~1,000,000 borrowings - the same
--   scan also backs the foreign-key check when a book is deleted;
-- - the fine rate lookup (book_id, effective_date) in PaymentService.handleAssignFine over ~500,000 rates.
-- The extra borrowings reuse the 2,000 books and 200 members of V17. Every borrowing is RETURNED and every book
-- available, so borrowing and returning still work as before.
INSERT INTO book (id, name, isbn, available)
SELECT md5('lt-book-' || g)::uuid,
       'Catalogue Volume ' || lpad(g::text, 6, '0'),
       '979' || lpad(g::text, 10, '0'),
       TRUE
FROM generate_series(1, 300000) AS g;

INSERT INTO member (id, name, email, password, enabled)
SELECT md5('lt-member-' || g)::uuid,
       'Registered Reader ' || lpad(g::text, 6, '0'),
       'reader.' || g || '@library.example.com',
       'password',
       TRUE
FROM generate_series(1, 100000) AS g;

INSERT INTO borrowing (id, book_id, requested_book_id, member_id, borrow_date, return_date, status)
SELECT md5('lt-borrowing-' || g)::uuid,
       md5('n1-book-' || (((g - 1) % 2000) + 1))::uuid,
       md5('n1-book-' || (((g - 1) % 2000) + 1))::uuid,
       md5('n1-member-' || (((g - 1) % 200) + 1))::uuid,
       TIMESTAMP '2020-01-01 10:00:00' + g * INTERVAL '1 minute',
       TIMESTAMP '2020-01-08 10:00:00' + g * INTERVAL '1 minute',
       'RETURNED'
FROM generate_series(1, 1000000) AS g;

INSERT INTO book_fine_rate (id, book_id, price_per_day_overdue, effective_date)
SELECT md5('lt-fine-rate-' || g)::uuid,
       md5('lt-book-' || (((g - 1) % 300000) + 1))::uuid,
       0.50 + (g % 10) * 0.25,
       DATE '2020-01-01' + (g % 1500)
FROM generate_series(1, 500000) AS g;

ANALYZE book;
ANALYZE member;
ANALYZE borrowing;
ANALYZE book_fine_rate;
