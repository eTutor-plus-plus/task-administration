package at.jku.dke.etutor.task_administration.controllers;

import at.jku.dke.etutor.task_administration.dto.ModifyTaskCategoryDto;
import at.jku.dke.etutor.task_administration.dto.TaskCategoryDto;
import at.jku.dke.etutor.task_administration.services.TaskCategoryService;
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
 * Controller for managing {@link at.jku.dke.etutor.task_administration.data.entities.TaskCategory}.
 */
@RestController
@RequestMapping("/api/taskCategory")
@Tag(name = "Task Category", description = "Manage task categories")
@Validated
public class TaskCategoryController {

    private final TaskCategoryService taskCategoryService;

    /**
     * Creates a new instance of class {@link TaskCategoryController}.
     *
     * @param taskCategoryService The task category service.
     */
    public TaskCategoryController(TaskCategoryService taskCategoryService) {
        this.taskCategoryService = taskCategoryService;
    }

    /**
     * Load task category list.
     *
     * @param page                     The page of task categories to load.
     * @param nameFilter               The name filter.
     * @param parentFilter             The parent filter.
     * @param organizationalUnitFilter The organizational unit filter.
     * @return Page of task categories
     */
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task category list"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    @PageableAsQueryParam
    public ResponseEntity<Page<TaskCategoryDto>> getTaskCategories(Pageable page,
                                                                   @RequestParam(required = false) String nameFilter,
                                                                   @RequestParam(required = false) Long parentFilter,
                                                                   @RequestParam(required = false) Long organizationalUnitFilter) {
        var result = this.taskCategoryService.getTaskCategories(page, nameFilter, parentFilter, organizationalUnitFilter);
        return ResponseEntity.ok(result);
    }

    /**
     * Load task category details.
     *
     * @param id The task category identifier for which to load the task category details.
     * @return The task category details or a 404 error.
     */
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task category details"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "Task category does not exist", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<TaskCategoryDto> getTaskCategory(@PathVariable long id) {
        var dto = this.taskCategoryService.getTaskCategory(id);
        return dto
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new EntityNotFoundException("Task category with id " + id + " does not exist."));
    }

    /**
     * Creates a new task category.
     *
     * @param taskCategoryDto The task category data.
     * @return The details of the created task category.
     */
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Task category created"),
        @ApiResponse(responseCode = "400", description = "Validation of task category data failed.", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<TaskCategoryDto> createTaskCategory(@Valid @RequestBody ModifyTaskCategoryDto taskCategoryDto) {
        var taskCategory = this.taskCategoryService.create(taskCategoryDto);
        return ResponseEntity
            .created(URI.create("/api/taskCategory/" + taskCategory.getId()))
            .body(new TaskCategoryDto(taskCategory));
    }

    /**
     * Updates an existing task category.
     *
     * @param id               The identifier of the task category to update.
     * @param taskCategoryDto  The new task category data.
     * @param concurrencyToken The concurrency token.
     * @return No content
     */
    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task category updated"),
        @ApiResponse(responseCode = "400", description = "Validation of task category data failed.", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "Task category not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "409", description = "Task category has been modified by someone else", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<Void> updateTaskCategory(@PathVariable long id,
                                                   @Valid @RequestBody ModifyTaskCategoryDto taskCategoryDto,
                                                   @RequestHeader(value = "If-Unmodified-Since", required = false) Instant concurrencyToken) {
        this.taskCategoryService.update(id, taskCategoryDto, concurrencyToken);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes the task category with the specified identifier.
     *
     * @param id The task category identifier.
     * @return No content
     */
    @DeleteMapping("/{id}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task category deleted or task category does not exist"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<Void> deleteTaskCategory(@PathVariable long id) {
        this.taskCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Forces moodle synchronization for the task category.
     *
     * @param id The identifier of the task category to update.
     * @return Accepted
     */
    @PostMapping(value = "/{id}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Task category synchronization initiated"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "Task category not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<Void> syncMoodle(@PathVariable long id) {
        this.taskCategoryService.createMoodleObjectsForTaskCategory(id);
        return ResponseEntity.accepted().build();
    }
}
