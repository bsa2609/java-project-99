package hexlet.code;

import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AppApplicationTests {
    @Autowired
    private UserRepository userRepository;

    @AfterEach
    public void clearTables() {
        userRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }
}
