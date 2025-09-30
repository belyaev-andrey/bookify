package org.jetbrains.conf.bookify.employee;

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
public class EmployeeControllerTest {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    public void testFindAllSortedByName() {
        var mockMvc = mockMvcTester.get()
                .accept(MediaType.APPLICATION_JSON)
                .uri("/api/employee?sortBy=name");
        assertThat(mockMvc).hasStatus(HttpStatus.OK).bodyJson().isNotNull();
        mockMvc.assertThat().bodyJson()
                .extractingPath("$[0].name")
                .isEqualTo("Caleb Hayes");

        mockMvc.assertThat().bodyJson()
                .extractingPath("$[0].organization")
                .isEqualTo("SCIENCE");
    }


    @Test
    public void testWrongSorting() {
        var mockMvc = mockMvcTester.get()
                .accept(MediaType.APPLICATION_JSON)
                .uri("/api/employee?sortBy=non-existing-field");
        assertThat(mockMvc).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR).bodyJson().isNotNull();
    }


}
