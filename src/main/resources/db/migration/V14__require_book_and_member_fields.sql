-- Book name/isbn and member name/email/password are required. The API rejects requests without them with
-- 400, and these constraints back that up for writes that bypass it. The spring-data-jpa branch's schema
-- declares the same columns NOT NULL.
ALTER TABLE book
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN isbn SET NOT NULL;

ALTER TABLE member
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN email SET NOT NULL,
    ALTER COLUMN password SET NOT NULL;
