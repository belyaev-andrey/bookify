-- Test user holding ROLE_ADMIN in addition to ROLE_LIBRARIAN, for MemberController.getAllActive's
-- role-based branch: a plain librarian only sees active members, but a caller who also holds
-- ROLE_ADMIN sees every member, including disabled ones.
INSERT INTO users (username, password, enabled)
VALUES ('testadmin', '$2a$10$YKyJ5KAYVg7lbwoIzvtXOOxe2VPEvat7IY4AkgQp2mPnZjZw6.58C', true);

INSERT INTO authorities (username, authority)
VALUES ('testadmin', 'ROLE_LIBRARIAN'), ('testadmin', 'ROLE_ADMIN');

-- A disabled member for the same test, mirroring the dev-profile seed data.
INSERT INTO member (id, name, email, password, enabled) VALUES ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'Alice Cooper', 'alice.cooper@example.com', 'passwordabc', false);
