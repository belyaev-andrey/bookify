-- A disabled member, seeded so GET /api/members/active has something to omit for a plain
-- LIBRARIAN but include for a caller who also holds ROLE_ADMIN (see MemberController.getAllActive).
INSERT INTO member (id, name, email, password, enabled) VALUES ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'Alice Cooper', 'alice.cooper@example.com', 'passwordabc', false);
