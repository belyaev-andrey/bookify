-- Create borrowing table
CREATE TABLE borrowing (
    id UUID PRIMARY KEY,
    book_id UUID,
    requested_book_id UUID NOT NULL,
    member_id UUID NOT NULL,
    borrow_date TIMESTAMP,
    return_date TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY (book_id) REFERENCES book(id),
    FOREIGN KEY (member_id) REFERENCES member(id)
);