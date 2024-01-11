package at.jku.dke.etutor.task_administration.services;

import at.jku.dke.etutor.task_administration.data.entities.TaskApp;
import at.jku.dke.etutor.task_administration.data.repositories.TaskAppRepository;
import at.jku.dke.etutor.task_administration.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_administration.dto.ModifyTaskGroupDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Streams;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service for communication with the task apps.
 */
@Service
public class TaskAppCommunicationService {
    private static final Logger LOG = LoggerFactory.getLogger(TaskAppCommunicationService.class);
    private final TaskAppRepository taskAppRepository;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new instance of class {@link TaskAppCommunicationService}.
     *
     * @param taskAppRepository The repository for the task apps.
     * @param objectMapper      The JSON object mapper.
     */
    public TaskAppCommunicationService(TaskAppRepository taskAppRepository, ObjectMapper objectMapper) {
        this.taskAppRepository = taskAppRepository;
        this.objectMapper = objectMapper;
    }

    //#region --- TaskGroup ---

    /**
     * Returns the additional data for the specified task group.
     *
     * @param taskGroupId   The task group identifier.
     * @param taskGroupType The task group type.
     * @return The additional data or {@code null} if no task app was found.
     * @throws ResponseStatusException If the request failed.
     */
    public Map<String, Object> getTaskGroupAdditionalData(long taskGroupId, String taskGroupType) {
        try {
            var requestBuilder = this.prepareHttpRequest(taskGroupType, "api/taskGroup/" + taskGroupId);
            if (requestBuilder == null)
                return null;

            LOG.info("Requesting additional data for task group {} of type {}.", taskGroupId, taskGroupType);
            HttpRequest request = requestBuilder.GET().build();
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    //noinspection unchecked
                    return this.objectMapper.readValue(response.body(), Map.class);
                } else {
                    LOG.error("Request for additional data failed with status code {}.", response.statusCode());
                    throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Request for additional data failed.");
                }
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not build URL to request additional data.", ex);
        } catch (IOException | InterruptedException ex) {
            LOG.error("Request for additional data failed.", ex);
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Request for additional data failed.", ex);
        }
        return null;
    }

    /**
     * Creates a new task group.
     *
     * @param id   The identifier of the task group.
     * @param data The data for the task group.
     * @throws ResponseStatusException If the request failed.
     */
    public void createTaskGroup(long id, ModifyTaskGroupDto data) {
        try {
            var requestBuilder = this.prepareHttpRequest(data.taskGroupType(), "api/taskGroup/" + id);
            if (requestBuilder == null)
                return;

            LOG.info("Creating task group {} of type {}.", id, data.taskGroupType());
            String json = this.objectMapper.writeValueAsString(data);
            HttpRequest request = requestBuilder
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 201) {
                    LOG.error("Request for creating task group failed with status code {} and body {}.", response.statusCode(), response.body());
                    throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Request for creating task group failed.");
                }
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not build URL to create new task group.", ex);
        } catch (IOException | InterruptedException ex) {
            LOG.error("Request for for creating task group failed.", ex);
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Request for creating task group failed.", ex);
        }
    }

    /**
     * Updates an existing task group.
     *
     * @param id   The identifier of the task group.
     * @param data The data for the task group.
     * @throws ResponseStatusException If the request failed.
     */
    public void updateTaskGroup(long id, ModifyTaskGroupDto data) {
        try {
            var requestBuilder = this.prepareHttpRequest(data.taskGroupType(), "api/taskGroup/" + id);
            if (requestBuilder == null)
                return;

            LOG.info("Updating task group {} of type {}.", id, data.taskGroupType());
            String json = this.objectMapper.writeValueAsString(data);
            HttpRequest request = requestBuilder
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 204) {
                    LOG.error("Request for updating task group failed with status code {} and body {}.", response.statusCode(), response.body());
                    throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Request for updating task group failed.");
                }
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not build URL to update existing task group.", ex);
        } catch (IOException | InterruptedException ex) {
            LOG.error("Request for for updating task group failed.", ex);
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Request for updating task group failed.", ex);
        }
    }

    /**
     * Deletes an existing task group.
     *
     * @param id            The identifier of the task group.
     * @param taskGroupType The task group type.
     * @throws ResponseStatusException If the request failed.
     */
    public void deleteTaskGroup(long id, String taskGroupType) {
        try {
            var requestBuilder = this.prepareHttpRequest(taskGroupType, "api/taskGroup/" + id);
            if (requestBuilder == null)
                return;

            LOG.info("Deleting task group {} of type {}.", id, taskGroupType);
            HttpRequest request = requestBuilder
                .DELETE()
                .build();
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 204) {
                    LOG.error("Request for deleting task group failed with status code {} and body {}.", response.statusCode(), response.body());
                    throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Request for deleting task group failed.");
                }
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not build URL to delete existing task group.", ex);
        } catch (IOException | InterruptedException ex) {
            LOG.error("Request for for deleting task group failed.", ex);
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Request for deleting task group failed.", ex);
        }
    }

    //#endregion

    //#region --- Task ---

    /**
     * Returns the additional data for the specified task.
     *
     * @param taskId   The task identifier.
     * @param taskType The task type.
     * @return The additional data or {@code null} if no task app was found.
     * @throws ResponseStatusException If the request failed.
     */
    public Map<String, Object> getTaskAdditionalData(long taskId, String taskType) {
        try {
            var requestBuilder = this.prepareHttpRequest(taskType, "api/task/" + taskId);
            if (requestBuilder == null)
                return null;

            LOG.info("Requesting additional data for task {} of type {}.", taskId, taskType);
            HttpRequest request = requestBuilder.GET().build();
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    //noinspection unchecked
                    return this.objectMapper.readValue(response.body(), Map.class);
                } else {
                    LOG.error("Request for additional data failed with status code {}.", response.statusCode());
                    throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Request for additional data failed.");
                }
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not build URL to request additional data.", ex);
        } catch (IOException | InterruptedException ex) {
            LOG.error("Request for additional data failed.", ex);
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Request for additional data failed.", ex);
        }
        return null;
    }

    /**
     * Creates a new task.
     *
     * @param id   The identifier of the task.
     * @param data The data for the task.
     * @throws ResponseStatusException If the request failed.
     */
    public void createTask(long id, ModifyTaskDto data) {
        try {
            var requestBuilder = this.prepareHttpRequest(data.taskType(), "api/task/" + id);
            if (requestBuilder == null)
                return;

            LOG.info("Creating task {} of type {}.", id, data.taskType());
            String json = this.objectMapper.writeValueAsString(data);
            HttpRequest request = requestBuilder
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 201) {
                    LOG.error("Request for creating task failed with status code {} and body {}.", response.statusCode(), response.body());
                    throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Request for creating task failed.");
                }
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not build URL to create new task.", ex);
        } catch (IOException | InterruptedException ex) {
            LOG.error("Request for for creating task failed.", ex);
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Request for creating task failed.", ex);
        }
    }

    /**
     * Updates an existing task.
     *
     * @param id   The identifier of the task.
     * @param data The data for the task.
     * @throws ResponseStatusException If the request failed.
     */
    public void updateTask(long id, ModifyTaskDto data) {
        try {
            var requestBuilder = this.prepareHttpRequest(data.taskType(), "api/task/" + id);
            if (requestBuilder == null)
                return;

            LOG.info("Updating task {} of type {}.", id, data.taskType());
            String json = this.objectMapper.writeValueAsString(data);
            HttpRequest request = requestBuilder
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 204) {
                    LOG.error("Request for updating task failed with status code {} and body {}.", response.statusCode(), response.body());
                    throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Request for updating task failed.");
                }
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not build URL to update existing task.", ex);
        } catch (IOException | InterruptedException ex) {
            LOG.error("Request for for updating task failed.", ex);
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Request for updating task failed.", ex);
        }
    }

    /**
     * Deletes an existing task.
     *
     * @param id       The identifier of the task.
     * @param taskType The task type.
     * @throws ResponseStatusException If the request failed.
     */
    public void deleteTask(long id, String taskType) {
        try {
            var requestBuilder = this.prepareHttpRequest(taskType, "api/task/" + id);
            if (requestBuilder == null)
                return;

            LOG.info("Deleting task {} of type {}.", id, taskType);
            HttpRequest request = requestBuilder
                .DELETE()
                .build();
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 204) {
                    LOG.error("Request for deleting task failed with status code {} and body {}.", response.statusCode(), response.body());
                    throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Request for deleting task failed.");
                }
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not build URL to delete existing task.", ex);
        } catch (IOException | InterruptedException ex) {
            LOG.error("Request for for deleting task failed.", ex);
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Request for deleting task failed.", ex);
        }
    }

    //#endregion

    //#region --- Forward ---

    /**
     * Forwards the request to the task app and returns its response.
     * <p>
     * Supports the methods: {@code GET, POST, PUT, DELETE}
     * Supported request-headers: {@code accept, content-type, accept-encoding, accept-language, X-*}
     * Supported response-headers: {@code content-type, content-language, content-disposition, X-}
     *
     * @param taskType The task type.
     * @param request  The request.
     * @return The response from the task app.
     * @throws ResponseStatusException If the request failed.
     */
    public ResponseEntity<?> forwardRequest(String taskType, String requestPath, HttpServletRequest request) {
        try {
            HttpRequest.Builder requestBuilder = createRequestBuilder(taskType, requestPath, request);
            if (requestBuilder == null)
                return ResponseEntity.notFound().build();

            LOG.info("Forwarding request {} for type {}.", requestPath, taskType);
            prepareRequest(request, requestBuilder);

            // send request
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpRequest requestToSend = requestBuilder.build();
                LOG.debug("Sending {}-request to {}", requestToSend.method(), requestToSend.uri());

                HttpResponse<byte[]> response = client.send(requestToSend, HttpResponse.BodyHandlers.ofByteArray());
                return buildResponse(response);
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not build URL to forward request.", ex);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IOException | InterruptedException ex) {
            LOG.error("Could not forward request.", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private HttpRequest.Builder createRequestBuilder(String taskType, String requestPath, HttpServletRequest request) throws URISyntaxException {
        // build query string
        var query = request.getQueryString();

        // get request builder
        String path = requestPath.startsWith("/") ? requestPath.substring(1) : requestPath;
        if (query != null && !query.isBlank())
            path += '?' + query;

        // don't forward requests to default endpoints
        if (Pattern.matches("^api/submission(\\?.*)?", path.toLowerCase()) ||
            Pattern.matches("^api/submission/[a-z0-9-]+/result(\\?.*)?", path.toLowerCase()) ||
            Pattern.matches("^api/(task|taskgroup)/[0-9]+(\\?.*)?", path.toLowerCase()))
            return null;

        return this.prepareHttpRequest(taskType, path);
    }

    private static void prepareRequest(HttpServletRequest request, HttpRequest.Builder requestBuilder) throws IOException {
        // add headers
        Streams.stream(request.getHeaderNames().asIterator())
            .filter(h -> h.equalsIgnoreCase("accept") ||
                h.equalsIgnoreCase("accept-language") ||
                h.equalsIgnoreCase("accept-encoding") ||
                h.toLowerCase().startsWith("x-"))
            .forEach(h -> requestBuilder.header(h, request.getHeader(h)));

        // set method and body
        switch (request.getMethod()) {
            case "GET":
                requestBuilder.GET();
                break;
            case "POST":
                byte[] body;
                try (InputStream is = request.getInputStream()) {
                    body = IOUtils.toByteArray(is);
                }
                requestBuilder.POST(HttpRequest.BodyPublishers.ofByteArray(body));
                break;
            case "PUT":
                byte[] putBody;
                try (InputStream is = request.getInputStream()) {
                    putBody = IOUtils.toByteArray(is);
                }
                requestBuilder.PUT(HttpRequest.BodyPublishers.ofByteArray(putBody));
                break;
            case "DELETE":
                requestBuilder.DELETE();
                break;
        }
    }

    private static ResponseEntity<InputStreamResource> buildResponse(HttpResponse<byte[]> response) {
        byte[] body = response.body();

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.statusCode());
        response.headers().map().entrySet().stream()
            .filter(h -> h.getKey().equalsIgnoreCase("content-type")
                || h.getKey().equalsIgnoreCase("content-language")
                || h.getKey().equalsIgnoreCase("content-disposition")
                || h.getKey().toLowerCase().startsWith("x-"))
            .forEach(h -> h.getValue().forEach(v -> responseBuilder.header(h.getKey(), v)));
        return responseBuilder.body(new InputStreamResource(new ByteArrayInputStream(body)));
    }

    //#endregion

    /**
     * Prepares an HTTP request for the specified task group type.
     *
     * @param taskGroupType The task group type.
     * @param path          The path to append to the URL.
     * @return The HTTP request builder or {@code null} if no task app was found.
     * @throws URISyntaxException If the URL is invalid.
     */
    private HttpRequest.Builder prepareHttpRequest(String taskGroupType, String path) throws URISyntaxException {
        var app = this.getTaskApp(taskGroupType);
        if (app == null)
            return null;

        String url = app.getUrl();
        if (path != null) {
            if (!url.endsWith("/"))
                url += '/';
            url += path;
        }

        var builder = HttpRequest.newBuilder();
        if (app.getApiKey() != null)
            builder = builder.header("X-API-KEY", app.getApiKey());

        return builder
            .header("Accept", "application/json")
            .timeout(Duration.ofSeconds(20))
            .uri(new URI(url));
    }

    /**
     * Returns the task app for the specified task group type.
     *
     * @param taskGroupType The task group type.
     * @return The task app or {@code null} if no task app was found.
     */
    private TaskApp getTaskApp(String taskGroupType) {
        return this.taskAppRepository.findByTaskType(taskGroupType).orElse(null);
    }

}
