package hexlet.code.dto.taskStatus;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class TaskStatusUpdateDTO {
    @NotBlank(message = "Name may not be blank")
    private JsonNullable<String> name;

    @NotBlank(message = "Slug may not be blank")
    private JsonNullable<String> slug;
}
