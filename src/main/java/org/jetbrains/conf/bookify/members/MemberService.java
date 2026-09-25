package org.jetbrains.conf.bookify.members;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
class MemberService {

    static final String MEMBERS_CACHE = "members";
    static final String ALL_MEMBERS_CACHE = "allMembers";
    static final String ACTIVE_MEMBERS_CACHE = "activeMembers";

    private final MemberRepository memberRepository;

    MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * Add a new member
     * @param member the member to add
     * @return the saved member
     */
    @Transactional
    @CacheEvict(cacheNames = {ALL_MEMBERS_CACHE, ACTIVE_MEMBERS_CACHE}, allEntries = true)
    Member addMember(Member member) {
        return memberRepository.save(member);
    }

    /**
     * Disable a member
     * @param id the id of the member to disable
     * @return the updated member if found, empty otherwise
     */
    @Transactional
    @PreAuthorize("hasRole('SUPERVISOR')")
    @Caching(evict = {
            @CacheEvict(cacheNames = MEMBERS_CACHE, key = "#id"),
            @CacheEvict(cacheNames = {ALL_MEMBERS_CACHE, ACTIVE_MEMBERS_CACHE}, allEntries = true)
    })
    Optional<Member> disableMember(UUID id) {
        Optional<Member> memberOpt = memberRepository.findById(id);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            member.setEnabled(false);
            return Optional.of(memberRepository.save(member));
        }
        return Optional.empty();
    }

    /**
     * Search for members by name
     * @param name the name to search for
     * @return a list of members matching the search criteria
     */
    @Transactional(readOnly = true)
    List<Member> searchMembersByName(String name) {
        return memberRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Search for members by email
     * @param email the email to search for
     * @return a list of members matching the search criteria
     */
    @Transactional(readOnly = true)
    List<Member> searchMembersByEmail(String email) {
        return memberRepository.findByEmailContainingIgnoreCase(email);
    }

    /**
     * Get all members
     * @return a list of all members
     */
    @Transactional(readOnly = true)
    @Cacheable(ALL_MEMBERS_CACHE)
    List<Member> findAll() {
        List<Member> members = new ArrayList<>();
        memberRepository.findAll().forEach(members::add);
        return members;
    }

    /**
     * Get all active members
     * @return a list of all active members
     */
    @Transactional(readOnly = true)
    @Cacheable(ACTIVE_MEMBERS_CACHE)
    List<Member> findAllActive() {
        return memberRepository.findByEnabled(true);
    }

    /**
     * Get a member by its id
     * @param id the id of the member
     * @return the member, if found
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = MEMBERS_CACHE, unless = "#result == null")
    Optional<Member> findById(UUID id) {
        return memberRepository.findById(id);
    }
}
