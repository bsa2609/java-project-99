package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.service.TaskStatusService;
import hexlet.code.service.UserService;
import hexlet.code.util.ModelGenerator;
import hexlet.code.util.TaskStatusUtil;
import hexlet.code.util.UserUtils;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
@AutoConfigureMockMvc
public class TasksControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusService taskStatusService;

    @Autowired
    private TaskStatusUtil taskStatusUtil;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    private Task testTask;
    private JwtRequestPostProcessor token;

    @BeforeEach
    public void setUp() {
        userUtils.createAdminUser();
        taskStatusUtil.createDefaultTaskStatuses();

        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        token = jwt().jwt(builder -> builder.subject(userUtils.getAdminsEmail()));

        testTask = Instancio.of(modelGenerator.getTaskModel())
                .create();
    }

    @AfterEach
    public void clearTables() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
    }

    @Test
    @DisplayName("Test GET request to /api/tasks")
    public void testGetToApiTasks() throws Exception {
        taskRepository.save(testTask);

        var result = mockMvc.perform(get("/api/tasks")
                        .with(token))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
        assertThat(body).contains(testTask.getName());
        assertThat(body).contains(testTask.getDescription());
        assertThat(body).contains(testTask.getTaskStatus().getSlug());
        assertThat(body).contains(String.valueOf(testTask.getAssignee().getId()));
    }

    @Test
    @DisplayName("Test GET request to /api/tasks/{id}")
    public void testGetToApiTasksId() throws Exception {
        taskRepository.save(testTask);

        var result = mockMvc.perform(get("/api/tasks/" + testTask.getId())
                        .with(token))
                .andExpect(status().isOk())
                .andReturn();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String createdAt = testTask.getCreatedAt().format(formatter);

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(testTask.getId()),
                v -> v.node("title").isEqualTo(testTask.getName()),
                v -> v.node("content").isEqualTo(testTask.getDescription()),
                v -> v.node("index").isEqualTo(testTask.getIndex()),
                v -> v.node("status").isEqualTo(testTask.getTaskStatus().getSlug()),
                v -> v.node("assignee_id").isEqualTo(testTask.getAssignee().getId()),
                v -> v.node("createdAt").isEqualTo(createdAt)
        );
    }

    @Test
    @DisplayName("Test POST request to /api/tasks")
    public void testPostToApiTasks() throws Exception {
        var dto = new TaskCreateDTO();
        dto.setTitle(testTask.getName());
        dto.setStatus(testTask.getTaskStatus().getSlug());
        dto.setContent(JsonNullable.of(testTask.getDescription()));
        dto.setIndex(JsonNullable.of(testTask.getIndex()));
        dto.setAssigneeId(JsonNullable.of(testTask.getAssignee().getId()));

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("title").isEqualTo(testTask.getName()),
                v -> v.node("content").isEqualTo(testTask.getDescription()),
                v -> v.node("index").isEqualTo(testTask.getIndex()),
                v -> v.node("status").isEqualTo(testTask.getTaskStatus().getSlug()),
                v -> v.node("assignee_id").isEqualTo(testTask.getAssignee().getId())
        );

        TaskDTO taskDTO = objectMapper.readValue(body, TaskDTO.class);

        Task task = taskRepository.findById(taskDTO.getId()).get();
        assertNotNull(task);
        assertThat(task.getName()).isEqualTo(testTask.getName());
        assertThat(task.getDescription()).isEqualTo(testTask.getDescription());
        assertThat(task.getIndex()).isEqualTo(testTask.getIndex());
        assertThat(task.getTaskStatus()).isEqualTo(testTask.getTaskStatus());
        assertThat(task.getAssignee()).isEqualTo(testTask.getAssignee());
    }

    @Test
    @DisplayName("Test POST request to /api/tasks (with not valid title)")
    public void testPostToApiTasksWithNotValidTitle() throws Exception {
        var dto = new TaskCreateDTO();
        dto.setTitle("");
        dto.setStatus(testTask.getTaskStatus().getSlug());
        dto.setContent(JsonNullable.of(testTask.getDescription()));
        dto.setIndex(JsonNullable.of(testTask.getIndex()));
        dto.setAssigneeId(JsonNullable.of(testTask.getAssignee().getId()));

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test POST request to /api/tasks (with not valid status)")
    public void testPostToApiTasksWithNotValidStatus() throws Exception {
        var dto = new TaskCreateDTO();
        dto.setTitle(testTask.getName());
        dto.setStatus("unknown_status");
        dto.setContent(JsonNullable.of(testTask.getDescription()));
        dto.setIndex(JsonNullable.of(testTask.getIndex()));
        dto.setAssigneeId(JsonNullable.of(testTask.getAssignee().getId()));

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test POST request to /api/tasks (with not valid assignee_id)")
    public void testPostToApiTasksWithNotValidAssigneeId() throws Exception {
        var dto = new TaskCreateDTO();
        dto.setTitle(testTask.getName());
        dto.setStatus(testTask.getTaskStatus().getSlug());
        dto.setContent(JsonNullable.of(testTask.getDescription()));
        dto.setIndex(JsonNullable.of(testTask.getIndex()));
        dto.setAssigneeId(JsonNullable.of(999L));

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test POST request to /api/tasks (from unauthorized user)")
    public void testPostToApiTasksFromUnauthorizedUser() throws Exception {
        var dto = new TaskCreateDTO();
        dto.setTitle(testTask.getName());
        dto.setStatus(testTask.getTaskStatus().getSlug());
        dto.setContent(JsonNullable.of(testTask.getDescription()));
        dto.setIndex(JsonNullable.of(testTask.getIndex()));
        dto.setAssigneeId(JsonNullable.of(testTask.getAssignee().getId()));

        var request = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        assertThat(taskRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Test PUT request to /api/tasks/{id} (updating all fields)")
    public void testPutToApiTasksIdUpdatingAllFields() throws Exception {
        taskRepository.save(testTask);

        Task testTask2 = Instancio.of(modelGenerator.getTaskModel())
                .create();

        var dto = new TaskUpdateDTO();
        dto.setTitle(JsonNullable.of(testTask2.getName()));
        dto.setContent(JsonNullable.of(testTask2.getDescription()));
        dto.setIndex(JsonNullable.of(testTask2.getIndex()));
        dto.setStatus(JsonNullable.of(testTask2.getTaskStatus().getSlug()));
        dto.setAssigneeId(JsonNullable.of(testTask2.getAssignee().getId()));

        var request = put("/api/tasks/" + testTask.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("title").isEqualTo(testTask2.getName()),
                v -> v.node("content").isEqualTo(testTask2.getDescription()),
                v -> v.node("index").isEqualTo(testTask2.getIndex()),
                v -> v.node("status").isEqualTo(testTask2.getTaskStatus().getSlug()),
                v -> v.node("assignee_id").isEqualTo(testTask2.getAssignee().getId())
        );

        Task task = taskRepository.findById(testTask.getId()).get();
        assertNotNull(task);
        assertThat(task.getName()).isEqualTo(testTask2.getName());
        assertThat(task.getDescription()).isEqualTo(testTask2.getDescription());
        assertThat(task.getIndex()).isEqualTo(testTask2.getIndex());
        assertThat(task.getTaskStatus()).isEqualTo(testTask2.getTaskStatus());
        assertThat(task.getAssignee()).isEqualTo(testTask2.getAssignee());
    }

    @Test
    @DisplayName("Test PUT request to /api/tasks/{id} (updating all fields from unauthorized user)")
    public void testPutToApiTasksIdUpdatingAllFieldsFromUnauthorizedUser() throws Exception {
        taskRepository.save(testTask);

        Task testTask2 = Instancio.of(modelGenerator.getTaskModel())
                .create();

        var dto = new TaskUpdateDTO();
        dto.setTitle(JsonNullable.of(testTask2.getName()));
        dto.setContent(JsonNullable.of(testTask2.getDescription()));
        dto.setIndex(JsonNullable.of(testTask2.getIndex()));
        dto.setStatus(JsonNullable.of(testTask2.getTaskStatus().getSlug()));
        dto.setAssigneeId(JsonNullable.of(testTask2.getAssignee().getId()));

        var request = put("/api/tasks/" + testTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        Task task = taskRepository.findById(testTask.getId()).orElse(null);
        assertNotNull(task);
        assertThat(task.getName()).isEqualTo(testTask.getName());
        assertThat(task.getDescription()).isEqualTo(testTask.getDescription());
        assertThat(task.getIndex()).isEqualTo(testTask.getIndex());
        assertThat(task.getTaskStatus()).isEqualTo(testTask.getTaskStatus());
        assertThat(task.getAssignee()).isEqualTo(testTask.getAssignee());
    }

    @Test
    @DisplayName("Test PUT request to /api/tasks/{id} (updating some fields)")
    public void testPutToApiTasksIdUpdatingSomeFields() throws Exception {
        taskRepository.save(testTask);

        Task testTask2 = Instancio.of(modelGenerator.getTaskModel())
                .create();

        var dto = new TaskUpdateDTO();
        dto.setTitle(JsonNullable.of(testTask2.getName()));
        dto.setContent(JsonNullable.of(testTask2.getDescription()));

        var request = put("/api/tasks/" + testTask.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("title").isEqualTo(testTask2.getName()),
                v -> v.node("content").isEqualTo(testTask2.getDescription()),
                v -> v.node("index").isEqualTo(testTask.getIndex()),
                v -> v.node("status").isEqualTo(testTask.getTaskStatus().getSlug()),
                v -> v.node("assignee_id").isEqualTo(testTask.getAssignee().getId())
        );

        Task task = taskRepository.findById(testTask.getId()).orElse(null);
        assertNotNull(task);
        assertThat(task.getName()).isEqualTo(testTask2.getName());
        assertThat(task.getDescription()).isEqualTo(testTask2.getDescription());
        assertThat(task.getIndex()).isEqualTo(testTask.getIndex());
        assertThat(task.getTaskStatus()).isEqualTo(testTask.getTaskStatus());
        assertThat(task.getAssignee()).isEqualTo(testTask.getAssignee());
    }

    @Test
    @DisplayName("Test DELETE request to /api/tasks/{id}")
    public void testDeleteToApiTasksId() throws Exception {
        taskRepository.save(testTask);

        long testTaskId = testTask.getId();

        mockMvc.perform(delete("/api/tasks/" + testTask.getId())
                        .with(token))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.existsById(testTaskId)).isEqualTo(false);
    }

    @Test
    @DisplayName("Test DELETE request to /api/tasks/{id} (from unauthorized user)")
    public void testDeleteToApiTasksIdFromUnauthorizedUser() throws Exception {
        taskRepository.save(testTask);

        long testTaskId = testTask.getId();

        mockMvc.perform(delete("/api/tasks/" + testTask.getId()))
                .andExpect(status().isUnauthorized());

        assertThat(taskRepository.existsById(testTaskId)).isEqualTo(true);
    }
}
