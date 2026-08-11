package org.jetbrains.conf.bookify.members;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
interface MemberMapper {

    Member toEntity(MemberRequest request);

    MemberResponse toResponse(Member member);

    List<MemberResponse> toResponseList(List<Member> members);
}
