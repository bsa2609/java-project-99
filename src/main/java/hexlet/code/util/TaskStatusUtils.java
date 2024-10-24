package hexlet.code.util;

import hexlet.code.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.service.TaskStatusService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
public class TaskStatusUtils {
    private final List<String[]> defaultTaskStatuses = List.of(
            new String[]{"draft", "Draft"},
            new String[]{"to_review", "To review"},
            new String[]{"to_be_fixed", "To be fixed"},
            new String[]{"to_publish", "To publish"},
            new String[]{"published", "Published"});

    @Autowired
    private TaskStatusService taskStatusService;

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
