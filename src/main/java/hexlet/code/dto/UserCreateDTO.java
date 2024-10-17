package hexlet.code.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class UserCreateDTO {
    private JsonNullable<String> firstName;
    private JsonNullable<String> lastName;

    @NotBlank(message = "Email may not be blank")
    @Email(message = "Email address is incorrect")
    private String email;

    @NotBlank(message = "Password name may not be blank")
    @Size(min = 3, message = "The password must be at least 3 characters long")
    private String password;
}
