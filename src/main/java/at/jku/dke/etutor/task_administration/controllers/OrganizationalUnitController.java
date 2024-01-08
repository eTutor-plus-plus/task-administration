package at.jku.dke.etutor.task_administration.controllers;

import at.jku.dke.etutor.task_administration.dto.ModifyOrganizationalUnitDto;
import at.jku.dke.etutor.task_administration.dto.OrganizationalUnitDto;
import at.jku.dke.etutor.task_administration.services.OrganizationalUnitService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;

/**
 * Controller for managing {@link at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnit}.
 */
@RestController
@RequestMapping("/api/organizationalUnit")
@Tag(name = "Organizational Unit", description = "Manage organizational units")
public class OrganizationalUnitController {

    private final OrganizationalUnitService organizationalUnitService;

    /**
     * Creates a new instance of class {@link OrganizationalUnitController}.
     *
     * @param organizationalUnitService The organizational unit service.
     */
    public OrganizationalUnitController(OrganizationalUnitService organizationalUnitService) {
        this.organizationalUnitService = organizationalUnitService;
    }

    /**
     * Load organizational unit list.
     *
     * @param page   The page of organizational units to load.
     * @param filter The text used to filter the organizational units.
     * @return Page of organizational units
     */
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Organizational unit list"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    @PageableAsQueryParam
    public ResponseEntity<Page<OrganizationalUnitDto>> getOrganizationalUnits(Pageable page, @RequestParam(required = false) String filter) {
        var result = this.organizationalUnitService.getOrganizationalUnits(page, filter);
        return ResponseEntity.ok(result);
    }

    /**
     * Load organizational unit details.
     *
     * @param id The organizational unit identifier for which to load the organizational unit details.
     * @return The organizational unit details or a 404 error.
     */
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Organizational unit details"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "Organizational unit does not exist", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<OrganizationalUnitDto> getOrganizationalUnit(@PathVariable long id) {
        var dto = this.organizationalUnitService.getOrganizationalUnit(id);
        return dto
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * Creates a new organizational unit.
     *
     * @param organizationalUnitDto The organizational unit data.
     * @return The details of the created organizational unit.
     */
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Organizational unit created"),
        @ApiResponse(responseCode = "400", description = "Validation of organizational unit data failed.", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<OrganizationalUnitDto> createOrganizationalUnit(@Valid @RequestBody ModifyOrganizationalUnitDto organizationalUnitDto) {
        var organizationalUnit = this.organizationalUnitService.create(organizationalUnitDto);
        return ResponseEntity
            .created(URI.create("/api/organizationalUnit/" + organizationalUnit.getId()))
            .body(new OrganizationalUnitDto(organizationalUnit));
    }

    /**
     * Updates an existing organizational unit.
     *
     * @param id                    The identifier of the organizational unit to update.
     * @param organizationalUnitDto The new organizational unit data.
     * @param concurrencyToken      The concurrency token.
     * @return No content
     */
    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Organizational unit updated"),
        @ApiResponse(responseCode = "400", description = "Validation of organizational unit data failed.", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "Organizational unit not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "409", description = "Organizational unit has been modified by someone else", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<Void> updateOrganizationalUnit(@PathVariable long id,
                                                         @Valid @RequestBody ModifyOrganizationalUnitDto organizationalUnitDto,
                                                         @RequestHeader(value = "If-Unmodified-Since", required = false) Instant concurrencyToken) {
        this.organizationalUnitService.update(id, organizationalUnitDto, concurrencyToken);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes the organizational unit with the specified identifier.
     *
     * @param id The organizational unit identifier.
     * @return No content
     */
    @DeleteMapping("/{id}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Organizational unit deleted or organizational unit does not exist"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<Void> deleteOrganizationalUnit(@PathVariable long id) {
        this.organizationalUnitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
