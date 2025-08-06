package org.jetbrains.conf.bookify.members;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

interface MemberRepository extends CrudRepository<Member, UUID> {

    List<Member> findByNameContainingIgnoreCase(String name);

    List<Member> findByEmailContainingIgnoreCase(String email);

    List<Member> findByEnabled(boolean enabled);
}