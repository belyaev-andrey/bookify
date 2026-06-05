package org.jetbrains.conf.bookify.payments;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

interface BookFineRateRepository extends ListCrudRepository<BookFineRateEntity, UUID> {

    @Query("""
            SELECT r FROM BookFineRateEntity r
            WHERE r.bookId = :bookId AND r.effectiveDate <= :date
            ORDER BY r.effectiveDate DESC
            LIMIT 1
            """)
    Optional<BookFineRateEntity> findApplicableRate(@Param("bookId") UUID bookId, @Param("date") LocalDate date);
}
