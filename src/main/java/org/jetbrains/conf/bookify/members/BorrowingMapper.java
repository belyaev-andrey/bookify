package org.jetbrains.conf.bookify.members;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
interface BorrowingMapper {

    BorrowingResponse toResponse(Borrowing borrowing);

    List<BorrowingResponse> toResponseList(List<Borrowing> borrowings);
}
