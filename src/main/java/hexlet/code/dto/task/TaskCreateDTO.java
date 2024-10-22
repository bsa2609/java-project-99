package hexlet.code.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;

@Getter
@Setter
public class TaskCreateDTO {
    private JsonNullable<Integer> index;
    private JsonNullable<String> content;
    private JsonNullable<List<Long>> taskLabelIds;

    @JsonProperty("assignee_id")
    private JsonNullable<Long> assigneeId;

    @NotBlank(message = "Title may not be blank")
    private String title;

    @NotBlank(message = "Status may not be blank")
    private String status;
}
