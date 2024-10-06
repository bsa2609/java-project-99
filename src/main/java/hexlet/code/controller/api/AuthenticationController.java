package hexlet.code.controller.api;

import hexlet.code.dto.AuthRequest;
import hexlet.code.util.JWTUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
public class AuthenticationController {
    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("")
    public String login(@RequestBody @Valid AuthRequest authRequest) {
        var authentication = new UsernamePasswordAuthenticationToken(
                authRequest.getUsername(), authRequest.getPassword()
        );

        authenticationManager.authenticate(authentication);

        return jwtUtils.generateToken(authRequest.getUsername());
    }
}
