package org.jetbrains.conf.bookify.payments;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

interface BookFineRateRepository extends CrudRepository<BookFineRateEntity, UUID> {

    @Query("""
            SELECT * FROM book_fine_rate
            WHERE book_id = :bookId AND effective_date <= :date
            ORDER BY effective_date DESC
            LIMIT 1
            """)
    Optional<BookFineRateEntity> findApplicableRate(@Param("bookId") UUID bookId, @Param("date") LocalDate date);
}
