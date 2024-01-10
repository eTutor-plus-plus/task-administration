package at.jku.dke.etutor.task_administration.controllers;

import at.jku.dke.etutor.task_administration.dto.ModifyUserDto;
import at.jku.dke.etutor.task_administration.dto.ModifyUserPasswordDto;
import at.jku.dke.etutor.task_administration.dto.UserDto;
import at.jku.dke.etutor.task_administration.services.UserService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;

/**
 * Controller for managing {@link at.jku.dke.etutor.task_administration.data.entities.User}s.
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "User", description = "Manage users")
@Validated
public class UserController {

    private final UserService userService;

    /**
     * Creates a new instance of class {@link UserController}.
     *
     * @param userService The user service.
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Load user list.
     *
     * @param page            The page of users to load.
     * @param usernameFilter  Optional filter string (applies contains to username).
     * @param firstNameFilter Optional filter string (applies contains to first name).
     * @param lastNameFilter  Optional filter string (applies contains to last name).
     * @param emailFilter     Optional filter string (applies contains to email).
     * @param enabledFilter   Optional filter for active state.
     * @param fullAdminFilter Optional filter for full admin state.
     * @return Page of users
     */
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User list"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    @PageableAsQueryParam
    public ResponseEntity<Page<UserDto>> getUsers(Pageable page,
                                                  @RequestParam(required = false) String usernameFilter,
                                                  @RequestParam(required = false) String firstNameFilter,
                                                  @RequestParam(required = false) String lastNameFilter,
                                                  @RequestParam(required = false) String emailFilter,
                                                  @RequestParam(required = false) Boolean enabledFilter,
                                                  @RequestParam(required = false) Boolean fullAdminFilter) {
        var result = this.userService.getUsers(page, usernameFilter, firstNameFilter, lastNameFilter, emailFilter, enabledFilter, fullAdminFilter);
        return ResponseEntity.ok(result);
    }

    /**
     * Load user details.
     *
     * @param id The user identifier for which to load the user details.
     * @return The user details or a 404 error.
     */
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User details"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "User does not exist", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<UserDto> getUser(@PathVariable long id) {
        var dto = this.userService.getUser(id);
        return dto
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * Creates a new user.
     *
     * @param userDto The user data.
     * @return The details of the created user.
     */
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created"),
        @ApiResponse(responseCode = "400", description = "Validation of user data failed.", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "Organizational unit not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody ModifyUserDto userDto) {
        var user = this.userService.create(userDto);
        return ResponseEntity
            .created(URI.create("/api/user/" + user.getId()))
            .body(new UserDto(user));
    }

    /**
     * Updates an existing user.
     *
     * @param id               The identifier of the user to update.
     * @param userDto          The new user data.
     * @param concurrencyToken The concurrency token.
     * @return No content
     */
    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User updated"),
        @ApiResponse(responseCode = "400", description = "Validation of user data failed.", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "Organizational unit or user not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "409", description = "User has been modified by someone else", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<Void> updateUser(@PathVariable long id,
                                           @Valid @RequestBody ModifyUserDto userDto,
                                           @RequestHeader(value = "If-Unmodified-Since", required = false) Instant concurrencyToken) {
        this.userService.update(id, userDto, concurrencyToken);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates the password an existing user.
     *
     * @param id               The identifier of the user to update.
     * @param userDto          The new user data.
     * @param concurrencyToken The concurrency token.
     * @return No content
     */
    @PutMapping(value = "/{id}/password", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User password updated"),
        @ApiResponse(responseCode = "400", description = "Validation of password data failed.", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "409", description = "User has been modified by someone else", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<Void> updateUserPassword(@PathVariable long id,
                                                   @Valid @RequestBody ModifyUserPasswordDto userDto,
                                                   @RequestHeader(value = "If-Unmodified-Since", required = false) Instant concurrencyToken) {
        this.userService.setPassword(id, userDto, concurrencyToken);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes the user with the specified identifier.
     *
     * @param id The user identifier.
     * @return No content
     */
    @DeleteMapping("/{id}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted or user does not exist"),
        @ApiResponse(responseCode = "400", description = "A user is not allowed to delete itself."),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Operation not allowed", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<Void> deleteUser(@PathVariable long id) {
        this.userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
