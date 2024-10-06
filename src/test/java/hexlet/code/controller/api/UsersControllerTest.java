package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UsersControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Faker faker;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getPassword), () -> faker.internet().password(3, 10))
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .create();
    }

    @AfterEach
    public void clearTables() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Test GET request to /api/users")
    public void testGetToApiUsers() throws Exception {
        userRepository.save(testUser);

        var result = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
        assertThat(body).contains(testUser.getEmail());
    }

    @Test
    @DisplayName("Test GET request to /api/users/{id}")
    public void testGetToApiUsersId() throws Exception {
        userRepository.save(testUser);

        var result = mockMvc.perform(get("/api/users/" + testUser.getId()))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(testUser.getId()),
                v -> v.node("email").isEqualTo(testUser.getEmail()),
                v -> v.node("firstName").isEqualTo(testUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUser.getLastName()),
                v -> v.node("createdAt").isEqualTo(testUser.getCreatedAt())
        );
    }

    @Test
    @DisplayName("Test POST request to /api/users")
    public void testPostToApiUsers() throws Exception {
        var dto = new UserCreateDTO();
        dto.setEmail(testUser.getEmail());
        dto.setPassword(testUser.getPassword());
        dto.setFirstName(JsonNullable.of(testUser.getFirstName()));
        dto.setLastName(JsonNullable.of(testUser.getLastName()));

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("email").isEqualTo(testUser.getEmail()),
                v -> v.node("firstName").isEqualTo(testUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUser.getLastName())
        );

        var user = userRepository.findByEmail(dto.getEmail()).get();
        assertNotNull(user);
        assertThat(user.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(user.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(user.getLastName()).isEqualTo(testUser.getLastName());
    }

    @Test
    @DisplayName("Test POST request to /api/users (with incorrect email)")
    public void testPostToApiUsersWithIncorrectEmail() throws Exception {
        var dto = new UserCreateDTO();
        dto.setEmail("zzz");
        dto.setPassword(testUser.getPassword());
        dto.setFirstName(JsonNullable.of(testUser.getFirstName()));
        dto.setLastName(JsonNullable.of(testUser.getLastName()));

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test POST request to /api/users (with incorrect password)")
    public void testPostToApiUsersWithIncorrectPassword() throws Exception {
        var dto = new UserCreateDTO();
        dto.setEmail(testUser.getEmail());
        dto.setPassword("11");
        dto.setFirstName(JsonNullable.of(testUser.getFirstName()));
        dto.setLastName(JsonNullable.of(testUser.getLastName()));

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test PUT request to /api/users/{id} (updating all fields)")
    public void testPutToApiUsersIdUpdatingAllFields() throws Exception {
        userRepository.save(testUser);

        var dto = new UserUpdateDTO();
        dto.setEmail(JsonNullable.of(faker.internet().emailAddress()));
        dto.setPassword(JsonNullable.of(faker.internet().password(3, 10)));
        dto.setFirstName(JsonNullable.of(faker.name().firstName()));
        dto.setLastName(JsonNullable.of(faker.name().lastName()));

        var request = put("/api/users/" + testUser.getId())
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
    @DisplayName("Test PUT request to /api/users/{id} (updating some fields)")
    public void testPutToApiUsersIdUpdatingSomeFields() throws Exception {
        userRepository.save(testUser);

        var dto = new UserUpdateDTO();
        dto.setEmail(JsonNullable.of(faker.internet().emailAddress()));
        dto.setPassword(JsonNullable.of(faker.internet().password(3, 10)));

        var request = put("/api/users/" + testUser.getId())
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
        userRepository.save(testUser);

        long testUserId = testUser.getId();

        mockMvc.perform(delete("/api/users/" + testUser.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(testUserId)).isEqualTo(false);
    }
}