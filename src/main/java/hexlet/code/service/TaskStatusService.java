package hexlet.code.service;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.EntityNotUniqueException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskStatusService {
    private final TaskStatusMapper taskStatusMapper;
    private final TaskStatusRepository taskStatusRepository;

    @Autowired
    public TaskStatusService(TaskStatusMapper taskStatusMapper, TaskStatusRepository taskStatusRepository) {
        this.taskStatusMapper = taskStatusMapper;
        this.taskStatusRepository = taskStatusRepository;
    }

    public List<TaskStatusDTO> getAll() {
        return taskStatusRepository.findAll().stream()
                .map(taskStatusMapper::map)
                .toList();
    }

    public TaskStatusDTO get(long id) {
        TaskStatus taskStatus = findById(id);

        return taskStatusMapper.map(taskStatus);
    }

    public TaskStatusDTO create(TaskStatusCreateDTO data) {
        String slug = data.getSlug();
        if (isSlugExists(slug)) {
            throw new EntityNotUniqueException(
                    String.format("Task status with slug %s already exists", slug)
            );
        }

        String name = data.getName();
        if (isNameExists(name)) {
            throw new EntityNotUniqueException(
                    String.format("Task status with name %s already exists", name)
            );
        }

        TaskStatus taskStatus = taskStatusMapper.map(data);
        taskStatusRepository.save(taskStatus);

        return taskStatusMapper.map(taskStatus);
    }

    public TaskStatusDTO update(long id, TaskStatusUpdateDTO data) {
        TaskStatus taskStatus = findById(id);

        taskStatusMapper.update(data, taskStatus);
        taskStatusRepository.save(taskStatus);

        return taskStatusMapper.map(taskStatus);
    }

    public void delete(long id) {
        taskStatusRepository.deleteById(id);
    }

    public TaskStatus findById(long id) {
        return taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Task status with id %s not found", id)
                ));
    }

    public boolean isSlugExists(String slug) {
        return taskStatusRepository.existsBySlug(slug);
    }

    public boolean isNameExists(String name) {
        return taskStatusRepository.existsByName(name);
    }
}
