package at.jku.dke.etutor.task_administration.controllers;

import at.jku.dke.etutor.task_administration.services.TaskAppCommunicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller that forwards requests to the corresponding task app.
 */
@RestController
@RequestMapping("/api/forward/{taskType}")
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
    @RequestMapping(value = "{*path}", method = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<?> forwardGet(@PathVariable String taskType, @PathVariable String path, HttpServletRequest request) {
        return this.service.forwardRequest(taskType, path, request);
    }

}
