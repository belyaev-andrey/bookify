CREATE TABLE borrowing (
    id UUID PRIMARY KEY,
    book_id UUID NOT NULL,
    member_id UUID NOT NULL,
    borrow_date TIMESTAMP NOT NULL,
    return_date TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES book(id),
    FOREIGN KEY (member_id) REFERENCES member(id)
);

ALTER TABLE book ADD COLUMN available BOOLEAN DEFAULT TRUE;