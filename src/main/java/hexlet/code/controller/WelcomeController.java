package hexlet.code.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/welcome")
public class WelcomeController {
    @GetMapping("")
    public String welcome() {
        return "Welcome to Spring";
    }

    //@GetMapping("")
    //public String root() {
    //    return "It is root!";
    //}
}
