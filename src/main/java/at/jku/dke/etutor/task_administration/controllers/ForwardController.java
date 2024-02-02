package at.jku.dke.etutor.task_administration.controllers;

import at.jku.dke.etutor.task_administration.services.TaskAppCommunicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller that forwards requests to the corresponding task app.
 */
@RestController
@Tag(name = "Forward", description = "Forwards requests to the task app")
public class ForwardController {

    private final TaskAppCommunicationService service;

    /**
     * Creates a new instance of class {@link ForwardController}.
     *
     * @param service The task app communication service.
     */
    public ForwardController(TaskAppCommunicationService service) {
        this.service = service;
    }

    /**
     * Forwards the request to the task app.
     *
     * @param taskType The task type.
     * @param path     The path at the task app.
     * @param request  The request.
     * @return The received response from the task app.
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/forward/{taskType}/{*path}", method = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<?> forward(@PathVariable String taskType, @PathVariable String path, HttpServletRequest request) {
        return this.service.forwardRequest(taskType, path, request, true);
    }

    /**
     * Forwards the request to the task app. The forwarded request will not be authenticated.
     *
     * @param taskType The task type.
     * @param path     The path at the task app.
     * @param request  The request.
     * @return The received response from the task app.
     */
    @GetMapping(value = "/api/forwardPublic/{taskType}/{*path}")
    public ResponseEntity<?> forwardPublic(@PathVariable String taskType, @PathVariable String path, HttpServletRequest request) {
        return this.service.forwardRequest(taskType, path, request, false);
    }

}
