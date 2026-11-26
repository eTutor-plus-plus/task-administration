package at.jku.dke.etutor.task_administration.services;

import at.jku.dke.etutor.task_administration.SpringTaskAdministrationTest;
import at.jku.dke.etutor.task_administration.auth.AuthConstants;
import at.jku.dke.etutor.task_administration.moodle.QuestionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Integration tests for {@link TaskService#delete(long)}.
 */
@SpringTaskAdministrationTest
class TaskServiceDeleteTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private JdbcTemplate jdbc;

    @MockBean
    private TaskAppCommunicationService taskAppCommunicationService;

    @MockBean
    private QuestionService questionService;

    private final List<Long> createdOrganizationalUnits = new ArrayList<>();

    @BeforeEach
    void setUp() {
        var jwt = Jwt.withTokenValue("test")
            .header("alg", "none")
            .subject("1")
            .claim(AuthConstants.CLAIM_FULL_ADMIN, true)
            .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        for (Long ouId : this.createdOrganizationalUnits) {
            this.jdbc.update("DELETE FROM tasks WHERE ou_id = ?", ouId);
            this.jdbc.update("DELETE FROM task_categories WHERE ou_id = ?", ouId);
            this.jdbc.update("DELETE FROM organizational_units WHERE id = ?", ouId);
        }
        this.createdOrganizationalUnits.clear();
    }

    @Test
    void delete_approvedTaskWithMoodleIds_deletesTask() {
        // Arrange
        long taskId = this.createTask(true);

        // Act
        this.taskService.delete(taskId);

        // Assert
        assertEquals(0, this.count("tasks", taskId));
        assertEquals(0, this.count("task_moodleids", taskId));
        verify(this.taskAppCommunicationService).deleteTask(taskId, "sql");
        verify(this.questionService).markQuestionAsDeleted(argThat(t -> t.getId() == taskId), argThat(ids -> ids.size() == 1));
    }

    @Test
    void delete_draftTaskWithoutMoodleIds_deletesTask() {
        // Arrange
        long taskId = this.createTask(false);

        // Act
        this.taskService.delete(taskId);

        // Assert
        assertEquals(0, this.count("tasks", taskId));
        verify(this.taskAppCommunicationService).deleteTask(taskId, "sql");
    }

    @Test
    void delete_taskAppFails_keepsTask() {
        // Arrange
        long taskId = this.createTask(true);
        doThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY)).when(this.taskAppCommunicationService).deleteTask(anyLong(), anyString());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> this.taskService.delete(taskId));
        assertEquals(1, this.count("tasks", taskId));
        assertEquals(1, this.count("task_moodleids", taskId));
    }

    private long createTask(boolean approved) {
        Long ouId = this.jdbc.queryForObject("INSERT INTO organizational_units (name, moodle_id) VALUES (?, 5) RETURNING id", Long.class, "OU " + UUID.randomUUID());
        this.createdOrganizationalUnits.add(ouId);
        Long categoryId = this.jdbc.queryForObject("INSERT INTO task_categories (name, ou_id) VALUES ('Category', ?) RETURNING id", Long.class, ouId);
        Long taskId = this.jdbc.queryForObject("INSERT INTO tasks (ou_id, title, description_de, description_en, task_type, status) VALUES (?, 'Task', 'de', 'en', 'sql', CAST(? AS task_status)) RETURNING id",
            Long.class, ouId, approved ? "approved" : "draft");
        this.jdbc.update("INSERT INTO tasks_task_categories (task_id, task_category_id) VALUES (?, ?)", taskId, categoryId);
        if (approved)
            this.jdbc.update("INSERT INTO task_moodleids (task_id, task_category_id, moodle_id) VALUES (?, ?, 42)", taskId, categoryId);
        return taskId;
    }

    private long count(String table, long taskId) {
        String column = table.equals("tasks") ? "id" : "task_id";
        Long result = this.jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?", Long.class, taskId);
        return result == null ? 0 : result;
    }
}
