package at.jku.dke.etutor.task_administration.services;

import at.jku.dke.etutor.task_administration.data.entities.TaskApp;
import at.jku.dke.etutor.task_administration.data.repositories.TaskAppRepository;
import at.jku.dke.etutor.task_administration.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_administration.dto.ModifyTaskGroupDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

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
        LOG.info("Requesting additional data for task group {} of type {}.", taskGroupId, taskGroupType);
        try {
            var requestBuilder = this.prepareHttpRequest(taskGroupType, "api/taskGroup/" + taskGroupId);
            if (requestBuilder == null)
                return null;

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
        LOG.info("Creating task group {} of type {}.", id, data.taskGroupType());
        try {
            var requestBuilder = this.prepareHttpRequest(data.taskGroupType(), "api/taskGroup/" + id);
            if (requestBuilder == null)
                return;

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
        LOG.info("Updating task group {} of type {}.", id, data.taskGroupType());
        try {
            var requestBuilder = this.prepareHttpRequest(data.taskGroupType(), "api/taskGroup/" + id);
            if (requestBuilder == null)
                return;

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
        LOG.info("Deleting task group {} of type {}.", id, taskGroupType);
        try {
            var requestBuilder = this.prepareHttpRequest(taskGroupType, "api/taskGroup/" + id);
            if (requestBuilder == null)
                return;

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
        LOG.info("Requesting additional data for task {} of type {}.", taskId, taskType);
        try {
            var requestBuilder = this.prepareHttpRequest(taskType, "api/task/" + taskId);
            if (requestBuilder == null)
                return null;

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
        LOG.info("Creating task {} of type {}.", id, data.taskType());
        try {
            var requestBuilder = this.prepareHttpRequest(data.taskType(), "api/task/" + id);
            if (requestBuilder == null)
                return;

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
        LOG.info("Updating task {} of type {}.", id, data.taskType());
        try {
            var requestBuilder = this.prepareHttpRequest(data.taskType(), "api/task/" + id);
            if (requestBuilder == null)
                return;

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
        LOG.info("Deleting task {} of type {}.", id, taskType);
        try {
            var requestBuilder = this.prepareHttpRequest(taskType, "api/task/" + id);
            if (requestBuilder == null)
                return;

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
