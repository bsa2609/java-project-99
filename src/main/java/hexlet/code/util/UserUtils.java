package hexlet.code.util;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.UserService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Getter
public class UserUtils {
    private final String adminsEmail = "hexlet@example.com";
    private final String adminsPassword = "qwerty";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        var email = authentication.getName();
        return userRepository.findByEmail(email).get();
    }

    public boolean isCurrentUserId(long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        return currentUser.getId() == id;
    }

    public void createAdminUser() {
        if (!userService.isEmailExists(adminsEmail)) {
            UserCreateDTO userCreateDTO = new UserCreateDTO();
            userCreateDTO.setEmail(adminsEmail);
            userCreateDTO.setPassword(adminsPassword);

            userService.create(userCreateDTO);
        }
    }

    public User getAdminUser() {
        return userRepository.findByEmail(adminsEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with email %s not found", adminsEmail)
                ));
    }
}
