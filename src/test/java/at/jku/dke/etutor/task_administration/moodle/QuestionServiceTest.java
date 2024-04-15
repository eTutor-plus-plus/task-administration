package at.jku.dke.etutor.task_administration.moodle;

import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnit;
import at.jku.dke.etutor.task_administration.data.entities.Task;
import at.jku.dke.etutor.task_administration.data.entities.TaskCategory;
import at.jku.dke.etutor.task_administration.data.entities.TaskGroup;
import at.jku.dke.etutor.task_administration.data.repositories.TaskCategoryRepository;
import at.jku.dke.etutor.task_administration.data.repositories.TaskGroupRepository;
import at.jku.dke.etutor.task_administration.data.repositories.TaskMoodleIdRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuestionServiceTest {

    private final TaskCategoryRepository categoryRepository = mock(TaskCategoryRepository.class);
    private final TaskMoodleIdRepository taskMoodleIdRepository = mock(TaskMoodleIdRepository.class);
    private final TaskGroupRepository taskGroupRepository = mock(TaskGroupRepository.class);
    private final MoodleConfig config = new MoodleConfig("test-token", "http://localhost", true);

    //#region --- CREATE ---
    @Test
    void createQuestionFromTask_disabled() throws ExecutionException, InterruptedException {
        // Arrange
        this.config.setEnabled(false);
        var service = new QuestionTestService(this.config, this.categoryRepository, this.taskMoodleIdRepository, this.taskGroupRepository);

        // Act
        var result = service.createQuestionFromTask(null).get();

        // Assert
        assertThat(result).isEmpty();
        assertFalse(service.postCalled);
    }

    @Test
    void createQuestionFromTask_withoutTaskCategories() throws ExecutionException, InterruptedException {
        // Arrange
        var service = new QuestionTestService(this.config, this.categoryRepository, this.taskMoodleIdRepository, this.taskGroupRepository);

        var task = new Task();

        // Act
        var result = service.createQuestionFromTask(task).get();

        // Assert
        assertThat(result).isEmpty();
        assertFalse(service.postCalled);
    }

    @Test
    void createQuestionFromTask_withoutOUMoodleId() throws ExecutionException, InterruptedException {
        // Arrange
        var service = new QuestionTestService(this.config, this.categoryRepository, this.taskMoodleIdRepository, this.taskGroupRepository);

        var task = new Task();
        task.setTaskCategories(Set.of(new TaskCategory()));
        task.setOrganizationalUnit(new OrganizationalUnit());

        // Act
        var result = service.createQuestionFromTask(task).get();

        // Assert
        assertThat(result).isEmpty();
        assertFalse(service.postCalled);
    }

    @Test
    void createQuestionFromTask_withTaskGroup() throws ExecutionException, InterruptedException {
        // Arrange
        var service = new QuestionTestService(this.config, this.categoryRepository, this.taskMoodleIdRepository, this.taskGroupRepository);

        var ou = new OrganizationalUnit();
        ou.setId(2L);
        ou.setMoodleId(10);

        var cat = new TaskCategory();
        cat.setId(1L);
        cat.setName("Category");
        cat.setOrganizationalUnit(ou);

        var cat2 = new TaskCategory();
        cat2.setId(2L);
        cat2.setName("Invalid category");

        var tg = new TaskGroup();
        tg.setId(4L);
        tg.setOrganizationalUnit(ou);
        tg.setTaskGroupType("sql");
        tg.setName("Test Task Group");
        tg.setDescriptionDe("Test Description DE");
        tg.setDescriptionEn("Test Description EN");

        var task = new Task();
        task.setId(91L);
        task.setTitle("Test");
        task.setTaskType("sql");
        task.setDescriptionDe("Text DE");
        task.setDescriptionEn("Text EN");
        task.setTaskCategories(Set.of(cat, cat2));
        task.setOrganizationalUnit(ou);
        task.setMaxPoints(BigDecimal.TEN);

        when(this.categoryRepository.findById(cat.getId())).thenReturn(Optional.of(cat));
        when(this.categoryRepository.findById(cat2.getId())).thenReturn(Optional.empty());
        service.response = "{\"questionId\": 99}";

        // Act
        var result = service.createQuestionFromTask(task).get();

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).allMatch(taskMoodleId -> taskMoodleId.getMoodleId() == 99L);
        assertTrue(service.postCalled);
    }
    //#endregion

    //#region --- UPDATE ---
    @Test
    void updateQuestionFromTask_disabled() throws ExecutionException, InterruptedException {
        // Arrange
        this.config.setEnabled(false);
        var service = new QuestionTestService(this.config, this.categoryRepository, this.taskMoodleIdRepository, this.taskGroupRepository);

        // Act
        var result = service.updateQuestionFromTask(null).get();

        // Assert
        assertThat(result).isEmpty();
        assertFalse(service.postCalled);
    }
    //#endregion

    private static class QuestionTestService extends QuestionService {
        String response;
        boolean throwException;
        boolean postCalled;

        protected QuestionTestService(MoodleConfig config,
                                      TaskCategoryRepository categoryRepository,
                                      TaskMoodleIdRepository taskMoodleIdRepository,
                                      TaskGroupRepository taskGroupRepository) {
            super(config, new ObjectMapper(), categoryRepository, taskMoodleIdRepository, taskGroupRepository);
        }

        @Override
        protected String post(Map<String, String> queryParameters, Map<String, String> body) throws URISyntaxException, IOException, InterruptedException {
            this.postCalled = true;
            if (throwException)
                throw new IOException("Test");
            return response;
        }
    }
}
