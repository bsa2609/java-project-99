package hexlet.code.service;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskUpdateDTO;

import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;

    public List<TaskDTO> getAll(String titleCont, long assigneeId, String status, long labelId) {
        return taskRepository.findAllUsingFilters(titleCont, assigneeId, status, labelId).stream()
                .map(taskMapper::map)
                .toList();
    }

    public TaskDTO get(long id) {
        Task task = findById(id);

        return taskMapper.map(task);
    }

    public TaskDTO create(TaskCreateDTO data) {
        Task task = taskMapper.map(data);
        taskRepository.save(task);

        return taskMapper.map(task);
    }

    public TaskDTO update(long id, TaskUpdateDTO data) {
        Task task = findById(id);

        taskMapper.update(data, task);
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
