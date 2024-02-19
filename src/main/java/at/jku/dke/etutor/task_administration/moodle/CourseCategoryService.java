package at.jku.dke.etutor.task_administration.moodle;

import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing moodle course categories.
 */
@Service
public class CourseCategoryService extends MoodleService {

    /**
     * Creates a new instance of class {@link CourseCategoryService}.
     *
     * @param config       The moodle configuration.
     * @param objectMapper The object mapper.
     */
    public CourseCategoryService(MoodleConfig config, ObjectMapper objectMapper) {
        super(config, objectMapper);
    }

    /**
     * Creates a new course category for the given organizational unit.
     *
     * @param organizationalUnit The organizational unit.
     * @return The id of the created course category or an empty result if an error occurred.
     */
    @Async
    public CompletableFuture<Optional<Integer>> createCourseCategory(OrganizationalUnit organizationalUnit) {
        if (this.config.isDisabled())
            return CompletableFuture.completedFuture(Optional.empty());

        LOG.info("Creating course category for organizational unit {}.", organizationalUnit.getId());
        Map<String, String> body = new HashMap<>();
        body.put("categories[0][name]", organizationalUnit.getName());
        //body.put("categories[0][idnumber]", organizationalUnit.getId().toString());
        body.put("categories[0][description]", "Course Category for eTutor Organizational Unit: " + organizationalUnit.getName());
        body.put("categories[0][descriptionformat]", "2");

        // Send request
        try {
            String responseBody = this.post(getDefaultQueryParameters("core_course_create_categories"), body);
            CourseCategory[] result = objectMapper.readValue(responseBody, CourseCategory[].class);
            if (result.length > 0)
                return CompletableFuture.completedFuture(Optional.of(result[0].id()));
        } catch (URISyntaxException | RuntimeException | InterruptedException | IOException ex) {
            LOG.error("Failed to create course category for organizational unit {}.", organizationalUnit.getId(), ex);
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    /**
     * Updates an existing course category.
     *
     * @param organizationalUnit The organizational unit.
     */
    @Async
    public void updateCourseCategory(OrganizationalUnit organizationalUnit) {
        if (this.config.isDisabled())
            return;
        if (organizationalUnit.getMoodleId() == null)
            return;

        LOG.info("Updating course category for organizational unit {}.", organizationalUnit.getId());
        Map<String, String> body = new HashMap<>();
        body.put("categories[0][id]", organizationalUnit.getMoodleId().toString());
        body.put("categories[0][name]", organizationalUnit.getName());
        body.put("categories[0][idnumber]", organizationalUnit.getId().toString());
        body.put("categories[0][description]", "Course Category for eTutor Organizational Unit: " + organizationalUnit.getName());
        body.put("categories[0][descriptionformat]", "2");

        // Send request
        try {
            this.post(getDefaultQueryParameters("core_course_update_categories"), body);
        } catch (URISyntaxException | RuntimeException | InterruptedException | IOException ex) {
            LOG.error("Failed to update course category for organizational unit {}.", organizationalUnit.getId(), ex);
        }
    }

    private record CourseCategory(int id, String name) {
    }
}
