-- Additional test data for borrowing service tests
-- This data provides comprehensive coverage of different borrowing scenarios

-- Insert additional borrowing records covering various statuses and scenarios
INSERT INTO borrowing (id, book_id, requested_book_id, member_id, borrow_date, return_date, status, employee_organization, employee_number)
VALUES
    -- APPROVED borrowings with active loans (not yet returned)
    ('550e8400-e29b-41d4-a716-446655440010', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '2024-02-01 10:00:00', NULL, 'APPROVED', 'LIBRARY', 101),

    ('550e8400-e29b-41d4-a716-446655440011', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '2024-02-05 14:30:00', NULL, 'APPROVED', 'LIBRARY', 101),

    ('550e8400-e29b-41d4-a716-446655440012', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', '2024-02-10 09:15:00', NULL, 'APPROVED', 'SCIENCE', 201),

    -- RETURNED borrowings with completed returns
    ('550e8400-e29b-41d4-a716-446655440013', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '2024-01-15 11:00:00', '2024-01-25 15:30:00', 'RETURNED', 'ACADEMY', 301),

    ('550e8400-e29b-41d4-a716-446655440014', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', '2024-01-20 13:45:00', '2024-02-01 10:00:00', 'RETURNED', 'LIBRARY', 101),

    ('550e8400-e29b-41d4-a716-446655440015', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', '2024-01-05 08:00:00', '2024-01-12 16:20:00', 'RETURNED', 'SCIENCE', 201),

    -- PENDING borrowings (awaiting approval)
    ('550e8400-e29b-41d4-a716-446655440016', NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', '2024-02-15 12:00:00', NULL, 'PENDING', 'LIBRARY', 101),

    ('550e8400-e29b-41d4-a716-446655440017', NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', '2024-02-16 10:30:00', NULL, 'PENDING', 'ACADEMY', 301),

    -- REJECTED borrowings (book unavailable or request denied)
    ('550e8400-e29b-41d4-a716-446655440018', NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', '2024-02-08 15:00:00', NULL, 'REJECTED', 'SCIENCE', 201),

    ('550e8400-e29b-41d4-a716-446655440019', NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '2024-02-12 11:45:00', NULL, 'REJECTED', 'LIBRARY', 101),

    -- Overdue books (APPROVED but past expected return date - 30 days from borrow)
    ('550e8400-e29b-41d4-a716-446655440020', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', '2023-12-01 09:00:00', NULL, 'APPROVED', 'ACADEMY', 301),

    ('550e8400-e29b-41d4-a716-446655440021', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', '2023-11-15 14:00:00', NULL, 'APPROVED', 'SCIENCE', 201),

    -- Multiple borrowings by same member to test member history
    ('550e8400-e29b-41d4-a716-446655440022', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '2023-12-10 10:00:00', '2023-12-20 11:00:00', 'RETURNED', 'LIBRARY', 101),

    ('550e8400-e29b-41d4-a716-446655440023', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '2023-12-22 09:00:00', '2024-01-05 10:00:00', 'RETURNED', 'ACADEMY', 301),

    -- Same book borrowed multiple times (different time periods)
    ('550e8400-e29b-41d4-a716-446655440024', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', '2023-11-01 08:30:00', '2023-11-15 14:00:00', 'RETURNED', 'SCIENCE', 201),

    ('550e8400-e29b-41d4-a716-446655440025', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', '2023-11-20 10:00:00', '2023-12-05 15:30:00', 'RETURNED', 'LIBRARY', 101);
