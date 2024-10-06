package hexlet.code.component;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {
    private final UserService userService;

    @Autowired
    public DataInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String email = "hexlet@example.com";
        String password = "qwerty";

        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail(email);
        userCreateDTO.setPassword(password);

        userService.create(userCreateDTO);
    }
}
