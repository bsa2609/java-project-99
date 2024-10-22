package hexlet.code.dto.taskStatus;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusCreateDTO {
    @NotBlank(message = "Name may not be blank")
    private String name;

    @NotBlank(message = "Slug may not be blank")
    private String slug;
}
