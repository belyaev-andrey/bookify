package org.jetbrains.conf.bookify.members;

import org.jetbrains.conf.bookify.DbConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jetbrains.conf.bookify.members.MemberService.ACTIVE_MEMBERS_CACHE;
import static org.jetbrains.conf.bookify.members.MemberService.ALL_MEMBERS_CACHE;
import static org.jetbrains.conf.bookify.members.MemberService.MEMBERS_CACHE;

@SpringBootTest
@Import(DbConfiguration.class)
@ActiveProfiles("test")
class MemberCachingTest {

    private static final UUID SEEDED_MEMBER_ID = UUID.fromString("b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12");

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CacheManager cacheManager;

    // Also after each test: its cleanup deletes through the repository, which bypasses eviction
    @BeforeEach
    @AfterEach
    void clearCaches() {
        cacheManager.getCacheNames().forEach(name -> cache(name).clear());
    }

    @Test
    void findByIdIsServedFromCache() {
        Member first = memberService.findById(SEEDED_MEMBER_ID).orElseThrow();
        Member second = memberService.findById(SEEDED_MEMBER_ID).orElseThrow();

        assertThat(second).isSameAs(first);
        assertThat(cache(MEMBERS_CACHE).get(SEEDED_MEMBER_ID)).isNotNull();
    }

    @Test
    void addMemberEvictsMemberLists() {
        memberService.findAll();
        memberService.findAllActive();

        Member added = memberService.addMember(testMember());

        try {
            assertThat(cache(ALL_MEMBERS_CACHE).get(SimpleKey.EMPTY)).isNull();
            assertThat(cache(ACTIVE_MEMBERS_CACHE).get(SimpleKey.EMPTY)).isNull();
            assertThat(memberService.findAllActive()).extracting(Member::getId).contains(added.getId());
        } finally {
            memberRepository.deleteById(added.getId());
        }
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void disableMemberEvictsMemberAndLists() {
        UUID memberId = memberRepository.save(testMember()).getId();

        try {
            memberService.findById(memberId);
            memberService.findAll();
            memberService.findAllActive();

            memberService.disableMember(memberId);

            assertThat(cache(MEMBERS_CACHE).get(memberId)).isNull();
            assertThat(cache(ALL_MEMBERS_CACHE).get(SimpleKey.EMPTY)).isNull();
            assertThat(cache(ACTIVE_MEMBERS_CACHE).get(SimpleKey.EMPTY)).isNull();
            assertThat(memberService.findById(memberId).orElseThrow().isEnabled()).isFalse();
        } finally {
            memberRepository.deleteById(memberId);
        }
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void deniedDisableMemberKeepsCachedMember() {
        UUID memberId = memberRepository.save(testMember()).getId();

        try {
            memberService.findById(memberId);

            assertThatThrownBy(() -> memberService.disableMember(memberId))
                    .isInstanceOf(AccessDeniedException.class);

            assertThat(cache(MEMBERS_CACHE).get(memberId)).isNotNull();
        } finally {
            memberRepository.deleteById(memberId);
        }
    }

    private static Member testMember() {
        Member member = new Member();
        member.setName("Cache Test Member");
        member.setEmail("cache-test-" + UUID.randomUUID() + "@example.com");
        member.setEnabled(true);
        return member;
    }

    private Cache cache(String name) {
        return Objects.requireNonNull(cacheManager.getCache(name));
    }
}
