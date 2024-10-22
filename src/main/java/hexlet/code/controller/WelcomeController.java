package hexlet.code.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/welcome")
@Hidden
public class WelcomeController {
    @GetMapping("")
    public String welcome() {
        return "Welcome to Spring";
    }
}
