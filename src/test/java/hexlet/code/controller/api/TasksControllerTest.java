package hexlet.code.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.service.LabelService;
import hexlet.code.service.TaskStatusService;
import hexlet.code.util.LabelUtils;
import hexlet.code.util.ModelGenerator;
import hexlet.code.util.TaskStatusUtils;
import hexlet.code.util.UserUtils;
import jakarta.servlet.ServletException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TasksControllerTest {
    private final WebApplicationContext wac;
    private final TaskRepository taskRepository;
    private final TaskStatusService taskStatusService;
    private final TaskStatusUtils taskStatusUtils;
    private final TaskStatusRepository taskStatusRepository;
    private final UserUtils userUtils;
    private final ObjectMapper objectMapper;
    private final ModelGenerator modelGenerator;
    private final LabelUtils labelUtils;
    private final LabelRepository labelRepository;
    private final LabelService labelService;

    @NonNull
    private MockMvc mockMvc;

    private Task testTask;
    private JwtRequestPostProcessor token;

    @BeforeEach
    public void setUp() {
        userUtils.createAdminUser();
        taskStatusUtils.createDefaultTaskStatuses();
        labelUtils.createDefaultLabels();

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
        labelRepository.deleteAll();
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
    @DisplayName("Test GET request to /api/tasks (using filters)")
    public void testGetToApiTasksUsingFilters() throws Exception {
        Task testTask2 = Instancio.of(modelGenerator.getTaskModel())
                .create();

        Task testTask3 = Instancio.of(modelGenerator.getTaskModel())
                .create();

        Task testTask4 = Instancio.of(modelGenerator.getTaskModel())
                .create();

        Task testTask5 = Instancio.of(modelGenerator.getTaskModel())
                .create();

        Label labelBug = labelService.findByName("bug");
        Label labelFeature = labelService.findByName("feature");

        TaskStatus taskStatusDraft = taskStatusService.findBySlug("draft");
        TaskStatus taskStatusPublished = taskStatusService.findBySlug("published");

        testTask.setName("test 1");
        testTask.setTaskStatus(taskStatusDraft);
        testTask.setLabels(List.of(labelBug));

        testTask2.setName("test 2");
        testTask2.setTaskStatus(taskStatusDraft);
        testTask2.setLabels(List.of(labelBug));

        testTask3.setName("test 3");
        testTask3.setTaskStatus(taskStatusDraft);
        testTask3.setLabels(List.of(labelBug, labelFeature));

        testTask4.setName("task 1");
        testTask4.setTaskStatus(taskStatusPublished);
        testTask4.setLabels(List.of(labelBug, labelFeature));

        testTask5.setName("task 2");
        testTask5.setTaskStatus(taskStatusPublished);
        testTask5.setLabels(List.of(labelFeature));

        taskRepository.save(testTask);
        taskRepository.save(testTask2);
        taskRepository.save(testTask3);
        taskRepository.save(testTask4);
        taskRepository.save(testTask5);

        final int tasksCount = 3;
        final int tasks2Count = 2;
        final int tasks3Count = 1;

        var result = mockMvc.perform(get("/api/tasks?status=draft&labelId=" + labelBug.getId())
                        .with(token))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();

        List<Task> tasks = objectMapper.readValue(body, new TypeReference<List<Task>>() { });
        assertThat(tasks.size()).isEqualTo(tasksCount);

        assertThat(body).contains(testTask.getName());
        assertThat(body).contains(testTask2.getName());
        assertThat(body).contains(testTask3.getName());

        var result2 = mockMvc.perform(get("/api/tasks?titleCont=task&status=published&labelId="
                        + labelFeature.getId())
                        .with(token))
                .andExpect(status().isOk())
                .andReturn();

        String body2 = result2.getResponse().getContentAsString();
        assertThatJson(body2).isArray();

        List<Task> tasks2 = objectMapper.readValue(body2, new TypeReference<List<Task>>() { });
        assertThat(tasks2.size()).isEqualTo(tasks2Count);

        assertThat(body2).contains(testTask4.getName());
        assertThat(body2).contains(testTask5.getName());

        var result3 = mockMvc.perform(get("/api/tasks?titleCont=test&assigneeId="
                        + userUtils.getAdminUser().getId() + "&status=draft&labelId="
                        + labelFeature.getId())
                        .with(token))
                .andExpect(status().isOk())
                .andReturn();

        String body3 = result3.getResponse().getContentAsString();
        assertThatJson(body3).isArray();

        List<Task> tasks3 = objectMapper.readValue(body3, new TypeReference<List<Task>>() { });
        assertThat(tasks3.size()).isEqualTo(tasks3Count);

        assertThat(body3).contains(testTask3.getName());
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
                v -> v.node("status").isEqualTo(testTask.getTaskStatus().getSlug()),
                v -> v.node("createdAt").isEqualTo(createdAt),
                v -> v.node("taskLabelIds").isArray(),
                v -> v.node("taskLabelIds").isEqualTo(new long[]{testTask.getLabels().getFirst().getId()})
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
        dto.setTaskLabelIds(JsonNullable.of(List.of(testTask.getLabels().getFirst().getId())));

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
                v -> v.node("assignee_id").isEqualTo(testTask.getAssignee().getId()),
                v -> v.node("taskLabelIds").isArray(),
                v -> v.node("taskLabelIds").isEqualTo(new long[]{testTask.getLabels().getFirst().getId()})
        );

        TaskDTO taskDTO = objectMapper.readValue(body, TaskDTO.class);

        Task task = taskRepository.findByIdFetchLabels(taskDTO.getId()).orElse(null);
        assertNotNull(task);
        assertThat(task.getName()).isEqualTo(testTask.getName());
        assertThat(task.getDescription()).isEqualTo(testTask.getDescription());
        assertThat(task.getIndex()).isEqualTo(testTask.getIndex());
        assertThat(task.getTaskStatus()).isEqualTo(testTask.getTaskStatus());
        assertThat(task.getAssignee()).isEqualTo(testTask.getAssignee());
        assertThat(task.getLabels().getFirst()).isEqualTo(testTask.getLabels().getFirst());
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

        assertThrows(ServletException.class,
                () -> mockMvc.perform(request).andReturn()
        );
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
        dto.setTaskLabelIds(JsonNullable.of(List.of(testTask.getLabels().getFirst().getId())));

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var result = mockMvc.perform(request).andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("title").isEqualTo(testTask.getName()),
                v -> v.node("content").isEqualTo(testTask.getDescription()),
                v -> v.node("index").isEqualTo(testTask.getIndex()),
                v -> v.node("status").isEqualTo(testTask.getTaskStatus().getSlug()),
                v -> v.node("assignee_id").isEqualTo(0),
                v -> v.node("taskLabelIds").isArray(),
                v -> v.node("taskLabelIds").isEqualTo(new long[]{testTask.getLabels().getFirst().getId()})
        );

        TaskDTO taskDTO = objectMapper.readValue(body, TaskDTO.class);

        Task task = taskRepository.findByIdFetchLabels(taskDTO.getId()).orElse(null);
        assertNotNull(task);
        assertThat(task.getName()).isEqualTo(testTask.getName());
        assertThat(task.getDescription()).isEqualTo(testTask.getDescription());
        assertThat(task.getIndex()).isEqualTo(testTask.getIndex());
        assertThat(task.getTaskStatus()).isEqualTo(testTask.getTaskStatus());
        assertNull(task.getAssignee());
        assertThat(task.getLabels().getFirst()).isEqualTo(testTask.getLabels().getFirst());
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
        dto.setTaskLabelIds(JsonNullable.of(List.of(testTask2.getLabels().getFirst().getId())));

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
                v -> v.node("assignee_id").isEqualTo(testTask2.getAssignee().getId()),
                v -> v.node("taskLabelIds").isArray(),
                v -> v.node("taskLabelIds").isEqualTo(new long[]{testTask2.getLabels().getFirst().getId()})
        );

        Task task = taskRepository.findByIdFetchLabels(testTask.getId()).orElse(null);
        assertNotNull(task);
        assertThat(task.getName()).isEqualTo(testTask2.getName());
        assertThat(task.getDescription()).isEqualTo(testTask2.getDescription());
        assertThat(task.getIndex()).isEqualTo(testTask2.getIndex());
        assertThat(task.getTaskStatus()).isEqualTo(testTask2.getTaskStatus());
        assertThat(task.getAssignee()).isEqualTo(testTask2.getAssignee());
        assertThat(task.getLabels().getFirst()).isEqualTo(testTask2.getLabels().getFirst());
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
        dto.setTaskLabelIds(JsonNullable.of(List.of(testTask2.getLabels().getFirst().getId())));

        var request = put("/api/tasks/" + testTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        Task task = taskRepository.findByIdFetchLabels(testTask.getId()).orElse(null);
        assertNotNull(task);
        assertThat(task.getName()).isEqualTo(testTask.getName());
        assertThat(task.getDescription()).isEqualTo(testTask.getDescription());
        assertThat(task.getIndex()).isEqualTo(testTask.getIndex());
        assertThat(task.getTaskStatus()).isEqualTo(testTask.getTaskStatus());
        assertThat(task.getAssignee()).isEqualTo(testTask.getAssignee());
        assertThat(task.getLabels().getFirst()).isEqualTo(testTask.getLabels().getFirst());
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

        Task task = taskRepository.findByIdFetchLabels(testTask.getId()).orElse(null);
        assertNotNull(task);
        assertThat(task.getName()).isEqualTo(testTask2.getName());
        assertThat(task.getDescription()).isEqualTo(testTask2.getDescription());
        assertThat(task.getIndex()).isEqualTo(testTask.getIndex());
        assertThat(task.getTaskStatus()).isEqualTo(testTask.getTaskStatus());
        assertThat(task.getAssignee()).isEqualTo(testTask.getAssignee());
        assertThat(task.getLabels().getFirst()).isEqualTo(testTask.getLabels().getFirst());
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
