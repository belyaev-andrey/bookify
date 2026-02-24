-- Insert a default librarian user (password is 'password' encoded with BCrypt)
INSERT INTO users (username, password, enabled)
VALUES ('librarian', '$2a$10$YKyJ5KAYVg7lbwoIzvtXOOxe2VPEvat7IY4AkgQp2mPnZjZw6.58C', true);

INSERT INTO authorities (username, authority)
VALUES ('librarian', 'ROLE_LIBRARIAN');
