package org.jetbrains.conf.bookify.members;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/members")
class MemberController {

    private final MemberService memberService;

    MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * Get all members
     * @return a list of all members
     */
    @GetMapping("")
    ResponseEntity<List<Member>> getAll() {
        List<Member> memberList = memberService.findAll();
        return new ResponseEntity<>(memberList, HttpStatus.OK);
    }

    /**
     * Get all active members
     * @return a list of all active members
     */
    @GetMapping("/active")
    ResponseEntity<List<Member>> getAllActive() {
        List<Member> memberList = memberService.findAllActive();
        return new ResponseEntity<>(memberList, HttpStatus.OK);
    }

    /**
     * Add a new member
     * @param member the member to add
     * @return the added member
     */
    @PostMapping("")
    ResponseEntity<Member> addMember(@RequestBody Member member) {
        Member savedMember = memberService.addMember(member);
        return new ResponseEntity<>(savedMember, HttpStatus.CREATED);
    }

    /**
     * Disable a member
     * @param id the id of the member to disable
     * @return the disabled member or 404 if not found
     */
    @PutMapping("/{id}/disable")
    ResponseEntity<Member> disableMember(@PathVariable UUID id) {
        Optional<Member> disabledMember = memberService.disableMember(id);
        return disabledMember
                .map(member -> new ResponseEntity<>(member, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Search for members by name
     * @param name the name to search for
     * @return a list of members matching the search criteria
     */
    @GetMapping("/search")
    ResponseEntity<List<Member>> searchMembers(
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
        
        return new ResponseEntity<>(members, HttpStatus.OK);
    }

    /**
     * Get a member by id
     * @param id the id of the member
     * @return the member or 404 if not found
     */
    @GetMapping("/{id}")
    ResponseEntity<Member> getMemberById(@PathVariable UUID id) {
        Optional<Member> member = memberService.findById(id);
        return member
                .map(m -> new ResponseEntity<>(m, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}