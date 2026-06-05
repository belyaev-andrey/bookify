CREATE TABLE book_fine_rate
(
    id                    UUID PRIMARY KEY,
    book_id               UUID           NOT NULL,
    price_per_day_overdue NUMERIC(10, 2) NOT NULL,
    effective_date        DATE           NOT NULL,
    CONSTRAINT chk_price_non_negative CHECK (price_per_day_overdue >= 0)
);
