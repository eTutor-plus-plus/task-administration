package at.jku.dke.etutor.task_administration.controllers;

import at.jku.dke.etutor.task_administration.data.entities.TaskStatus;
import at.jku.dke.etutor.task_administration.dto.CombinedDto;
import at.jku.dke.etutor.task_administration.dto.ModifyTaskGroupDto;
import at.jku.dke.etutor.task_administration.dto.TaskGroupDto;
import at.jku.dke.etutor.task_administration.services.TaskGroupService;
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
import java.util.List;

/**
 * Controller for managing {@link at.jku.dke.etutor.task_administration.data.entities.TaskGroup}.
 */
@RestController
@RequestMapping("/api/taskGroup")
@Tag(name = "Task Group", description = "Manage task groups")
@Validated
public class TaskGroupController {

    private final TaskGroupService taskGroupService;

    /**
     * Creates a new instance of class {@link TaskGroupController}.
     *
     * @param taskGroupService The task group service.
     */
    public TaskGroupController(TaskGroupService taskGroupService) {
        this.taskGroupService = taskGroupService;
    }

    /**
     * Load task group list.
     *
     * @param page                The page of task groups to load.
     * @param nameFilter          The name filter.
     * @param statusFilter        The status filter.
     * @param taskGroupTypeFilter The task group type filter.
     * @param orgUnitFilter       The organizational unit filter.
     * @return Page of task groups
     */
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task group list"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    @PageableAsQueryParam
    public ResponseEntity<Page<TaskGroupDto>> getTaskGroups(Pageable page,
                                                            @RequestParam(required = false) String nameFilter,
                                                            @RequestParam(required = false) TaskStatus statusFilter,
                                                            @RequestParam(required = false) String taskGroupTypeFilter,
                                                            @RequestParam(required = false) Long orgUnitFilter) {
        var result = this.taskGroupService.getTaskGroups(page, nameFilter, statusFilter, taskGroupTypeFilter, orgUnitFilter);
        return ResponseEntity.ok(result);
    }

    /**
     * Load task group details.
     *
     * @param id The task group identifier for which to load the task group details.
     * @return The task group details or a 404 error.
     */
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task group details"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "Task group does not exist", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "424", description = "Operation in task app failed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<CombinedDto<TaskGroupDto>> getTaskGroup(@PathVariable long id) {
        var dto = this.taskGroupService.getTaskGroup(id);
        return dto
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new EntityNotFoundException("Task group with id " + id + " does not exist."));
    }

    /**
     * Load task group types.
     *
     * @return Page of task group types
     */
    @GetMapping(value = "/types", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task group types"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<List<String>> getTaskGroupTypes() {
        var result = this.taskGroupService.getTaskTypes();
        return ResponseEntity.ok(result);
    }

    /**
     * Creates a new task group.
     *
     * @param taskGroupDto The task group data.
     * @return The details of the created task group.
     */
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Task group created"),
        @ApiResponse(responseCode = "400", description = "Validation of task group data failed.", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "424", description = "Operation in task app failed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<TaskGroupDto> createTaskGroup(@Valid @RequestBody ModifyTaskGroupDto taskGroupDto) {
        var taskGroup = this.taskGroupService.create(taskGroupDto);
        return ResponseEntity
            .created(URI.create("/api/taskGroup/" + taskGroup.getId()))
            .body(new TaskGroupDto(taskGroup));
    }

    /**
     * Updates an existing task group.
     *
     * @param id               The identifier of the task group to update.
     * @param taskGroupDto     The new task group data.
     * @param concurrencyToken The concurrency token.
     * @return No content
     */
    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task group updated"),
        @ApiResponse(responseCode = "400", description = "Validation of task group data failed.", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "Task group not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "409", description = "Task group has been modified by someone else", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "424", description = "Operation in task app failed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<Void> updateTaskGroup(@PathVariable long id,
                                                @Valid @RequestBody ModifyTaskGroupDto taskGroupDto,
                                                @RequestHeader(value = "If-Unmodified-Since", required = false) Instant concurrencyToken) {
        this.taskGroupService.update(id, taskGroupDto, concurrencyToken);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes the task group with the specified identifier.
     *
     * @param id The task group identifier.
     * @return No content
     */
    @DeleteMapping("/{id}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task group deleted or task group does not exist"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "424", description = "Operation in task app failed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<Void> deleteTaskGroup(@PathVariable long id) {
        this.taskGroupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
