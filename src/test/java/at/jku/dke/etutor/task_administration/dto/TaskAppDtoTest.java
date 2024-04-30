package at.jku.dke.etutor.task_administration.dto;

import at.jku.dke.etutor.task_administration.data.entities.TaskApp;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskAppDtoTest {
    @Test
    void constructor() {
        // Arrange
        var taskApp = new TaskApp();
        taskApp.setId(1L);
        taskApp.setTaskType("sql");
        taskApp.setUrl("http://localhost:8080");
        taskApp.setApiKey("my-key");
        taskApp.setTaskPrefix("tpref");
        taskApp.setTaskGroupPrefix("gpref");
        taskApp.setSubmissionPrefix("spref");
        taskApp.setCreatedBy("creator");
        taskApp.setCreatedDate(Instant.now().minusSeconds(60));
        taskApp.setLastModifiedBy("modifier");
        taskApp.setLastModifiedDate(Instant.now());

        // Act
        var dto = new TaskAppDto(taskApp);

        // Assert
        assertEquals(taskApp.getId(), dto.id());
        assertEquals(taskApp.getTaskType(), dto.taskType());
        assertEquals(taskApp.getUrl(), dto.url());
        assertEquals(taskApp.getApiKey(), dto.apiKey());
        assertEquals(taskApp.getTaskPrefix(), dto.taskPrefix());
        assertEquals(taskApp.getTaskGroupPrefix(), dto.taskGroupPrefix());
        assertEquals(taskApp.getSubmissionPrefix(), dto.submissionPrefix());
        assertEquals(taskApp.getCreatedBy(), dto.createdBy());
        assertEquals(taskApp.getCreatedDate(), dto.createdDate());
        assertEquals(taskApp.getLastModifiedBy(), dto.lastModifiedBy());
        assertEquals(taskApp.getLastModifiedDate(), dto.lastModifiedDate());
    }
}
