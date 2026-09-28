package org.jetbrains.conf.bookify.members;

import org.jetbrains.conf.bookify.DbConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(DbConfiguration.class)
@ActiveProfiles("test")
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void memberWithoutPasswordIsRejectedByTheDatabase() {
        // The API rejects a missing password before it gets this far; the NOT NULL constraint catches
        // writes that bypass it
        Member member = new Member();
        member.setName("Member Without Password");
        member.setEmail("no-password@example.com");
        member.setEnabled(true);

        assertThatThrownBy(() -> memberRepository.save(member))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
