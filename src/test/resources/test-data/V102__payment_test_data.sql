-- Fine rate history for "The Lord of the Rings" (a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11):
-- three consecutive rates, so tests can exercise picking the most recent rate not after a given date.
INSERT INTO book_fine_rate (id, book_id, price_per_day_overdue, effective_date) VALUES
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 0.50, '2024-01-01'),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 0.75, '2024-06-01'),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 1.00, '2024-09-01');

-- A rate that only takes effect in the future, for "1984" (a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12).
INSERT INTO book_fine_rate (id, book_id, price_per_day_overdue, effective_date) VALUES
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 2.00, '2030-01-01');

-- "To Kill a Mockingbird" (a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13) intentionally has no fine rate configured.
