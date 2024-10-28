package hexlet.code.controller;

import hexlet.code.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class WelcomeControllerTest {
    private final UserRepository userRepository;

    @NonNull
    private MockMvc mockMvc;

    @AfterEach
    public void clearTables() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Test GET request to /welcome")
    public void testGetToWelcome() throws Exception {
        var result = mockMvc.perform(get("/welcome"))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).isEqualTo("Welcome to Spring");
    }
}
