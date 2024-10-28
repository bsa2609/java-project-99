package hexlet.code.controller.api;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TasksController {
    private final TaskService taskService;

    @GetMapping("")
    public ResponseEntity<List<TaskDTO>> index(
            @RequestParam(defaultValue = "") String titleCont,
            @RequestParam(defaultValue = "0") long assigneeId,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") long labelId) {
        List<TaskDTO> tasks = taskService.getAll(titleCont, assigneeId, status, labelId);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(tasks.size()))
                .body(tasks);
    }

    @GetMapping("/{id}")
    public TaskDTO show(@PathVariable long id) {
        return taskService.get(id);
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO create(@RequestBody @Valid TaskCreateDTO data) {
        return taskService.create(data);
    }

    @PutMapping("/{id}")
    public TaskDTO update(@PathVariable long id, @RequestBody @Valid TaskUpdateDTO data) {
        return taskService.update(id, data);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        taskService.delete(id);
    }
}
