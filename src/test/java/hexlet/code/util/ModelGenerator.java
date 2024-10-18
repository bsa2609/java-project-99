package hexlet.code.util;

import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.service.TaskStatusService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
public class ModelGenerator {
    private final int minTaskStatusIndex = 1;
    private final int maxTaskStatusIndex = 1000;

    private Model<Task> taskModel;

    @Autowired
    private Faker faker;

    @Autowired
    private TaskStatusService taskStatusService;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private TaskStatusUtil taskStatusUtil;

    @PostConstruct
    private void init() {
        taskModel = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .ignore(Select.field(Task::getCreatedAt))
                .supply(Select.field(Task::getName), () -> faker.gameOfThrones().house())
                .supply(Select.field(Task::getDescription), () -> faker.gameOfThrones().quote())
                .supply(Select.field(Task::getIndex), () -> getRandomInt(minTaskStatusIndex, maxTaskStatusIndex))
                .supply(Select.field(Task::getTaskStatus), () -> getRandomDefaultTaskStatus())
                .supply(Select.field(Task::getAssignee), () -> userUtils.getAdminUser())
                .toModel();

    }

    private int getRandomInt(int min, int max) {
        return min + (int) Math.round(Math.random() * (max - min));
    }

    private TaskStatus getRandomDefaultTaskStatus() {
        List<String[]> defaultTaskStatuses = taskStatusUtil.getDefaultTaskStatuses();

        int randomIndex = getRandomInt(0, defaultTaskStatuses.size() - 1);
        String randomSlug = defaultTaskStatuses.get(randomIndex)[0];

        return taskStatusService.findBySlug(randomSlug);
    }
}
