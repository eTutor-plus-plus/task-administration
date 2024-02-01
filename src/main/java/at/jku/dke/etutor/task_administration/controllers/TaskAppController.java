package at.jku.dke.etutor.task_administration.controllers;

import at.jku.dke.etutor.task_administration.dto.ModifyTaskAppDto;
import at.jku.dke.etutor.task_administration.dto.TaskAppDto;
import at.jku.dke.etutor.task_administration.services.TaskAppService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;

/**
 * Controller for managing {@link at.jku.dke.etutor.task_administration.data.entities.TaskApp}.
 */
@RestController
@RequestMapping("/api/taskApp")
@Tag(name = "Task App", description = "Manage task apps")
@Validated
public class TaskAppController {

    private final TaskAppService taskAppService;

    /**
     * Creates a new instance of class {@link TaskAppController}.
     *
     * @param taskAppService The task app service.
     */
    public TaskAppController(TaskAppService taskAppService) {
        this.taskAppService = taskAppService;
    }

    /**
     * Load task app list.
     *
     * @param page           The page of task apps to load.
     * @param urlFilter      The text used to filter the task app url.
     * @param taskTypeFilter The text used to filter the task app type.
     * @return Page of task apps
     */
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task app list"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    @PageableAsQueryParam
    public ResponseEntity<Page<TaskAppDto>> getTaskApps(Pageable page, @RequestParam(required = false) String urlFilter, @RequestParam(required = false) String taskTypeFilter) {
        var result = this.taskAppService.getTaskApps(page, urlFilter, taskTypeFilter);
        return ResponseEntity.ok(result);
    }

    /**
     * Load task app details.
     *
     * @param id The task app identifier for which to load the task app details.
     * @return The task app details or a 404 error.
     */
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task app details"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "Task app does not exist", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<TaskAppDto> getTaskApp(@PathVariable long id) {
        var dto = this.taskAppService.getTaskApp(id);
        return dto
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new EntityNotFoundException("Task app with id " + id + " does not exist."));
    }

    /**
     * Creates a new task app.
     *
     * @param taskAppDto The task app data.
     * @return The details of the created task app.
     */
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Task app created"),
        @ApiResponse(responseCode = "400", description = "Validation of task app data failed.", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<TaskAppDto> createTaskApp(@Valid @RequestBody ModifyTaskAppDto taskAppDto) {
        var taskApp = this.taskAppService.create(taskAppDto);
        return ResponseEntity
            .created(URI.create("/api/taskApp/" + taskApp.getId()))
            .body(new TaskAppDto(taskApp));
    }

    /**
     * Updates an existing task app.
     *
     * @param id               The identifier of the task app to update.
     * @param taskAppDto       The new task app data.
     * @param concurrencyToken The concurrency token.
     * @return No content
     */
    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task app updated"),
        @ApiResponse(responseCode = "400", description = "Validation of task app data failed.", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "Task app not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "409", description = "Task app has been modified by someone else", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<Void> updateTaskApp(@PathVariable long id,
                                              @Valid @RequestBody ModifyTaskAppDto taskAppDto,
                                              @RequestHeader(value = "If-Unmodified-Since", required = false) Instant concurrencyToken) {
        this.taskAppService.update(id, taskAppDto, concurrencyToken);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes the task app with the specified identifier.
     *
     * @param id The task app identifier.
     * @return No content
     */
    @DeleteMapping("/{id}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task app deleted or task app does not exist"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<Void> deleteTaskApp(@PathVariable long id) {
        this.taskAppService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
