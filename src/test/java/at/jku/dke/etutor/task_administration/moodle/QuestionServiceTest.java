package at.jku.dke.etutor.task_administration.moodle;

import at.jku.dke.etutor.task_administration.data.entities.*;
import at.jku.dke.etutor.task_administration.data.repositories.TaskCategoryRepository;
import at.jku.dke.etutor.task_administration.data.repositories.TaskGroupRepository;
import at.jku.dke.etutor.task_administration.data.repositories.TaskMoodleIdRepository;
import at.jku.dke.etutor.task_administration.dto.TaskCategoryFeedbackLevelDto;
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
        var result = service.createQuestionFromTask(new Task()).get();

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
        task.setStatus(TaskStatus.APPROVED);
        task.setTitle("Test");
        task.setTaskType("sql");
        task.setDescriptionDe("Text DE");
        task.setDescriptionEn("Text EN");
        task.setTaskCategories(Set.of(cat, cat2));
        task.setOrganizationalUnit(ou);
        task.setMaxPoints(BigDecimal.TEN);

        when(this.categoryRepository.findById(cat.getId())).thenReturn(Optional.of(cat));
        when(this.categoryRepository.findById(cat2.getId())).thenReturn(Optional.empty());
        service.response = "{\"questionid\": 99}";

        // Act
        var result = service.createQuestionFromTask(task).get();

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).allMatch(taskMoodleId -> taskMoodleId.getMoodleId() == 99L);
        assertTrue(service.postCalled);
    }

    @Test
    void buildTemplateParams_containsAllFeedbackLevels() {
        // Arrange
        var task = new Task();
        task.setId(5L);

        // Act
        var result = QuestionService.buildTemplateParams(task, new TaskCategoryFeedbackLevelDto(1L, (short) 3, (short) 2));

        // Assert
        assertThat(result).isEqualTo("{\"TASK_ID\": 5, \"FEEDBACK_LEVEL\": 2, \"FEEDBACK_LEVEL_SUBMIT\": 3, \"FEEDBACK_LEVEL_DIAGNOSE\": 2}");
    }

    @Test
    void createQuestionFromTask_usesFeedbackLevelsOfCategory() throws ExecutionException, InterruptedException {
        // Arrange
        var service = new QuestionTestService(this.config, this.categoryRepository, this.taskMoodleIdRepository, this.taskGroupRepository);
        var task = createApprovedTaskWithCategory();
        service.response = "{\"questionid\": 99}";

        // Act
        var result = service.createQuestionFromTask(task, Map.of(1L, new TaskCategoryFeedbackLevelDto(1L, (short) 1, (short) 3))).get();

        // Assert
        assertThat(result).isPresent();
        assertThat(service.lastBody.get("data[templateparams]"))
            .isEqualTo("{\"TASK_ID\": 91, \"FEEDBACK_LEVEL\": 3, \"FEEDBACK_LEVEL_SUBMIT\": 1, \"FEEDBACK_LEVEL_DIAGNOSE\": 3}");
    }

    @Test
    void createQuestionFromTask_withoutFeedbackLevels_usesDefault() throws ExecutionException, InterruptedException {
        // Arrange
        var service = new QuestionTestService(this.config, this.categoryRepository, this.taskMoodleIdRepository, this.taskGroupRepository);
        var task = createApprovedTaskWithCategory();
        service.response = "{\"questionid\": 99}";

        // Act
        var result = service.createQuestionFromTask(task).get();

        // Assert
        assertThat(result).isPresent();
        assertThat(service.lastBody.get("data[templateparams]"))
            .isEqualTo("{\"TASK_ID\": 91, \"FEEDBACK_LEVEL\": 0, \"FEEDBACK_LEVEL_SUBMIT\": 0, \"FEEDBACK_LEVEL_DIAGNOSE\": 0}");
    }

    private Task createApprovedTaskWithCategory() {
        var ou = new OrganizationalUnit();
        ou.setId(2L);
        ou.setMoodleId(10);

        var cat = new TaskCategory();
        cat.setId(1L);
        cat.setName("Category");
        cat.setOrganizationalUnit(ou);
        cat.setMoodleId(7);

        var task = new Task();
        task.setId(91L);
        task.setStatus(TaskStatus.APPROVED);
        task.setTitle("Test");
        task.setTaskType("sql");
        task.setDescriptionDe("Text DE");
        task.setDescriptionEn("Text EN");
        task.setTaskCategories(Set.of(cat));
        task.setOrganizationalUnit(ou);
        task.setMaxPoints(BigDecimal.TEN);

        when(this.categoryRepository.findById(cat.getId())).thenReturn(Optional.of(cat));
        return task;
    }
    //#endregion

    //#region --- UPDATE ---
    @Test
    void updateQuestionFromTask_disabled() throws ExecutionException, InterruptedException {
        // Arrange
        this.config.setEnabled(false);
        var service = new QuestionTestService(this.config, this.categoryRepository, this.taskMoodleIdRepository, this.taskGroupRepository);

        // Act
        var result = service.updateQuestionFromTask(new Task()).get();

        // Assert
        assertThat(result).isEmpty();
        assertFalse(service.postCalled);
    }
    //#endregion

    private static class QuestionTestService extends QuestionService {
        String response;
        boolean throwException;
        boolean postCalled;
        Map<String, String> lastBody;

        protected QuestionTestService(MoodleConfig config,
                                      TaskCategoryRepository categoryRepository,
                                      TaskMoodleIdRepository taskMoodleIdRepository,
                                      TaskGroupRepository taskGroupRepository) {
            super(config, new ObjectMapper(), categoryRepository, taskMoodleIdRepository, taskGroupRepository);
        }

        @Override
        protected String post(Map<String, String> queryParameters, Map<String, String> body) throws URISyntaxException, IOException, InterruptedException {
            this.postCalled = true;
            this.lastBody = body;
            if (throwException)
                throw new IOException("Test");
            return response;
        }
    }
}
