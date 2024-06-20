package at.jku.dke.etutor.task_administration.moodle;

import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnit;
import at.jku.dke.etutor.task_administration.data.entities.TaskCategory;
import at.jku.dke.etutor.task_administration.data.repositories.OrganizationalUnitRepository;
import at.jku.dke.etutor.task_administration.data.repositories.TaskCategoryRepository;
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
 * Service for managing moodle question categories.
 */
@Service
public class QuestionCategoryService extends MoodleService {

    private final OrganizationalUnitRepository ouRepository;
    private final TaskCategoryRepository categoryRepository;

    /**
     * Creates a new instance of class {@link QuestionCategoryService}.
     *
     * @param config             The moodle configuration.
     * @param objectMapper       The object mapper.
     * @param ouRepository       The organizational unit repository.
     * @param categoryRepository The task category repository.
     */
    public QuestionCategoryService(MoodleConfig config, ObjectMapper objectMapper, OrganizationalUnitRepository ouRepository, TaskCategoryRepository categoryRepository) {
        super(config, objectMapper);
        this.ouRepository = ouRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Creates a new question category for the given task category.
     *
     * @param category The task category.
     * @return The id of the created question category or an empty result if an error occurred.
     */
    @Async
    public CompletableFuture<Optional<Integer>> createQuestionCategory(TaskCategory category) {
        if (this.config.isDisabled())
            return CompletableFuture.completedFuture(Optional.empty());

        LOG.info("Creating question category for task category {}.", category.getId());

        // Load data
        OrganizationalUnit ou = this.ouRepository.findById(category.getOrganizationalUnit().getId()).orElseThrow();
        if (ou.getMoodleId() == null) {
            LOG.warn("Failed to create question category for task category {} because its organizational unit has no moodle-id.", category.getId());
            return CompletableFuture.completedFuture(Optional.empty());
        }

        TaskCategory parent = null;
        if (category.getParent() != null)
            parent = this.categoryRepository.findById(category.getParent().getId()).orElseThrow();
        if (parent != null && parent.getMoodleId() == null) {
            LOG.warn("Failed to create question category for task category {} because its parent has no moodle-id.", category.getId());
            return CompletableFuture.completedFuture(Optional.empty());
        }

        // Build body
        Map<String, String> body = new HashMap<>();
        body.put("data[course_category_id]", ou.getMoodleId().toString());
        if (parent != null)
            body.put("data[parent_question_category_id]", parent.getMoodleId().toString());
        body.put("data[id]", "TC_" + category.getId().toString());
        body.put("data[name]", category.getName());

        // Send request
        try {
            String responseBody = this.post(getDefaultQueryParameters("local_etutorsync_create_question_category"), body);
            QuestionCategory result = objectMapper.readValue(responseBody, QuestionCategory.class);
            LOG.debug("Received {} for created task category {}", result, category.getId());
            return CompletableFuture.completedFuture(Optional.of(result.id()));
        } catch (URISyntaxException | RuntimeException | InterruptedException | IOException ex) {
            LOG.error("Failed to create question category for task category {}.", category.getId(), ex);
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }

    /**
     * Updates an existing question category for the given task category.
     *
     * @param category The task category.
     */
    @Async
    public void updateQuestionCategory(TaskCategory category) {
        if (this.config.isDisabled())
            return;
        if (category.getMoodleId() == null)
            return;

        LOG.info("Updating question category for task category {}.", category.getId());

        // Load data
        OrganizationalUnit ou = this.ouRepository.findById(category.getOrganizationalUnit().getId()).orElseThrow();
        if (ou.getMoodleId() == null) {
            LOG.warn("Failed to update question category for task category {} because its organizational unit has no moodle-id.", category.getId());
            return;
        }

        TaskCategory parent = null;
        if (category.getParent() != null)
            parent = this.categoryRepository.findById(category.getParent().getId()).orElseThrow();
        if (parent != null && parent.getMoodleId() == null) {
            LOG.warn("Failed to update question category for task category {} because its parent has no moodle-id.", category.getId());
            return;
        }

        // Build body
        Map<String, String> body = new HashMap<>();
        body.put("data[course_category_id]", ou.getMoodleId().toString());
        if (parent != null)
            body.put("data[parent_question_category_id]", parent.getMoodleId().toString());
        body.put("data[id]", category.getMoodleId().toString());
        body.put("data[name]", category.getName());

        // Send request
        try {
            var result = this.post(getDefaultQueryParameters("local_etutorsync_update_question_category"), body);
            LOG.debug("Received {} for updated task category {}", result, category.getId());
        } catch (URISyntaxException | RuntimeException | InterruptedException | IOException ex) {
            LOG.error("Failed to update question category for task category {}.", category.getId(), ex);
        }
    }

    private record QuestionCategory(int id) {
    }
}
