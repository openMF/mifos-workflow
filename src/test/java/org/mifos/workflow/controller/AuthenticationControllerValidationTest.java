package org.mifos.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.dto.fineract.auth.AuthenticationRequest;
import org.mifos.workflow.service.fineract.auth.FineractAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FineractAuthService fineractAuthService; // mock the dependency

    @Test
    void shouldReturnBadRequestWhenUsernameIsBlank() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("")
                .password("validPass")
                .build();

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.username").value("Username must not be blank"))
                .andExpect(jsonPath("$.fieldErrors.password").doesNotExist());
    }

    @Test
    void shouldReturnBadRequestWhenPasswordIsBlank() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("user")
                .password("")
                .build();

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").value("Password must not be blank"))
                .andExpect(jsonPath("$.fieldErrors.username").doesNotExist());
    }

    @Test
    void shouldReturnBadRequestWhenBothAreBlank() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("")
                .password("")
                .build();

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.username").value("Username must not be blank"))
                .andExpect(jsonPath("$.fieldErrors.password").value("Password must not be blank"));
    }
}