package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.dto.taskStatus.TaskStatusUpdateDTO;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskStatusesControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Faker faker;

    private TaskStatus testTaskStatus;
    private JwtRequestPostProcessor token;

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));

        testTaskStatus = Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .ignore(Select.field(TaskStatus::getCreatedAt))
                .supply(Select.field(TaskStatus::getSlug), () -> faker.internet().slug())
                .supply(Select.field(TaskStatus::getName), () -> faker.gameOfThrones().house())
                .create();
    }

    @AfterEach
    public void clearTables() {
        taskStatusRepository.deleteAll();
    }

    @Test
    @DisplayName("Test GET request to /api/task_statuses")
    public void testGetToApiTaskStatuses() throws Exception {
        taskStatusRepository.save(testTaskStatus);

        var result = mockMvc.perform(get("/api/task_statuses")
                        .with(token))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
        assertThat(body).contains(testTaskStatus.getSlug());
        assertThat(body).contains(testTaskStatus.getName());
    }

    @Test
    @DisplayName("Test GET request to /api/task_statuses/{id}")
    public void testGetToApiTaskStatusesId() throws Exception {
        taskStatusRepository.save(testTaskStatus);

        var result = mockMvc.perform(get("/api/task_statuses/" + testTaskStatus.getId())
                        .with(token))
                .andExpect(status().isOk())
                .andReturn();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String createdAt = testTaskStatus.getCreatedAt().format(formatter);

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(testTaskStatus.getId()),
                v -> v.node("slug").isEqualTo(testTaskStatus.getSlug()),
                v -> v.node("name").isEqualTo(testTaskStatus.getName()),
                v -> v.node("createdAt").isEqualTo(createdAt)
        );
    }

    @Test
    @DisplayName("Test POST request to /api/task_statuses")
    public void testPostToApiTaskStatuses() throws Exception {
        var dto = new TaskStatusCreateDTO();
        dto.setSlug(testTaskStatus.getSlug());
        dto.setName(testTaskStatus.getName());

        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("slug").isEqualTo(testTaskStatus.getSlug()),
                v -> v.node("name").isEqualTo(testTaskStatus.getName())
        );

        TaskStatus taskStatus = taskStatusRepository.findBySlug(testTaskStatus.getSlug()).get();
        assertNotNull(taskStatus);
        assertThat(taskStatus.getSlug()).isEqualTo(testTaskStatus.getSlug());
        assertThat(taskStatus.getName()).isEqualTo(testTaskStatus.getName());
    }

    @Test
    @DisplayName("Test POST request to /api/task_statuses (with not valid slug)")
    public void testPostToApiTaskStatusesWithNotValidSlug() throws Exception {
        var dto = new TaskStatusCreateDTO();
        dto.setSlug("");
        dto.setName(testTaskStatus.getName());

        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test POST request to /api/task_statuses (with not valid name)")
    public void testPostToApiTaskStatusesWithNotValidName() throws Exception {
        var dto = new TaskStatusCreateDTO();
        dto.setSlug(testTaskStatus.getSlug());
        dto.setName("");

        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test PUT request to /api/task_statuses/{id} (updating all fields)")
    public void testPutToApiTaskStatusesIdUpdatingAllFields() throws Exception {
        taskStatusRepository.save(testTaskStatus);

        var dto = new TaskStatusUpdateDTO();
        dto.setSlug(JsonNullable.of(faker.internet().slug()));
        dto.setName(JsonNullable.of(faker.gameOfThrones().house()));

        var request = put("/api/task_statuses/" + testTaskStatus.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("slug").isEqualTo(dto.getSlug().get()),
                v -> v.node("name").isEqualTo(dto.getName().get())
        );

        TaskStatus taskStatus = taskStatusRepository.findBySlug(dto.getSlug().get()).get();
        assertThat(taskStatus.getSlug()).isEqualTo(dto.getSlug().get());
        assertThat(taskStatus.getName()).isEqualTo(dto.getName().get());
    }

    @Test
    @DisplayName("Test PUT request to /api/task_statuses/{id} (updating some fields)")
    public void testPutToApiTaskStatusesIdUpdatingSomeFields() throws Exception {
        taskStatusRepository.save(testTaskStatus);

        var dto = new TaskStatusUpdateDTO();
        dto.setName(JsonNullable.of(faker.gameOfThrones().house()));

        var request = put("/api/task_statuses/" + testTaskStatus.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("slug").isEqualTo(testTaskStatus.getSlug()),
                v -> v.node("name").isEqualTo(dto.getName().get())
        );

        TaskStatus taskStatus = taskStatusRepository.findById(testTaskStatus.getId()).get();
        assertThat(taskStatus.getSlug()).isEqualTo(testTaskStatus.getSlug());
        assertThat(taskStatus.getName()).isEqualTo(dto.getName().get());
    }

    @Test
    @DisplayName("Test DELETE request to /api/task_statuses/{id}")
    public void testDeleteToApiTaskStatusesId() throws Exception {
        taskStatusRepository.save(testTaskStatus);

        long testTaskStatusId = testTaskStatus.getId();

        mockMvc.perform(delete("/api/task_statuses/" + testTaskStatus.getId())
                        .with(token))
                .andExpect(status().isNoContent());

        assertThat(taskStatusRepository.existsById(testTaskStatusId)).isEqualTo(false);
    }
}
