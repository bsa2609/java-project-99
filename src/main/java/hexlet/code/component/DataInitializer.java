package hexlet.code.component;

import hexlet.code.util.LabelUtil;
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
    private final LabelUtil labelUtil;

    @Autowired
    public DataInitializer(UserUtils userUtils, TaskStatusUtil taskStatusUtil, LabelUtil labelUtil) {
        this.userUtils = userUtils;
        this.taskStatusUtil = taskStatusUtil;
        this.labelUtil = labelUtil;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        userUtils.createAdminUser();
        taskStatusUtil.createDefaultTaskStatuses();
        labelUtil.createDefaultLabels();
    }
}
