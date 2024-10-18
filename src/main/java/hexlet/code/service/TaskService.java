package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ReferenceNotFoundException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskMapper taskMapper, TaskRepository taskRepository) {
        this.taskMapper = taskMapper;
        this.taskRepository = taskRepository;
    }

    public List<TaskDTO> getAll() {
        return taskRepository.findAll().stream()
                .map(taskMapper::map)
                .toList();
    }

    public TaskDTO get(long id) {
        Task task = findById(id);

        return taskMapper.map(task);
    }

    public TaskDTO create(TaskCreateDTO data) {
        Task task = taskMapper.map(data);

        if (task.getTaskStatus() == null) {
            throw new ReferenceNotFoundException(
                    String.format("Task status with slug %s not found. Task cannot be created.", data.getStatus())
            );
        }

        if (data.getAssigneeId() != null
                && data.getAssigneeId().isPresent()
                && task.getAssignee() == null) {
            throw new ReferenceNotFoundException(
                    String.format("User with id %s not found. Task cannot be created.", data.getAssigneeId().get())
            );
        }

        taskRepository.save(task);

        return taskMapper.map(task);
    }

    public TaskDTO update(long id, TaskUpdateDTO data) {
        Task task = findById(id);

        taskMapper.update(data, task);

        if (data.getStatus() != null
                && data.getStatus().isPresent()
                && task.getTaskStatus() == null) {
            throw new ReferenceNotFoundException(
                    String.format("Task status with slug %s not found. Task cannot be updated.", data.getStatus().get())
            );
        }

        if (data.getAssigneeId() != null
                && data.getAssigneeId().isPresent()
                && task.getAssignee() == null) {
            throw new ReferenceNotFoundException(
                    String.format("User with id %s not found. Task cannot be updated.", data.getAssigneeId().get())
            );
        }

        taskRepository.save(task);

        return taskMapper.map(task);
    }

    public void delete(long id) {
        taskRepository.deleteById(id);
    }

    public Task findById(long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Task with id %s not found", id)
                ));
    }
}
