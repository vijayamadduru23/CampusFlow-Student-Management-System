package com.studentmanagement.config;

import com.studentmanagement.controller.CsrfController;
import com.studentmanagement.controller.StudentController;
import com.studentmanagement.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(StudentController.class)
@Import({SecurityConfig.class, CsrfController.class})
class SecurityConfigTest {

    private static final String TEST_USERNAME = UUID.randomUUID().toString();
    private static final String TEST_PASSWORD = UUID.randomUUID() + "A1!";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService studentService;

    @DynamicPropertySource
    static void adminCredentials(DynamicPropertyRegistry registry) {
        registry.add("CAMPUSFLOW_ADMIN_USERNAME", () -> TEST_USERNAME);
        registry.add("CAMPUSFLOW_ADMIN_PASSWORD", () -> TEST_PASSWORD);
    }

    @Test
    void studentApiRejectsUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void configuredAdminCanSignIn() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", TEST_USERNAME)
                        .param("password", TEST_PASSWORD))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void csrfEndpointProvidesTokenForLoginForm() throws Exception {
        mockMvc.perform(get("/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.headerName").isNotEmpty());
    }

    @Test
    void adminCanReadStudentApi() throws Exception {
        mockMvc.perform(get("/api/students").with(user("test-admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedNonAdminCannotReadStudentApi() throws Exception {
        mockMvc.perform(get("/api/students").with(user("test-user").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentWritesRequireCsrfToken() throws Exception {
        when(studentService.addStudent(org.mockito.ArgumentMatchers.any())).thenReturn(1);

        mockMvc.perform(post("/api/students")
                        .with(user("test-admin").roles("ADMIN"))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/students")
                        .with(user("test-admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    void logoutRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/logout").with(user("test-admin").roles("ADMIN")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/logout")
                        .with(user("test-admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isFound());
    }
}
