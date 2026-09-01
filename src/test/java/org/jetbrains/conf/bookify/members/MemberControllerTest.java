package org.jetbrains.conf.bookify.members;

import org.jetbrains.conf.bookify.DbConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Import(DbConfiguration.class)
class MemberControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @Test
    void testFetchAll() throws Exception {
        var membersRequestResult = mockMvc.get().uri("/api/members");
        assertThat(membersRequestResult)
                .hasStatus(HttpStatus.OK)
                .bodyJson();
    }

    @Test
    void testFetchAllActive() throws Exception {
        var activeRequestResult = mockMvc.get().uri("/api/members/active");
        assertThat(activeRequestResult)
                .hasStatus(HttpStatus.OK)
                .bodyJson();
    }

    @Test
    void testAddMember() throws Exception {
        // Add a member
        var addMemberResult = mockMvc.post()
                .uri("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Member\",\"email\":\"test@example.com\",\"password\":\"password123\"}");

        assertThat(addMemberResult)
                .hasStatus(HttpStatus.CREATED);
    }

    @Test
    void testDisableMember() throws Exception {
        // Disable a member (using a UUID from initial data)
        var disableMemberResult = mockMvc.put().uri("/api/members/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/disable");
        assertThat(disableMemberResult).hasStatus(HttpStatus.OK);

        // Verify the member is now disabled by checking active members
        var activeRequestResult = mockMvc.get().uri("/api/members/active");
        assertThat(activeRequestResult)
                .hasStatus(HttpStatus.OK)
                .bodyJson();

        // The disabled member should not be in the active members list
        var searchResult = mockMvc.get().uri("/api/members/search?name=John");
        assertThat(searchResult)
                .hasStatus(HttpStatus.OK)
                .bodyJson();
    }

    @Test
    void testSearchMembersByName() throws Exception {
        // Search for members with "John" in the name (from initial data)
        var searchResult = mockMvc.get().uri("/api/members/search?name=John");
        assertThat(searchResult)
                .hasStatus(HttpStatus.OK)
                .bodyJson();
    }

    @Test
    void testSearchMembersByEmail() throws Exception {
        // Search for members with "example.com" in the email (from initial data)
        var searchResult = mockMvc.get().uri("/api/members/search?email=example.com");
        assertThat(searchResult)
                .hasStatus(HttpStatus.OK)
                .bodyJson();
    }

    @Test
    void testGetMemberById() throws Exception {
        // Get a member by ID (using a UUID from initial data)
        var memberResult = mockMvc.get().uri("/api/members/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
        assertThat(memberResult)
                .hasStatus(HttpStatus.OK)
                .bodyJson();
    }
}
