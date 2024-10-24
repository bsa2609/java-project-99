package hexlet.code.util;

import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.service.LabelService;
import hexlet.code.service.TaskStatusService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.datafaker.Faker;
import net.datafaker.providers.base.Text;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.datafaker.providers.base.Text.DEFAULT_SPECIAL;
import static net.datafaker.providers.base.Text.DIGITS;
import static net.datafaker.providers.base.Text.EN_LOWERCASE;
import static net.datafaker.providers.base.Text.EN_UPPERCASE;

@Getter
@Component
public class ModelGenerator {
    private final int minTaskStatusIndex = 1;
    private final int maxTaskStatusIndex = 1000;

    private final int minPasswordLength = 5;
    private final int maxPasswordLength = 10;

    private Model<Task> taskModel;
    private Model<Label> labelModel;
    private Model<User> userModel;

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

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        userModel = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getPassword), () -> generateHashedPassword())
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
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

    public String generatePassword() {
        int passwordLength = getRandomInt(minPasswordLength, maxPasswordLength);

        return faker.text().text(Text.TextSymbolsBuilder.builder()
                .len(passwordLength)
                .with(EN_LOWERCASE, 1)
                .with(EN_UPPERCASE, 1)
                .with(DIGITS, 1)
                .with(DEFAULT_SPECIAL, 1)
                .build()
        );
    }

    public String generateHashedPassword() {
        String password = generatePassword();

        return passwordEncoder.encode(password);
    }
}
