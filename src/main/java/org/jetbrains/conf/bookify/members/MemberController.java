/*
 * Test
 */

package org.jetbrains.conf.bookify.members;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/members")
class MemberController {

    private static final Logger log = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;
    private final MemberMapper memberMapper;

    MemberController(MemberService memberService, MemberMapper memberMapper) {
        this.memberService = memberService;
        this.memberMapper = memberMapper;
    }

    /**
     * Get all members
     *
     * @return a list of all members
     */
    @GetMapping("")
    ResponseEntity<List<MemberResponse>> getAll() {
        List<Member> memberList = memberService.findAll();
        return new ResponseEntity<>(memberMapper.toResponseList(memberList), HttpStatus.OK);
    }

    /**
     * Get all active members. Callers who also hold ROLE_ADMIN (on top of the LIBRARIAN role this
     * endpoint requires) get every member, disabled ones included; a plain librarian only sees the
     * active roster.
     *
     * @return a list of members, scoped by the caller's roles
     */
    @GetMapping("/active")
    ResponseEntity<List<MemberResponse>> getAllActive(Authentication authentication) {
        log.info("Principal: {}", authentication.getPrincipal());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(
                        authority -> Objects.equals(authority.getAuthority(), "ROLE_ADMIN"));
        List<Member> memberList = isAdmin ? memberService.findAll() : memberService.findAllActive();
        return new ResponseEntity<>(memberMapper.toResponseList(memberList), HttpStatus.OK);
    }

    /**
     * Add a new member
     *
     * @param request the member to add
     * @return the added member
     */
    @PostMapping("")
    ResponseEntity<Object> addMember(@RequestBody MemberRequest request) {
        Member savedMember = memberService.addMember(memberMapper.toEntity(request));
        return ResponseEntity.created(URI.create("/api/members/%s".formatted(savedMember.getId()))).build();
    }

    /**
     * Disable a member
     *
     * @param id the id of the member to disable
     * @return the disabled member or 404 if not found
     */
    @PutMapping("/{id}/disable")
    ResponseEntity<MemberResponse> disableMember(@PathVariable UUID id) {
        Optional<Member> disabledMember = memberService.disableMember(id);
        return disabledMember
                .map(member -> new ResponseEntity<>(memberMapper.toResponse(member), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Search for members by name
     *
     * @param name the name to search for
     * @return a list of members matching the search criteria
     */
    @GetMapping("/search")
    ResponseEntity<List<MemberResponse>> searchMembers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email) {

        List<Member> members;
        if (name != null && !name.isEmpty()) {
            members = memberService.searchMembersByName(name);
        } else if (email != null && !email.isEmpty()) {
            members = memberService.searchMembersByEmail(email);
        } else {
            members = memberService.findAll();
        }

        return new ResponseEntity<>(memberMapper.toResponseList(members), HttpStatus.OK);
    }

    /**
     * Get a member by id
     *
     * @param id the id of the member
     * @return the member or 404 if not found
     */
    @GetMapping("/{id}")
    ResponseEntity<MemberResponse> getMemberById(@PathVariable UUID id) {
        Optional<Member> member = memberService.findById(id);
        return member
                .map(m -> new ResponseEntity<>(memberMapper.toResponse(m), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}