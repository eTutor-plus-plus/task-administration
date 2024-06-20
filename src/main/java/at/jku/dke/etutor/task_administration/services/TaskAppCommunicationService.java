package at.jku.dke.etutor.task_administration.services;

import at.jku.dke.etutor.task_administration.data.entities.TaskApp;
import at.jku.dke.etutor.task_administration.data.repositories.TaskAppRepository;
import at.jku.dke.etutor.task_administration.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Streams;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.function.TriFunction;
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
import java.io.Serializable;
import java.net.ConnectException;
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
            var requestBuilder = this.prepareHttpRequest(taskGroupType, "api/taskGroup@@TASKGROUP@@/" + taskGroupId);
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
                    LOG.error("Request for additional data of task group {} failed with status code {}.", taskGroupId, response.statusCode());
                    throwExceptionIfBodyContainsMessage(response, "Request for additional data failed");
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Request for additional data failed.");
                }
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not build URL to request additional data of task group {}", taskGroupId, ex);
        } catch (ConnectException ex) {
            LOG.error("Could not connect to task app {}", taskGroupType, ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        } catch (IOException | InterruptedException ex) {
            LOG.error("Request for additional data of task group {} failed.", taskGroupId, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Request for additional data failed.", ex);
        }
        return null;
    }

    /**
     * Creates a new task group.
     *
     * @param id   The identifier of the task group.
     * @param data The data for the task group.
     * @return The response data received from the task app.
     * @throws ResponseStatusException If the request failed.
     */
    public TaskGroupModificationResponseDto createTaskGroup(long id, ModifyTaskGroupDto data) {
        try {
            var requestBuilder = this.prepareHttpRequest(data.taskGroupType(), "api/taskGroup@@TASKGROUP@@/" + id);
            if (requestBuilder == null)
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unknown task group type.");

            LOG.info("Creating task group {} of type {}.", id, data.taskGroupType());
            String json = this.objectMapper.writeValueAsString(data);
            HttpRequest request = requestBuilder
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 201) {
                    LOG.error("Request for creating {}-task group failed with status code {} and body {}.", data.taskGroupType(), response.statusCode(), response.body());
                    throwExceptionIfBodyContainsMessage(response, "Request for creating task group failed");
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Request for creating task group failed.");
                }
                return this.objectMapper.readValue(response.body(), TaskGroupModificationResponseDto.class);
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not build URL to create new {}-task group.", data.taskGroupType(), ex);
        } catch (ConnectException ex) {
            LOG.error("Could not connect to task app {}", data.taskGroupType(), ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        } catch (IOException | InterruptedException ex) {
            LOG.error("Request for for creating {}-task group failed", data.taskGroupType(), ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Request for creating task group failed.", ex);
        }
        return null;
    }

    /**
     * Updates an existing task group.
     *
     * @param id   The identifier of the task group.
     * @param data The data for the task group.
     * @return The response data received from the task app.
     * @throws ResponseStatusException If the request failed.
     */
    public TaskGroupModificationResponseDto updateTaskGroup(long id, ModifyTaskGroupDto data) {
        try {
            var requestBuilder = this.prepareHttpRequest(data.taskGroupType(), "api/taskGroup@@TASKGROUP@@/" + id);
            if (requestBuilder == null)
                return null;

            LOG.info("Updating task group {} of type {}.", id, data.taskGroupType());
            String json = this.objectMapper.writeValueAsString(data);
            HttpRequest request = requestBuilder
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200)
                    return this.objectMapper.readValue(response.body(), TaskGroupModificationResponseDto.class);
                if (response.statusCode() == 204)
                    return null;

                LOG.error("Request for updating task group {} failed with status code {} and body {}.", id, response.statusCode(), response.body());
                throwExceptionIfBodyContainsMessage(response, "Request for updating task group failed");
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Request for updating task group failed.");
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not build URL to update existing task group {}", id, ex);
        } catch (ConnectException ex) {
            LOG.error("Could not connect to task app {}", data.taskGroupType(), ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        } catch (IOException | InterruptedException ex) {
            LOG.error("Request for for updating task group {} failed", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Request for updating task group failed.", ex);
        }
        return null;
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
            var requestBuilder = this.prepareHttpRequest(taskGroupType, "api/taskGroup@@TASKGROUP@@/" + id);
            if (requestBuilder == null)
                return;

            LOG.info("Deleting task group {} of type {}.", id, taskGroupType);
            HttpRequest request = requestBuilder
                .DELETE()
                .build();
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 204) {
                    LOG.error("Request for deleting task group {} failed with status code {} and body {}.", id, response.statusCode(), response.body());
                    throwExceptionIfBodyContainsMessage(response, "Request for deleting task group failed");
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Request for deleting task group failed.");
                }
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not build URL to delete existing task group {}", id, ex);
        } catch (ConnectException ex) {
            LOG.error("Could not connect to task app {}", taskGroupType, ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        } catch (IOException | InterruptedException ex) {
            LOG.error("Request for for deleting task group {} failed", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Request for deleting task group failed.", ex);
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
            var requestBuilder = this.prepareHttpRequest(taskType, "api/task@@TASK@@/" + taskId);
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
                    LOG.error("Request for additional data of task {} failed with status code {}.", taskId, response.statusCode());
                    throwExceptionIfBodyContainsMessage(response, "Request for additional data failed");
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Request for additional data failed.");
                }
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not build URL to request additional data for task {}", taskId, ex);
        } catch (ConnectException ex) {
            LOG.error("Could not connect to task app {}", taskType, ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        } catch (IOException | InterruptedException ex) {
            LOG.error("Request for additional data of task {} failed", taskId, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Request for additional data failed.", ex);
        }
        return null;
    }

    /**
     * Creates a new task.
     *
     * @param id   The identifier of the task.
     * @param data The data for the task.
     * @return The response data received from the task app.
     * @throws ResponseStatusException If the request failed.
     */
    public TaskModificationResponseDto createTask(long id, ModifyTaskDto data) {
        try {
            var requestBuilder = this.prepareHttpRequest(data.taskType(), "api/task@@TASK@@/" + id);
            if (requestBuilder == null)
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unknown task type.");

            LOG.info("Creating task {} of type {}.", id, data.taskType());
            String json = this.objectMapper.writeValueAsString(data);
            HttpRequest request = requestBuilder
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 201) {
                    LOG.error("Request for creating {}-task failed with status code {} and body {}", data.taskType(), response.statusCode(), response.body());
                    throwExceptionIfBodyContainsMessage(response, "Request for creating task failed");
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Request for creating task failed.");
                }
                return this.objectMapper.readValue(response.body(), TaskModificationResponseDto.class);
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not build URL to create new {}-task", data.taskType(), ex);
        } catch (ConnectException ex) {
            LOG.error("Could not connect to task app {}", data.taskType(), ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        } catch (IOException | InterruptedException ex) {
            LOG.error("Request for for creating {}-task failed", data.taskType(), ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Request for creating task failed.", ex);
        }
        return null;
    }

    /**
     * Updates an existing task.
     *
     * @param id   The identifier of the task.
     * @param data The data for the task.
     * @return The response data received from the task app.
     * @throws ResponseStatusException If the request failed.
     */
    public TaskModificationResponseDto updateTask(long id, ModifyTaskDto data) {
        try {
            var requestBuilder = this.prepareHttpRequest(data.taskType(), "api/task@@TASK@@/" + id);
            if (requestBuilder == null)
                return null;

            LOG.info("Updating task {} of type {}.", id, data.taskType());
            String json = this.objectMapper.writeValueAsString(data);
            HttpRequest request = requestBuilder
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200)
                    return this.objectMapper.readValue(response.body(), TaskModificationResponseDto.class);
                if (response.statusCode() == 204)
                    return null;

                LOG.error("Request for updating task {} failed with status code {} and body {}", id, response.statusCode(), response.body());
                throwExceptionIfBodyContainsMessage(response, "Request for updating task failed");
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Request for updating task failed.");
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not build URL to update existing task {}", id, ex);
        } catch (ConnectException ex) {
            LOG.error("Could not connect to task app {}", data.taskType(), ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        } catch (IOException | InterruptedException ex) {
            LOG.error("Request for for updating task {} failed", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Request for updating task failed.", ex);
        }
        return null;
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
            var requestBuilder = this.prepareHttpRequest(taskType, "api/task@@TASK@@/" + id);
            if (requestBuilder == null)
                return;

            LOG.info("Deleting task {} of type {}.", id, taskType);
            HttpRequest request = requestBuilder
                .DELETE()
                .build();
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 204) {
                    LOG.error("Request for deleting task {} failed with status code {} and body {}.", id, response.statusCode(), response.body());
                    throwExceptionIfBodyContainsMessage(response, "Request for deleting task failed");
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Request for deleting task failed.");
                }
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not build URL to delete existing task {}", id, ex);
        } catch (ConnectException ex) {
            LOG.error("Could not connect to task app {}", taskType, ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        } catch (IOException | InterruptedException ex) {
            LOG.error("Request for for deleting task {} failed", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Request for deleting task failed.", ex);
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
     * @param taskType    The task type.
     * @param request     The request.
     * @param requestPath The path at the task app.
     * @param secured     Whether the request should be authenticated.
     * @return The response from the task app.
     * @throws ResponseStatusException If the request failed.
     */
    public ResponseEntity<?> forwardRequest(String taskType, String requestPath, HttpServletRequest request, boolean secured) {
        try {
            HttpRequest.Builder requestBuilder = createRequestBuilder(taskType, requestPath, request, secured);
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
            LOG.error("Could not build URL to forward request {}", requestPath, ex);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (ConnectException ex) {
            LOG.error("Could not connect to task app {}", taskType, ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        } catch (IOException | InterruptedException ex) {
            LOG.error("Could not forward request {}", requestPath, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private HttpRequest.Builder createRequestBuilder(String taskType, String requestPath, HttpServletRequest request, boolean secured) throws URISyntaxException {
        // build query string
        var query = request.getQueryString();

        // get request builder
        String path = requestPath.startsWith("/") ? requestPath.substring(1) : requestPath;
        if (query != null && !query.isBlank())
            path += '?' + query;

        // don't forward requests to default endpoints
        if (Pattern.matches("^api/submission(\\?.*)?", path.toLowerCase()) ||
            Pattern.matches("^api/submission/[a-z0-9-]+/result(\\?.*)?", path.toLowerCase()) ||
            Pattern.matches("^api/(task|taskgroup)/[0-9]+(\\?.*)?", path.toLowerCase()) ||
            Pattern.matches("^api/(task|taskgroup)/.+/[0-9]+(\\?.*)?", path.toLowerCase()))
            return null;

        return this.prepareHttpRequest(taskType, path, secured);
    }

    private static void prepareRequest(HttpServletRequest request, HttpRequest.Builder requestBuilder) throws IOException {
        // add headers
        Streams.stream(request.getHeaderNames().asIterator())
            .filter(h -> h.equalsIgnoreCase("accept") ||
                h.equalsIgnoreCase("accept-language") ||
                h.equalsIgnoreCase("accept-encoding") ||
                h.equalsIgnoreCase("content-type") ||
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
     * Submits a submission for testing purposes.
     *
     * @param taskType   The task type.
     * @param submission The submission data.
     * @return The submission result.
     */
    public Serializable submit(String taskType, SubmitSubmissionDto submission) {
        try {
            var requestBuilder = this.prepareHttpRequest(taskType, "api/submission@@SUBMISSION@@?persist=false&runInBackground=false");
            if (requestBuilder == null)
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);

            LOG.info("Submitting task {} of type {}", submission.taskId(), taskType);
            String json = this.objectMapper.writeValueAsString(submission);
            HttpRequest request = requestBuilder
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.body();
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not build URL to submit task {}", submission.taskId(), ex);
        } catch (ConnectException ex) {
            LOG.error("Could not connect to task app {}", taskType, ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        } catch (IOException | InterruptedException ex) {
            LOG.error("Request for for submitting task {} failed", submission.taskId(), ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Request for submitting task failed.", ex);
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Prepares an HTTP request for the specified task group type.
     *
     * @param taskGroupType The task group type.
     * @param path          The path to append to the URL.
     * @return The HTTP request builder or {@code null} if no task app was found.
     * @throws URISyntaxException If the URL is invalid.
     */
    private HttpRequest.Builder prepareHttpRequest(String taskGroupType, String path) throws URISyntaxException {
        return this.prepareHttpRequest(taskGroupType, path, true);
    }

    /**
     * Prepares an HTTP request for the specified task group type.
     * <p>
     * Following placeholders may be used in the path:
     * <ul>
     *     <li>{@code @@TASK@@}: Prefix for task</li>
     *     <li>{@code @@TASKGROUP@@}: Prefix for task group</li>
     *     <li>{@code @@SUBMISSION@@}: Prefix for submission</li>
     * </ul>
     * This method will replace the placeholders with the actual values of the task app if configured (and prefixes it with a "/").
     * E.g. if the path is "api/task@@TASK@@/1", the resulting path will be "api/task/1" if no prefix is configured and
     * "api/task/dlg/1" if the prefix "dlg" is configured.
     *
     * @param taskGroupType   The task group type.
     * @param path            The path to append to the URL.
     * @param addApiKeyHeader Whether to add the API key header.
     * @return The HTTP request builder or {@code null} if no task app was found.
     * @throws URISyntaxException If the URL is invalid.
     */
    private HttpRequest.Builder prepareHttpRequest(String taskGroupType, String path, boolean addApiKeyHeader) throws URISyntaxException {
        var app = this.getTaskApp(taskGroupType);
        if (app == null)
            return null;

        // Replace placeholder
        TriFunction<String, String, String, String> replaceFunc = (p, f, s) -> {
            if (p.contains(s)) {
                if (f != null && !f.isBlank())
                    p = p.replace(s, '/' + f);
                else
                    p = p.replace(s, "");
            }
            return p;
        };
        if (path != null) {
            path = replaceFunc.apply(path, app.getTaskPrefix(), "@@TASK@@");
            path = replaceFunc.apply(path, app.getTaskGroupPrefix(), "@@TASKGROUP@@");
            path = replaceFunc.apply(path, app.getSubmissionPrefix(), "@@SUBMISSION@@");
        }

        // Normalize URL
        String url = app.getUrl();
        if (path != null) {
            if (!url.endsWith("/"))
                url += '/';
            url += path;
        }

        // Build request
        var builder = HttpRequest.newBuilder();
        if (app.getApiKey() != null && addApiKeyHeader)
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

    /**
     * Throws an exception if the response body contains a message.
     *
     * @param response      The response.
     * @param messagePrefix The message prefix.
     * @throws ResponseStatusException If the response body contains a message.
     */
    private void throwExceptionIfBodyContainsMessage(HttpResponse<String> response, String messagePrefix) throws ResponseStatusException {
        try {
            Map<String, Object> body = this.objectMapper.readValue(response.body(), new TypeReference<>() {
            });
            if (body.containsKey("message")) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, messagePrefix + ": " + body.get("message"));
            }
            if (body.containsKey("detail")) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, messagePrefix + ": " + body.get("detail"));
            }
        } catch (JsonProcessingException ignored) {
        }
    }
}
