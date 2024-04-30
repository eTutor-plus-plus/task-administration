package at.jku.dke.etutor.task_administration.services;

import at.jku.dke.etutor.task_administration.data.entities.TaskApp;
import at.jku.dke.etutor.task_administration.data.repositories.TaskAppRepository;
import at.jku.dke.etutor.task_administration.dto.ModifyTaskAppDto;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskAppServiceTest {

    @Test
    void getTaskApps() {
        // Arrange
        var repo = mock(TaskAppRepository.class);
        var service = new TaskAppService(repo);
        when(repo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        // Act
        service.getTaskApps(Pageable.ofSize(3), "/sql", "sql");

        // Assert
        verify(repo).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getTaskApp() {
        // Arrange
        var repo = mock(TaskAppRepository.class);
        var service = new TaskAppService(repo);
        var id = 4L;
        when(repo.findById(id)).thenReturn(Optional.of(new TaskApp()));

        // Act
        var result = service.getTaskApp(id);

        // Assert
        assertThat(result).isPresent();
    }

    @Test
    void create() {
        // Arrange
        var repo = mock(TaskAppRepository.class);
        var service = new TaskAppService(repo);
        var dto = new ModifyTaskAppDto("sql", "http://localhost", "my-key", "tp", "tgp", "sp");
        when(repo.save(any(TaskApp.class))).thenAnswer(x -> x.getArgument(0));

        // Act
        var result = service.create(dto);

        // Assert
        assertEquals(dto.taskType(), result.getTaskType());
        assertEquals(dto.url(), result.getUrl());
        assertEquals(dto.apiKey(), result.getApiKey());
        assertEquals(dto.taskPrefix(), result.getTaskPrefix());
        assertEquals(dto.taskGroupPrefix(), result.getTaskGroupPrefix());
        assertEquals(dto.submissionPrefix(), result.getSubmissionPrefix());
    }

    @Test
    void update() {
        // Arrange
        var repo = mock(TaskAppRepository.class);
        var service = new TaskAppService(repo);
        var dto = new ModifyTaskAppDto("sql", "http://localhost", "my-key", "tp", "tgp", "sp");
        var app = new TaskApp();
        app.setId(5L);
        when(repo.findById(app.getId())).thenReturn(Optional.of(app));

        // Act
        service.update(app.getId(), dto, null);

        // Assert
        verify(repo).save(any(TaskApp.class));
        assertEquals(dto.taskType(), app.getTaskType());
        assertEquals(dto.url(), app.getUrl());
        assertEquals(dto.apiKey(), app.getApiKey());
        assertEquals(dto.taskPrefix(), app.getTaskPrefix());
        assertEquals(dto.taskGroupPrefix(), app.getTaskGroupPrefix());
        assertEquals(dto.submissionPrefix(), app.getSubmissionPrefix());
    }

    @Test
    void update_concurrencyProblem() {
        // Arrange
        var repo = mock(TaskAppRepository.class);
        var service = new TaskAppService(repo);
        var dto = new ModifyTaskAppDto("sql", "http://localhost", "my-key", "tp", "tgp", "sp");
        var app = new TaskApp();
        app.setId(5L);
        app.setLastModifiedDate(Instant.MAX);
        when(repo.findById(app.getId())).thenReturn(Optional.of(app));

        // Act & Assert
        assertThrows(ConcurrencyFailureException.class, () -> service.update(app.getId(), dto, Instant.MIN));
    }

    @Test
    void update_notFound() {
        // Arrange
        var repo = mock(TaskAppRepository.class);
        var service = new TaskAppService(repo);
        var dto = new ModifyTaskAppDto("sql", "http://localhost", "my-key", "tp", "tgp", "sp");
        when(repo.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.update(1L, dto, Instant.MIN));
    }

    @Test
    void delete() {
        // Arrange
        var repo = mock(TaskAppRepository.class);
        var service = new TaskAppService(repo);

        // Act
        service.delete(3L);

        // Assert
        verify(repo).deleteById(3L);
    }
}
