package org.jetbrains.conf.bookify.members;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * Add a new member
     * @param member the member to add
     * @return the saved member
     */
    @Transactional
    public Member addMember(Member member) {
        return memberRepository.save(member);
    }

    /**
     * Disable a member
     * @param id the id of the member to disable
     * @return the updated member if found, empty otherwise
     */
    @Transactional
    public Optional<Member> disableMember(UUID id) {
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
    public List<Member> searchMembersByName(String name) {
        return memberRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Search for members by email
     * @param email the email to search for
     * @return a list of members matching the search criteria
     */
    @Transactional(readOnly = true)
    public List<Member> searchMembersByEmail(String email) {
        return memberRepository.findByEmailContainingIgnoreCase(email);
    }

    /**
     * Get all members
     * @return a list of all members
     */
    @Transactional(readOnly = true)
    public List<Member> findAll() {
        List<Member> members = new ArrayList<>();
        memberRepository.findAll().forEach(members::add);
        return members;
    }

    /**
     * Get all active members
     * @return a list of all active members
     */
    @Transactional(readOnly = true)
    public List<Member> findAllActive() {
        return memberRepository.findByEnabled(true);
    }

    /**
     * Get a member by its id
     * @param id the id of the member
     * @return the member, if found
     */
    @Transactional(readOnly = true)
    public Optional<Member> findById(UUID id) {
        return memberRepository.findById(id);
    }
}
