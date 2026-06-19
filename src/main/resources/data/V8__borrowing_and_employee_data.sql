INSERT INTO employee (organization, employee_number, name, birth_date, email)
VALUES ('LIBRARY', 101, 'Eleanor Vance', '1985-03-12', 'evance@mail.com'),
       ('SCIENCE', 201, 'Caleb Hayes', '1992-07-21', 'chayes@mail.com'),
       ('ACADEMY', 301, 'Isla Bennett', '1988-11-02', 'ibennett@mail.com');

-- Insert sample borrowing data with employee references
INSERT INTO borrowing (id, book_id, requested_book_id, member_id, borrow_date, return_date, status,
                       employee_organization, employee_number)
VALUES ('550e8400-e29b-41d4-a716-446655440002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', '2024-01-10 14:15:00',
        '2024-01-20 16:45:00', 'RETURNED', 'SCIENCE', 201),
       ('550e8400-e29b-41d4-a716-446655440003', NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13',
        'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', '2024-01-18 09:00:00', NULL, 'PENDING', 'ACADEMY', 301);