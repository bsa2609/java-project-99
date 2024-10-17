package hexlet.code.component;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.service.TaskStatusService;
import hexlet.code.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {
    private final UserService userService;
    private final TaskStatusService taskStatusService;

    private final List<String[]> defaultTaskStatuses = List.of(
            new String[]{"draft", "Draft"},
            new String[]{"to_review", "To review"},
            new String[]{"to_be_fixed", "To be fixed"},
            new String[]{"to_publish", "To publish"},
            new String[]{"published", "Published"});

    @Autowired
    public DataInitializer(UserService userService, TaskStatusService taskStatusService) {
        this.userService = userService;
        this.taskStatusService = taskStatusService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String email = "hexlet@example.com";
        String password = "qwerty";

        if (!userService.isEmailExists(email)) {
            UserCreateDTO userCreateDTO = new UserCreateDTO();
            userCreateDTO.setEmail(email);
            userCreateDTO.setPassword(password);

            userService.create(userCreateDTO);
        }

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
