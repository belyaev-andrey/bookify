package org.jetbrains.conf.bookify.members;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
interface BorrowingMapper {

    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "memberId", source = "member.id")
    BorrowingResponse toResponse(Borrowing borrowing);

    List<BorrowingResponse> toResponseList(List<Borrowing> borrowings);
}
