package hexlet.code.util;

import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.service.LabelService;
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
    private Model<Label> labelModel;

    @Autowired
    private Faker faker;

    @Autowired
    private TaskStatusService taskStatusService;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private TaskStatusUtils taskStatusUtils;

    @Autowired
    private LabelUtils labelUtils;

    @Autowired
    private LabelService labelService;

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
                .supply(Select.field(Task::getLabels), () -> getRandomDefaultLabel())
                .toModel();

        labelModel = Instancio.of(Label.class)
                .ignore(Select.field(Label::getId))
                .ignore(Select.field(Label::getCreatedAt))
                .ignore(Select.field(Label::getTasks))
                .supply(Select.field(Label::getName), () -> faker.gameOfThrones().city())
                .toModel();

    }

    private int getRandomInt(int min, int max) {
        return min + (int) Math.round(Math.random() * (max - min));
    }

    private TaskStatus getRandomDefaultTaskStatus() {
        List<String[]> defaultTaskStatuses = taskStatusUtils.getDefaultTaskStatuses();

        int randomIndex = getRandomInt(0, defaultTaskStatuses.size() - 1);
        String randomSlug = defaultTaskStatuses.get(randomIndex)[0];

        return taskStatusService.findBySlug(randomSlug);
    }

    private List<Label> getRandomDefaultLabel() {
        List<String> defaultLabels = labelUtils.getDefaultLabels();

        int randomIndex = getRandomInt(0, defaultLabels.size() - 1);
        String randomName = defaultLabels.get(randomIndex);

        return List.of(labelService.findByName(randomName));
    }
}
