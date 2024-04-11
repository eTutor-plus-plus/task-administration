package at.jku.dke.etutor.task_administration.moodle;

import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseCategoryServiceTest {

    @Test
    void createCourseCategory_disabled() {
        // Arrange
        var service = new CourseCategoryTestService(new MoodleConfig("test-token", "http://example.com", false), null);

        // Act
        var result = service.createCourseCategory(null).resultNow();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void createCourseCategory_enabled_withReturnId() {
        // Arrange
        var service = new CourseCategoryTestService(new MoodleConfig("test-token", "http://example.com", true), new ObjectMapper());
        var ou = new OrganizationalUnit(1L);
        ou.setName("Test");
        service.response = "[{\"id\":99, \"name\":\"Test\"}]";

        // Act
        var result = service.createCourseCategory(ou).resultNow();

        // Assert
        assertThat(result).hasValue(99);
    }

    @Test
    void createCourseCategory_enabled_withoutReturnId() {
        // Arrange
        var service = new CourseCategoryTestService(new MoodleConfig("test-token", "http://example.com", true), new ObjectMapper());
        var ou = new OrganizationalUnit(1L);
        ou.setName("Test");
        service.response = "[]";

        // Act
        var result = service.createCourseCategory(ou).resultNow();

        // Assert
       assertThat(result).isEmpty();
    }

    @Test
    void createCourseCategory_enabled_error() {
        // Arrange
        var service = new CourseCategoryTestService(new MoodleConfig("test-token", "http://example.com", true), new ObjectMapper());
        var ou = new OrganizationalUnit(1L);
        ou.setName("Test");
        service.throwException = true;

        // Act
        var result = service.createCourseCategory(ou).resultNow();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void updateCourseCategory_disabled() {
        // Arrange
        var service = new CourseCategoryTestService(new MoodleConfig("test-token", "http://example.com", false), null);

        // Act
        service.updateCourseCategory(null);

        // Assert
        assertFalse(service.postCalled);
    }

    @Test
    void updateCourseCategory_enabled_withMoodleId() {
        // Arrange
        var service = new CourseCategoryTestService(new MoodleConfig("test-token", "http://example.com", true), new ObjectMapper());
        var ou = new OrganizationalUnit(1L);
        ou.setName("Test");
        ou.setMoodleId(99);

        // Act
        service.updateCourseCategory(ou);

        // Assert
        assertTrue(service.postCalled);
    }

    @Test
    void updateCourseCategory_enabled_withoutMoodleId() {
        // Arrange
        var service = new CourseCategoryTestService(new MoodleConfig("test-token", "http://example.com", true), new ObjectMapper());
        var ou = new OrganizationalUnit(1L);
        ou.setName("Test");

        // Act
        service.updateCourseCategory(ou);

        // Assert
        assertFalse(service.postCalled);
    }

    @Test
    void updateCourseCategory_enabled_error() {
        // Arrange
        var service = new CourseCategoryTestService(new MoodleConfig("test-token", "http://example.com", true), new ObjectMapper());
        var ou = new OrganizationalUnit(1L);
        ou.setName("Test");
        ou.setMoodleId(99);
        service.throwException = true;

        // Act
        service.updateCourseCategory(ou);

        // Assert
        assertTrue(service.postCalled);
    }

    private static class CourseCategoryTestService extends CourseCategoryService {
        String response;
        boolean throwException;
        boolean postCalled;

        public CourseCategoryTestService(MoodleConfig config, ObjectMapper objectMapper) {
            super(config, objectMapper);
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
