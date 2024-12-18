package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import jakarta.servlet.ServletException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UsersControllerTest {
    private final WebApplicationContext wac;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final Faker faker;
    private final ModelGenerator modelGenerator;

    @NonNull
    private MockMvc mockMvc;

    private User testUser;
    private JwtRequestPostProcessor token;

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        testUser = Instancio.of(modelGenerator.getUserModel())
                .create();

        userRepository.save(testUser);

        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
    }

    @AfterEach
    public void clearTables() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Test GET request to /api/users")
    public void testGetToApiUsers() throws Exception {
        var result = mockMvc.perform(get("/api/users")
                        .with(token))
                .andExpect(status().isOk())
                .andReturn();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String createdAt = testUser.getCreatedAt().format(formatter);

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
        assertThat(body).contains(testUser.getEmail());
        assertThat(body).contains(String.valueOf(testUser.getId()));
        assertThat(body).contains(testUser.getFirstName());
        assertThat(body).contains(testUser.getLastName());
        assertThat(body).contains(createdAt);
    }

    @Test
    @DisplayName("Test GET request to /api/users/{id}")
    public void testGetToApiUsersId() throws Exception {
        var result = mockMvc.perform(get("/api/users/" + testUser.getId())
                        .with(token))
                .andExpect(status().isOk())
                .andReturn();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String createdAt = testUser.getCreatedAt().format(formatter);

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(testUser.getId()),
                v -> v.node("email").isEqualTo(testUser.getEmail()),
                v -> v.node("firstName").isEqualTo(testUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUser.getLastName()),
                v -> v.node("createdAt").isEqualTo(createdAt)
        );
    }

    @Test
    @DisplayName("Test POST request to /api/users")
    public void testPostToApiUsers() throws Exception {
        String password = modelGenerator.generatePassword();

        var dto = new UserCreateDTO();
        dto.setEmail(faker.internet().emailAddress());
        dto.setPassword(password);
        dto.setFirstName(JsonNullable.of(faker.name().firstName()));
        dto.setLastName(JsonNullable.of(faker.name().lastName()));

        var request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("email").isEqualTo(dto.getEmail()),
                v -> v.node("firstName").isEqualTo(dto.getFirstName().get()),
                v -> v.node("lastName").isEqualTo(dto.getLastName().get())
        );

        var user = userRepository.findByEmail(dto.getEmail()).get();
        assertNotNull(user);
        assertThat(user.getEmail()).isEqualTo(dto.getEmail());
        assertThat(user.getFirstName()).isEqualTo(dto.getFirstName().get());
        assertThat(user.getLastName()).isEqualTo(dto.getLastName().get());
    }

    @Test
    @DisplayName("Test POST request to /api/users (with not valid email)")
    public void testPostToApiUsersWithNotValidEmail() throws Exception {
        String password = modelGenerator.generatePassword();

        var dto = new UserCreateDTO();
        dto.setEmail("zzz");
        dto.setPassword(password);
        dto.setFirstName(JsonNullable.of(faker.name().firstName()));
        dto.setLastName(JsonNullable.of(faker.name().lastName()));

        var request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test POST request to /api/users (with not valid password)")
    public void testPostToApiUsersWithNotValidPassword() throws Exception {
        var dto = new UserCreateDTO();
        dto.setEmail(faker.internet().emailAddress());
        dto.setPassword("11");
        dto.setFirstName(JsonNullable.of(faker.name().firstName()));
        dto.setLastName(JsonNullable.of(faker.name().lastName()));

        var request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test POST request to /api/users (with duplicate email)")
    public void testPostToApiUsersWithDuplicateEmail() throws Exception {
        String password = modelGenerator.generatePassword();

        var dto = new UserCreateDTO();
        dto.setEmail(testUser.getEmail());
        dto.setPassword(password);
        dto.setFirstName(JsonNullable.of(faker.name().firstName()));
        dto.setLastName(JsonNullable.of(faker.name().lastName()));

        var request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        assertThrows(ServletException.class,
                () -> mockMvc.perform(request).andReturn()
        );
    }

    @Test
    @DisplayName("Test PUT request to /api/users/{id} (updating all fields)")
    public void testPutToApiUsersIdUpdatingAllFields() throws Exception {
        String password = modelGenerator.generatePassword();

        var dto = new UserUpdateDTO();
        dto.setEmail(JsonNullable.of(faker.internet().emailAddress()));
        dto.setPassword(JsonNullable.of(password));
        dto.setFirstName(JsonNullable.of(faker.name().firstName()));
        dto.setLastName(JsonNullable.of(faker.name().lastName()));

        var request = put("/api/users/" + testUser.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("email").isEqualTo(dto.getEmail().get()),
                v -> v.node("firstName").isEqualTo(dto.getFirstName().get()),
                v -> v.node("lastName").isEqualTo(dto.getLastName().get())
        );

        var user = userRepository.findByEmail(dto.getEmail().get()).get();
        assertThat(user.getEmail()).isEqualTo(dto.getEmail().get());
        assertThat(user.getFirstName()).isEqualTo(dto.getFirstName().get());
        assertThat(user.getLastName()).isEqualTo(dto.getLastName().get());
    }

    @Test
    @DisplayName("Test PUT request to /api/users/{id} (updating all fields from other user)")
    public void testPutToApiUsersIdUpdatingAllFieldsFromOtherUser() throws Exception {
        User testUser2 = Instancio.of(modelGenerator.getUserModel())
                .create();

        userRepository.save(testUser2);

        JwtRequestPostProcessor token2 = jwt().jwt(builder -> builder.subject(testUser2.getEmail()));

        String password = modelGenerator.generatePassword();

        var dto = new UserUpdateDTO();
        dto.setEmail(JsonNullable.of(faker.internet().emailAddress()));
        dto.setPassword(JsonNullable.of(password));
        dto.setFirstName(JsonNullable.of(faker.name().firstName()));
        dto.setLastName(JsonNullable.of(faker.name().lastName()));

        var request = put("/api/users/" + testUser.getId())
                .with(token2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var result = mockMvc.perform(request)
                .andExpect(status().isForbidden());

        var user = userRepository.findByEmail(testUser.getEmail()).get();
        assertThat(user.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(user.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(user.getLastName()).isEqualTo(testUser.getLastName());
    }

    @Test
    @DisplayName("Test PUT request to /api/users/{id} (updating some fields)")
    public void testPutToApiUsersIdUpdatingSomeFields() throws Exception {
        String password = modelGenerator.generatePassword();

        var dto = new UserUpdateDTO();
        dto.setEmail(JsonNullable.of(faker.internet().emailAddress()));
        dto.setPassword(JsonNullable.of(password));

        var request = put("/api/users/" + testUser.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("email").isEqualTo(dto.getEmail().get()),
                v -> v.node("firstName").isEqualTo(testUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUser.getLastName())
        );

        var user = userRepository.findByEmail(dto.getEmail().get()).get();
        assertThat(user.getEmail()).isEqualTo(dto.getEmail().get());
        assertThat(user.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(user.getLastName()).isEqualTo(testUser.getLastName());
    }

    @Test
    @DisplayName("Test DELETE request to /api/users/{id}")
    public void testDeleteToApiUsersId() throws Exception {
        long testUserId = testUser.getId();

        mockMvc.perform(delete("/api/users/" + testUser.getId())
                        .with(token))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(testUserId)).isEqualTo(false);
    }

    @Test
    @DisplayName("Test DELETE request to /api/users/{id} (from other user)")
    public void testDeleteToApiUsersIdFromOtherUser() throws Exception {
        User testUser2 = Instancio.of(modelGenerator.getUserModel())
                .create();

        userRepository.save(testUser2);

        JwtRequestPostProcessor token2 = jwt().jwt(builder -> builder.subject(testUser2.getEmail()));

        long testUserId = testUser.getId();

        mockMvc.perform(delete("/api/users/" + testUser.getId())
                        .with(token2))
                .andExpect(status().isForbidden());

        assertThat(userRepository.existsById(testUserId)).isEqualTo(true);
    }
}
