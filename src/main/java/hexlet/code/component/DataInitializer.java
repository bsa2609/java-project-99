package hexlet.code.component;

import hexlet.code.util.LabelUtils;
import hexlet.code.util.TaskStatusUtils;
import hexlet.code.util.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {
    private final UserUtils userUtils;
    private final TaskStatusUtils taskStatusUtils;
    private final LabelUtils labelUtils;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        userUtils.createAdminUser();
        taskStatusUtils.createDefaultTaskStatuses();
        labelUtils.createDefaultLabels();
    }
}
