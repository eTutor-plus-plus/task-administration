package at.jku.dke.etutor.task_administration.controllers;

import at.jku.dke.etutor.task_administration.data.entities.TaskStatus;
import at.jku.dke.etutor.task_administration.dto.CombinedDto;
import at.jku.dke.etutor.task_administration.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_administration.dto.SubmitSubmissionDto;
import at.jku.dke.etutor.task_administration.dto.TaskDto;
import at.jku.dke.etutor.task_administration.services.TaskService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * Controller for managing {@link at.jku.dke.etutor.task_administration.data.entities.Task}.
 */
@RestController
@RequestMapping("/api/task")
@Tag(name = "Task", description = "Manage tasks")
@Validated
public class TaskController {

    private final TaskService taskService;

    /**
     * Creates a new instance of class {@link TaskController}.
     *
     * @param taskService The task service.
     */
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Load task list.
     *
     * @param page            The page of tasks to load.
     * @param nameFilter      The name filter.
     * @param statusFilter    The status filter.
     * @param taskTypeFilter  The task type filter.
     * @param orgUnitFilter   The organizational unit filter.
     * @param taskGroupFilter The task group filter.
     * @return Page of tasks
     */
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task list"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    @PageableAsQueryParam
    public ResponseEntity<Page<TaskDto>> getTasks(Pageable page,
                                                  @RequestParam(required = false) String nameFilter,
                                                  @RequestParam(required = false) TaskStatus statusFilter,
                                                  @RequestParam(required = false) String taskTypeFilter,
                                                  @RequestParam(required = false) Long orgUnitFilter,
                                                  @RequestParam(required = false) Long taskGroupFilter) {
        var result = this.taskService.getTasks(page, nameFilter, statusFilter, taskTypeFilter, orgUnitFilter, taskGroupFilter);
        return ResponseEntity.ok(result);
    }

    /**
     * Load task details.
     *
     * @param id The task identifier for which to load the task details.
     * @return The task details or a 404 error.
     */
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task details"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "Task does not exist", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "424", description = "Operation in task app failed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<CombinedDto<TaskDto>> getTask(@PathVariable long id) {
        var dto = this.taskService.getTask(id);
        return dto
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new EntityNotFoundException("Task with id " + id + " does not exist."));
    }

    /**
     * Load task types.
     *
     * @return Page of task types
     */
    @GetMapping(value = "/types", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task types"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<List<String>> getTaskTypes() {
        var result = this.taskService.getTaskTypes();
        return ResponseEntity.ok(result);
    }

    /**
     * Export the tasks.
     *
     * @return List of tasks
     */
    @GetMapping(value = "/export", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<List<CombinedDto<TaskDto>>> export() {
        var result = this.taskService.export();
        return ResponseEntity.ok(result);
    }

    /**
     * Creates a new task.
     *
     * @param taskDto The task data.
     * @return The details of the created task.
     */
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Task created"),
        @ApiResponse(responseCode = "400", description = "Validation of task data failed.", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "424", description = "Operation in task app failed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody ModifyTaskDto taskDto) {
        var task = this.taskService.create(taskDto);
        return ResponseEntity
            .created(URI.create("/api/task/" + task.getId()))
            .body(new TaskDto(task));
    }

    /**
     * Updates an existing task.
     *
     * @param id               The identifier of the task to update.
     * @param taskDto          The new task data.
     * @param concurrencyToken The concurrency token.
     * @return No content
     */
    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task updated"),
        @ApiResponse(responseCode = "400", description = "Validation of task data failed.", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "409", description = "Task has been modified by someone else", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "424", description = "Operation in task app failed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<Void> updateTask(@PathVariable long id,
                                           @Valid @RequestBody ModifyTaskDto taskDto,
                                           @RequestHeader(value = "If-Unmodified-Since", required = false) Instant concurrencyToken) {
        this.taskService.update(id, taskDto, concurrencyToken);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes the task with the specified identifier.
     *
     * @param id The task identifier.
     * @return No content
     */
    @DeleteMapping("/{id}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task deleted or task does not exist"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "424", description = "Operation in task app failed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<Void> deleteTask(@PathVariable long id) {
        this.taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Submits a task for testing purpose.
     *
     * @param submissionDto The submission.
     * @return The evaluation result.
     */
    @PostMapping(value = "/submit", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Submission evaluated"),
        @ApiResponse(responseCode = "400", description = "Validation of task data failed.", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "424", description = "Operation in task app failed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "503", description = "Task does not support testing submissions", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<Serializable> submit(@Valid @RequestBody SubmitSubmissionDto submissionDto) {
        var result = this.taskService.submit(submissionDto);
        return ResponseEntity.ok(result);
    }

    /**
     * Forces moodle synchronization for the task.
     *
     * @param id The identifier of the task to update.
     * @return Accepted
     */
    @Transactional
    @PostMapping(value = "/{id}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Task synchronization initiated"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<Void> syncMoodle(@PathVariable long id) {
        this.taskService.updateMoodleObjectsForTask(id);
        return ResponseEntity.accepted().build();
    }
}
