-- Dev-only bulk data for the query demo, the same rows as the spring-data-jpa branch's 014-n-plus-one-demo-data.xml.
-- 2,000 books, 200 members and 4,000 returned borrowings, including N+1 Heavy Reader with 1,000 loans of
-- different books. On that branch Borrowing's EAGER book/requestedBook/member make listing these borrowings issue
-- one select per book and member; here Borrowing only holds IDs, so the same request is a single select.
-- md5-derived UUIDs keep the ids stable.
INSERT INTO book (id, name, isbn, available)
SELECT md5('n1-book-' || g)::uuid,
       'N+1 Demo Book ' || lpad(g::text, 4, '0'),
       '978' || lpad(g::text, 10, '0'),
       TRUE
FROM generate_series(1, 2000) AS g;

INSERT INTO member (id, name, email, password, enabled)
SELECT md5('n1-member-' || g)::uuid,
       'N+1 Demo Member ' || lpad(g::text, 3, '0'),
       'n1.member.' || g || '@example.com',
       'password',
       TRUE
FROM generate_series(1, 200) AS g;

INSERT INTO member (id, name, email, password, enabled)
VALUES ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a20', 'N+1 Heavy Reader', 'n1.heavy.reader@example.com',
        'password', TRUE);

-- 1,000 loans for the heavy reader, each of a different book.
INSERT INTO borrowing (id, book_id, requested_book_id, member_id, borrow_date, return_date, status)
SELECT md5('n1-heavy-borrowing-' || g)::uuid,
       md5('n1-book-' || g)::uuid,
       md5('n1-book-' || g)::uuid,
       'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a20',
       TIMESTAMP '2023-01-01 10:00:00' + g * INTERVAL '1 hour',
       TIMESTAMP '2023-01-08 10:00:00' + g * INTERVAL '1 hour',
       'RETURNED'
FROM generate_series(1, 1000) AS g;

-- 3,000 loans spread over the 200 demo members and all 2,000 demo books.
INSERT INTO borrowing (id, book_id, requested_book_id, member_id, borrow_date, return_date, status)
SELECT md5('n1-borrowing-' || g)::uuid,
       md5('n1-book-' || (((g - 1) % 2000) + 1))::uuid,
       md5('n1-book-' || (((g - 1) % 2000) + 1))::uuid,
       md5('n1-member-' || (((g - 1) % 200) + 1))::uuid,
       TIMESTAMP '2023-06-01 10:00:00' + g * INTERVAL '1 hour',
       TIMESTAMP '2023-06-08 10:00:00' + g * INTERVAL '1 hour',
       'RETURNED'
FROM generate_series(1, 3000) AS g;
