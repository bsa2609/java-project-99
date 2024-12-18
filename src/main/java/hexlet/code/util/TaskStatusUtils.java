package hexlet.code.util;

import hexlet.code.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.service.TaskStatusService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@RequiredArgsConstructor
public class TaskStatusUtils {
    private final TaskStatusService taskStatusService;

    private final List<String[]> defaultTaskStatuses = List.of(
            new String[]{"draft", "Draft"},
            new String[]{"to_review", "To review"},
            new String[]{"to_be_fixed", "To be fixed"},
            new String[]{"to_publish", "To publish"},
            new String[]{"published", "Published"});

    public void createDefaultTaskStatuses() {
        for (String[] defaultTaskStatus : defaultTaskStatuses) {
            String slug = defaultTaskStatus[0];
            String name = defaultTaskStatus[1];

            if (!taskStatusService.isSlugExists(slug) && !taskStatusService.isNameExists(name)) {
                TaskStatusCreateDTO taskStatusCreateDTO = new TaskStatusCreateDTO();
                taskStatusCreateDTO.setName(name);
                taskStatusCreateDTO.setSlug(slug);

                taskStatusService.create(taskStatusCreateDTO);
            }
        }
    }
}
