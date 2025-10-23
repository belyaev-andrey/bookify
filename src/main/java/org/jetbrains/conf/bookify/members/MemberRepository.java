package org.jetbrains.conf.bookify.members;

import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.UUID;

interface MemberRepository extends ListCrudRepository<Member, UUID> {

    List<Member> findByNameContainingIgnoreCase(String name);

    List<Member> findByEmailContainingIgnoreCase(String email);

    List<Member> findByEnabled(boolean enabled);
}