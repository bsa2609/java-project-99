package hexlet.code.mapper;

import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public abstract class ReferenceTaskStatusMapper {
    @Autowired
    private TaskStatusRepository taskStatusRepository;

    public TaskStatus toEntity(String slug) {
        return slug != null ? taskStatusRepository.findBySlug(slug).orElse(null) : null;
    }
}

