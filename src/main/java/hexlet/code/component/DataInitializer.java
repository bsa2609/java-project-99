package hexlet.code.component;

import hexlet.code.util.TaskStatusUtil;
import hexlet.code.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {
    private final UserUtils userUtils;
    private final TaskStatusUtil taskStatusUtil;

    @Autowired
    public DataInitializer(UserUtils userUtils, TaskStatusUtil taskStatusUtil) {
        this.userUtils = userUtils;
        this.taskStatusUtil = taskStatusUtil;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        userUtils.createAdminUser();
        taskStatusUtil.createDefaultTaskStatuses();
    }
}
