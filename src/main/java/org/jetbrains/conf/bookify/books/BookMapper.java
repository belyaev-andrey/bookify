package org.jetbrains.conf.bookify.books;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
interface BookMapper {

    @Mapping(target = "available", source = "available", defaultValue = "false")
    Book toEntity(BookRequest request);

    @Mapping(target = "available", source = "available", defaultValue = "false")
    Book toEntity(BookUpdateRequest request);

    BookResponse toResponse(Book book);

    List<BookResponse> toResponseList(List<Book> books);
}
