package hexlet.code.service;

import hexlet.code.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.dto.taskStatus.TaskStatusDTO;
import hexlet.code.dto.taskStatus.TaskStatusUpdateDTO;

import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskStatusService {
    private final TaskStatusMapper taskStatusMapper;
    private final TaskStatusRepository taskStatusRepository;

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

    public TaskStatus findBySlug(String slug) {
        return taskStatusRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Task status with slug %s not found", slug)
                ));
    }
}
