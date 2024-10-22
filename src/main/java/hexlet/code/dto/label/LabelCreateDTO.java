package hexlet.code.dto.label;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelCreateDTO {
    @NotBlank(message = "Name may not be blank")
    @Size(min = 3, message = "Name must be at least 3 characters long")
    @Size(max = 1000, message = "Name must be no more than 1000 characters long")
    private String name;
}
