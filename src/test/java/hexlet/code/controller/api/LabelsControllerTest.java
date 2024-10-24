package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.util.LabelUtils;
import hexlet.code.util.ModelGenerator;
import hexlet.code.util.UserUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.instancio.Instancio;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LabelsControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private LabelUtils labelUtils;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    private Label testLabel;
    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    @BeforeEach
    public void setUp() {
        userUtils.createAdminUser();
        labelUtils.createDefaultLabels();

        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        token = jwt().jwt(builder -> builder.subject(userUtils.getAdminsEmail()));

        testLabel = Instancio.of(modelGenerator.getLabelModel())
                .create();
    }

    @AfterEach
    public void clearTables() {
        labelRepository.deleteAll();
    }

    @Test
    @DisplayName("Test GET request to /api/labels")
    public void testGetToApiLabels() throws Exception {
        labelRepository.save(testLabel);

        var result = mockMvc.perform(get("/api/labels")
                        .with(token))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
        assertThat(body).contains(testLabel.getName());
    }

    @Test
    @DisplayName("Test GET request to /api/labels (from unauthorized user)")
    public void testGetToApiLabelsFromUnauthorizedUser() throws Exception {
        labelRepository.save(testLabel);

        mockMvc.perform(get("/api/labels"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test GET request to /api/labels/{id}")
    public void testGetToApiLabelsId() throws Exception {
        labelRepository.save(testLabel);

        var result = mockMvc.perform(get("/api/labels/" + testLabel.getId())
                        .with(token))
                .andExpect(status().isOk())
                .andReturn();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String createdAt = testLabel.getCreatedAt().format(formatter);

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(testLabel.getId()),
                v -> v.node("name").isEqualTo(testLabel.getName()),
                v -> v.node("createdAt").isEqualTo(createdAt)
        );
    }

    @Test
    @DisplayName("Test GET request to /api/labels/{id} (from unauthorized user)")
    public void testGetToApiLabelsIdFromUnauthorizedUser() throws Exception {
        labelRepository.save(testLabel);

        mockMvc.perform(get("/api/labels/" + testLabel.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test POST request to /api/labels")
    public void testPostToApiLabels() throws Exception {
        var dto = new LabelCreateDTO();
        dto.setName(testLabel.getName());

        var request = post("/api/labels")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(testLabel.getName())
        );

        LabelDTO labelDTO = objectMapper.readValue(body, LabelDTO.class);

        Label label = labelRepository.findById(labelDTO.getId()).orElse(null);
        assertNotNull(label);
        assertThat(label.getName()).isEqualTo(testLabel.getName());
    }

    @Test
    @DisplayName("Test POST request to /api/labels (with not valid name)")
    public void testPostToApiLabelsWithNotValidName() throws Exception {
        var dto = new LabelCreateDTO();
        dto.setName("aa");

        var request = post("/api/labels")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test POST request to /api/labels (from unauthorized user)")
    public void testPostToApiLabelsFromUnauthorizedUser() throws Exception {
        var dto = new LabelCreateDTO();
        dto.setName(testLabel.getName());

        var request = post("/api/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        assertNull(labelRepository.findByName(testLabel.getName()).orElse(null));
    }

    @Test
    @DisplayName("Test PUT request to /api/labels/{id}")
    public void testPutToApiLabelsId() throws Exception {
        labelRepository.save(testLabel);

        Label testLabel2 = Instancio.of(modelGenerator.getLabelModel())
                .create();

        var dto = new LabelUpdateDTO();
        dto.setName(testLabel2.getName());

        var request = put("/api/labels/" + testLabel.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(testLabel2.getName())
        );

        Label label = labelRepository.findById(testLabel.getId()).orElse(null);
        assertNotNull(label);
        assertThat(label.getName()).isEqualTo(testLabel2.getName());
    }

    @Test
    @DisplayName("Test PUT request to /api/labels/{id} (with not valid name)")
    public void testPutToApiLabelsIWithNotValidName() throws Exception {
        labelRepository.save(testLabel);

        var dto = new LabelUpdateDTO();
        dto.setName("d");

        var request = post("/api/labels")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        Label label = labelRepository.findById(testLabel.getId()).orElse(null);
        assertNotNull(label);
        assertThat(label.getName()).isEqualTo(testLabel.getName());
    }

    @Test
    @DisplayName("Test PUT request to /api/labels/{id} (from unauthorized user)")
    public void testPutToApiLabelsIdUpdatingAllFieldsFromUnauthorizedUser() throws Exception {
        labelRepository.save(testLabel);

        Label testLabel2 = Instancio.of(modelGenerator.getLabelModel())
                .create();

        var dto = new LabelUpdateDTO();
        dto.setName(testLabel2.getName());

        var request = put("/api/labels/" + testLabel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        Label label = labelRepository.findById(testLabel.getId()).orElse(null);
        assertNotNull(label);
        assertThat(label.getName()).isEqualTo(testLabel.getName());
    }

    @Test
    @DisplayName("Test DELETE request to /api/labels/{id}")
    public void testDeleteToApiLabelsId() throws Exception {
        labelRepository.save(testLabel);

        long labelTaskId = testLabel.getId();

        mockMvc.perform(delete("/api/labels/" + labelTaskId)
                        .with(token))
                .andExpect(status().isNoContent());

        assertThat(labelRepository.existsById(labelTaskId)).isEqualTo(false);
    }

    @Test
    @DisplayName("Test DELETE request to /api/labels/{id} (from unauthorized user)")
    public void testDeleteToApiLabelsIdFromUnauthorizedUser() throws Exception {
        labelRepository.save(testLabel);

        long labelTaskId = testLabel.getId();

        mockMvc.perform(delete("/api/labels/" + labelTaskId))
                .andExpect(status().isUnauthorized());

        assertThat(labelRepository.existsById(labelTaskId)).isEqualTo(true);
    }
}
