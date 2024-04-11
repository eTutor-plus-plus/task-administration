package at.jku.dke.etutor.task_administration.moodle;

import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnit;
import at.jku.dke.etutor.task_administration.data.entities.TaskCategory;
import at.jku.dke.etutor.task_administration.data.repositories.OrganizationalUnitRepository;
import at.jku.dke.etutor.task_administration.data.repositories.TaskCategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuestionCategoryServiceTest {

    @Test
    void createQuestionCategory_disabled() {
        // Arrange
        var service = new QuestionCategoryTestService(new MoodleConfig("test-token", "http://example.com", false), null, null, null);

        // Act
        var result = service.createQuestionCategory(null).resultNow();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void createQuestionCategory_enabled() {
        // Arrange
        var ou = new OrganizationalUnit();
        ou.setMoodleId(123);

        var parent = new TaskCategory();
        parent.setId(11L);
        parent.setName("Parent");
        parent.setMoodleId(29);

        var ouRep = mock(OrganizationalUnitRepository.class);
        when(ouRep.findById(any())).thenReturn(Optional.of(ou));

        var catRep = mock(TaskCategoryRepository.class);
        when(catRep.findById(11L)).thenReturn(Optional.of(parent));

        var service = new QuestionCategoryTestService(new MoodleConfig("test-token", "http://example.com", true), new ObjectMapper(), ouRep, catRep);
        service.response = "{\"id\":99}";

        var cat = new TaskCategory();
        cat.setId(22L);
        cat.setName("Test");
        cat.setParent(parent);
        cat.setOrganizationalUnit(ou);

        // Act
        var result = service.createQuestionCategory(cat).resultNow();

        // Assert
        assertThat(result).hasValue(99);
    }

    @Test
    void createQuestionCategory_enabled_withoutParent() {
        // Arrange
        var ou = new OrganizationalUnit();
        ou.setMoodleId(123);

        var ouRep = mock(OrganizationalUnitRepository.class);
        when(ouRep.findById(any())).thenReturn(Optional.of(ou));

        var catRep = mock(TaskCategoryRepository.class);

        var service = new QuestionCategoryTestService(new MoodleConfig("test-token", "http://example.com", true), new ObjectMapper(), ouRep, catRep);
        service.response = "{\"id\":99}";

        var cat = new TaskCategory();
        cat.setId(22L);
        cat.setName("Test");
        cat.setOrganizationalUnit(ou);

        // Act
        var result = service.createQuestionCategory(cat).resultNow();

        // Assert
        assertThat(result).hasValue(99);
    }

    @Test
    void createQuestionCategory_enabled_withoutParentMoodleId() {
        // Arrange
        var ou = new OrganizationalUnit();
        ou.setMoodleId(123);

        var parent = new TaskCategory();
        parent.setId(11L);
        parent.setName("Parent");

        var ouRep = mock(OrganizationalUnitRepository.class);
        when(ouRep.findById(any())).thenReturn(Optional.of(ou));

        var catRep = mock(TaskCategoryRepository.class);
        when(catRep.findById(11L)).thenReturn(Optional.of(parent));

        var service = new QuestionCategoryTestService(new MoodleConfig("test-token", "http://example.com", true), new ObjectMapper(), ouRep, catRep);
        service.response = "{\"id\":99}";

        var cat = new TaskCategory();
        cat.setId(22L);
        cat.setName("Test");
        cat.setParent(parent);
        cat.setOrganizationalUnit(ou);

        // Act
        var result = service.createQuestionCategory(cat).resultNow();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void createQuestionCategory_enabled_withoutOUMoodleId() {
        // Arrange
        var ou = new OrganizationalUnit();

        var ouRep = mock(OrganizationalUnitRepository.class);
        when(ouRep.findById(any())).thenReturn(Optional.of(ou));

        var catRep = mock(TaskCategoryRepository.class);

        var service = new QuestionCategoryTestService(new MoodleConfig("test-token", "http://example.com", true), new ObjectMapper(), ouRep, catRep);

        var cat = new TaskCategory();
        cat.setId(22L);
        cat.setName("Test");
        cat.setOrganizationalUnit(ou);

        // Act
        var result = service.createQuestionCategory(cat).resultNow();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void createQuestionCategory_enabled_error() {
        // Arrange
        var ou = new OrganizationalUnit();
        ou.setMoodleId(123);

        var ouRep = mock(OrganizationalUnitRepository.class);
        when(ouRep.findById(any())).thenReturn(Optional.of(ou));

        var catRep = mock(TaskCategoryRepository.class);

        var service = new QuestionCategoryTestService(new MoodleConfig("test-token", "http://example.com", true), new ObjectMapper(), ouRep, catRep);
        service.throwException = true;

        var cat = new TaskCategory();
        cat.setId(22L);
        cat.setName("Test");
        cat.setOrganizationalUnit(ou);

        // Act
        var result = service.createQuestionCategory(cat).resultNow();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void updateQuestionCategory_disabled() {
        // Arrange
        var service = new QuestionCategoryTestService(new MoodleConfig("test-token", "http://example.com", false), new ObjectMapper(), null, null);

        // Act
        service.updateQuestionCategory(null);

        // Assert
        assertFalse(service.postCalled);
    }

    @Test
    void updateQuestionCategory_enabled() {
        // Arrange
        var ou = new OrganizationalUnit();
        ou.setMoodleId(123);

        var parent = new TaskCategory();
        parent.setId(11L);
        parent.setName("Parent");
        parent.setMoodleId(29);

        var ouRep = mock(OrganizationalUnitRepository.class);
        when(ouRep.findById(any())).thenReturn(Optional.of(ou));

        var catRep = mock(TaskCategoryRepository.class);
        when(catRep.findById(11L)).thenReturn(Optional.of(parent));

        var service = new QuestionCategoryTestService(new MoodleConfig("test-token", "http://example.com", true), new ObjectMapper(), ouRep, catRep);
        service.response = "{\"id\":99}";

        var cat = new TaskCategory();
        cat.setId(22L);
        cat.setName("Test");
        cat.setParent(parent);
        cat.setOrganizationalUnit(ou);
        cat.setMoodleId(99);

        // Act
        service.updateQuestionCategory(cat);

        // Assert
        assertTrue(service.postCalled);
    }

    @Test
    void updateQuestionCategory_enabled_withoutMoodleId() {
        // Arrange
        var ou = new OrganizationalUnit();
        ou.setMoodleId(123);

        var ouRep = mock(OrganizationalUnitRepository.class);
        when(ouRep.findById(any())).thenReturn(Optional.of(ou));

        var catRep = mock(TaskCategoryRepository.class);

        var service = new QuestionCategoryTestService(new MoodleConfig("test-token", "http://example.com", true), new ObjectMapper(), ouRep, catRep);
        service.response = "{\"id\":99}";

        var cat = new TaskCategory();
        cat.setId(22L);
        cat.setName("Test");
        cat.setOrganizationalUnit(ou);

        // Act
        service.updateQuestionCategory(cat);

        // Assert
        assertFalse(service.postCalled);
    }

    @Test
    void updateQuestionCategory_enabled_withoutParent() {
        // Arrange
        var ou = new OrganizationalUnit();
        ou.setMoodleId(123);

        var ouRep = mock(OrganizationalUnitRepository.class);
        when(ouRep.findById(any())).thenReturn(Optional.of(ou));

        var catRep = mock(TaskCategoryRepository.class);

        var service = new QuestionCategoryTestService(new MoodleConfig("test-token", "http://example.com", true), new ObjectMapper(), ouRep, catRep);
        service.response = "{\"id\":99}";

        var cat = new TaskCategory();
        cat.setId(22L);
        cat.setName("Test");
        cat.setOrganizationalUnit(ou);
        cat.setMoodleId(99);

        // Act
        service.updateQuestionCategory(cat);

        // Assert
        assertTrue(service.postCalled);
    }

    @Test
    void updateQuestionCategory_enabled_withoutParentMoodleId() {
        // Arrange
        var ou = new OrganizationalUnit();
        ou.setMoodleId(123);

        var parent = new TaskCategory();
        parent.setId(11L);
        parent.setName("Parent");

        var ouRep = mock(OrganizationalUnitRepository.class);
        when(ouRep.findById(any())).thenReturn(Optional.of(ou));

        var catRep = mock(TaskCategoryRepository.class);
        when(catRep.findById(11L)).thenReturn(Optional.of(parent));

        var service = new QuestionCategoryTestService(new MoodleConfig("test-token", "http://example.com", true), new ObjectMapper(), ouRep, catRep);
        service.response = "{\"id\":99}";

        var cat = new TaskCategory();
        cat.setId(22L);
        cat.setName("Test");
        cat.setParent(parent);
        cat.setOrganizationalUnit(ou);
        cat.setMoodleId(99);

        // Act
        service.updateQuestionCategory(cat);

        // Assert
        assertFalse(service.postCalled);
    }

    @Test
    void updateQuestionCategory_enabled_withoutOUMoodleId() {
        // Arrange
        var ou = new OrganizationalUnit();

        var ouRep = mock(OrganizationalUnitRepository.class);
        when(ouRep.findById(any())).thenReturn(Optional.of(ou));

        var catRep = mock(TaskCategoryRepository.class);

        var service = new QuestionCategoryTestService(new MoodleConfig("test-token", "http://example.com", true), new ObjectMapper(), ouRep, catRep);

        var cat = new TaskCategory();
        cat.setId(22L);
        cat.setName("Test");
        cat.setOrganizationalUnit(ou);
        cat.setMoodleId(99);

        // Act
        service.updateQuestionCategory(cat);

        // Assert
        assertFalse(service.postCalled);
    }

    @Test
    void updateQuestionCategory_enabled_error() {
        // Arrange
        var ou = new OrganizationalUnit();
        ou.setMoodleId(123);

        var ouRep = mock(OrganizationalUnitRepository.class);
        when(ouRep.findById(any())).thenReturn(Optional.of(ou));

        var catRep = mock(TaskCategoryRepository.class);

        var service = new QuestionCategoryTestService(new MoodleConfig("test-token", "http://example.com", true), new ObjectMapper(), ouRep, catRep);
        service.throwException = true;

        var cat = new TaskCategory();
        cat.setId(22L);
        cat.setName("Test");
        cat.setOrganizationalUnit(ou);
        cat.setMoodleId(99);

        // Act
        service.updateQuestionCategory(cat);

        // Assert
        assertTrue(service.postCalled);
    }

    private static class QuestionCategoryTestService extends QuestionCategoryService {
        String response;
        boolean throwException;
        boolean postCalled;

        public QuestionCategoryTestService(MoodleConfig config, ObjectMapper objectMapper, OrganizationalUnitRepository ouRepository, TaskCategoryRepository categoryRepository) {
            super(config, objectMapper, ouRepository, categoryRepository);
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
