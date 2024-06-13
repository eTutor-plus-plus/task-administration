package at.jku.dke.etutor.task_administration.moodle;

import at.jku.dke.etutor.task_administration.data.entities.*;
import at.jku.dke.etutor.task_administration.data.repositories.TaskCategoryRepository;
import at.jku.dke.etutor.task_administration.data.repositories.TaskGroupRepository;
import at.jku.dke.etutor.task_administration.data.repositories.TaskMoodleIdRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing moodle questions.
 */
@Service
public class QuestionService extends MoodleService {

    private final TaskCategoryRepository categoryRepository;
    private final TaskMoodleIdRepository taskMoodleIdRepository;
    private final TaskGroupRepository taskGroupRepository;

    /**
     * Creates a new instance of class {@link QuestionService}.
     *
     * @param config                 The moodle configuration.
     * @param objectMapper           The object mapper.
     * @param categoryRepository     The category repository.
     * @param taskMoodleIdRepository The moodleId repository.
     * @param taskGroupRepository    The task-group repository.
     */
    protected QuestionService(MoodleConfig config, ObjectMapper objectMapper, TaskCategoryRepository categoryRepository, TaskMoodleIdRepository taskMoodleIdRepository, TaskGroupRepository taskGroupRepository) {
        super(config, objectMapper);
        this.categoryRepository = categoryRepository;
        this.taskMoodleIdRepository = taskMoodleIdRepository;
        this.taskGroupRepository = taskGroupRepository;
    }

    /**
     * Creates a question in Moodle for each category in the given Task
     *
     * @param task The task to create the questions.
     * @return A list of all created moodleIDs as List<TaskMoodleId>.
     */
    @Async
    public CompletableFuture<Optional<List<TaskMoodleId>>> createQuestionFromTask(Task task) {
        if (this.config.isDisabled() ||
            task.getStatus() != TaskStatus.APPROVED ||
            task.getTaskCategories().isEmpty() ||
            task.getOrganizationalUnit().getMoodleId() == null) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        LOG.info("starting moodle Task sync");
        ArrayList<TaskMoodleId> moodleIds = new ArrayList<>();

        // Array of all categoryIds in which the task should exist
        Object[] categoryIds = task.getTaskCategories().stream().map(AuditedEntity::getId).toArray();
        for (Object categoryId : categoryIds) {
            var category = categoryRepository.findById((long) categoryId);
            if (category.isEmpty())
                continue;

            Map<String, String> body_question = this.buildTaskParameterMap(task, category.get());
            try {
                String responseBody = this.post(getDefaultQueryParameters("local_etutorsync_create_question"), body_question);
                Question result = objectMapper.readValue(responseBody, Question.class);
                moodleIds.add(new TaskMoodleId(task, category.get(), result.questionid));
                LOG.info("Created question with qid {}", result.questionid);
            } catch (URISyntaxException | RuntimeException | InterruptedException | IOException ex) {
                LOG.error("Failed to create Question {}.", task.getId(), ex);
                return CompletableFuture.completedFuture(Optional.empty());
            }
        }
        return CompletableFuture.completedFuture(Optional.of(moodleIds));
    }

    /**
     * Updates questions already existing in Moodle to the given task
     *
     * @param task Task to which the questions should be updated.
     * @return A list of all created moodleIDs as List<TaskMoodleId>.
     */
    // @Async does not work
    public CompletableFuture<Optional<List<TaskMoodleId>>> updateQuestionFromTask(Task task) {
        LOG.info("Starting Moodle Task sync");
        if (this.config.isDisabled() || task.getStatus() != TaskStatus.APPROVED) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        // All categories in which the question should end up
        Set<TaskCategory> newCategories = this.categoryRepository.findByTasks_Id(task.getId());

        // List of all categories in which the question should no longer be contained
        List<TaskMoodleId> deprecatedCategories = new ArrayList<>(taskMoodleIdRepository.findById_TaskId(task.getId()));
        deprecatedCategories = deprecatedCategories.stream().filter(x -> !newCategories.contains(x.getTaskCategory())).toList();

        // List of all categories keeping the question
        List<TaskMoodleId> updateTaskCategories = new ArrayList<>(taskMoodleIdRepository.findById_TaskId(task.getId()));
        updateTaskCategories = updateTaskCategories.stream().filter(x -> newCategories.contains(x.getTaskCategory())).toList();

        // List of all category Ids which are either deprecated or already existing and persisting
        Set<Long> oldCategoryIds = new HashSet<>();
        deprecatedCategories.forEach(x -> oldCategoryIds.add(x.getTaskCategory().getId()));
        updateTaskCategories.forEach(x -> oldCategoryIds.add(x.getTaskCategory().getId()));

        // List of all Categories newly getting this question
        List<TaskCategory> newToCreateTaskCategories = newCategories.stream().filter(x -> !oldCategoryIds.contains(x.getId())).toList();
        ArrayList<TaskMoodleId> moodleIds = new ArrayList<>();

        // Creating the questions which are new
        Object[] newToCreateCategoryIds = newToCreateTaskCategories.stream().map(AuditedEntity::getId).toArray();
        for (Object newToCreateCategoryId : newToCreateCategoryIds) {
            var category = categoryRepository.findById((long) newToCreateCategoryId);
            if (category.isEmpty())
                continue;

            Map<String, String> body_question = this.buildTaskParameterMap(task, category.get());
            try {
                String responseBody = this.post(getDefaultQueryParameters("local_etutorsync_create_question"), body_question);
                Question result = objectMapper.readValue(responseBody, Question.class);
                moodleIds.add(new TaskMoodleId(task, category.get(), result.questionid));
                LOG.info("Created question with qid {}", result.questionid);
            } catch (URISyntaxException | RuntimeException | InterruptedException | IOException ex) {
                LOG.error("Failed to create Question {}.", task.getId(), ex);
                return CompletableFuture.completedFuture(Optional.empty());
            }
        }

        // Changing the title of old (no longer supported categories) questions to DEPRECATED_
        Object[] deprecatedQuestionIds = deprecatedCategories.stream().map(TaskMoodleId::getMoodleId).toArray();
        for (Object deprecatedQuestionId : deprecatedQuestionIds) {
            Map<String, String> body_question = new HashMap<>();
            body_question.put("data[course_category_id]", task.getOrganizationalUnit().getMoodleId().toString());
            body_question.put("data[question_id]", deprecatedQuestionId.toString());
            body_question.put("data[title_extension]", "DEPRECATED_");

            try {
                String responseBody = this.post(getDefaultQueryParameters("local_etutorsync_deprecate_old_question"), body_question);
                Question result = objectMapper.readValue(responseBody, Question.class);
                LOG.info("Deprecated question with qid {}", result.questionid);
            } catch (URISyntaxException | RuntimeException | InterruptedException | IOException ex) {
                LOG.error("Failed to deprecate old Question {}.", task.getId(), ex);
                return CompletableFuture.completedFuture(Optional.empty());
            }
        }

        // Updates existing questions by creating a new question and updating the Version in the questionbank
        Object[] updateCategoryIds = updateTaskCategories.stream().map(x -> x.getTaskCategory().getId()).toArray();
        for (Object updateCategoryId : updateCategoryIds) {
            var oldMoodle = taskMoodleIdRepository.findById_TaskIdAndId_TaskCategoryId(task.getId(), (long) updateCategoryId);
            var newCategory = categoryRepository.findById((long) updateCategoryId);
            if (oldMoodle.isEmpty() || newCategory.isEmpty())
                continue;

            Map<String, String> body_question = this.buildTaskParameterMap(task, newCategory.get());
            body_question.put("data[oldMoodleId]", String.valueOf(oldMoodle.get().getMoodleId()));

            try {
                String responseBody = this.post(getDefaultQueryParameters("local_etutorsync_update_question"), body_question);
                Question result = objectMapper.readValue(responseBody, Question.class);
                moodleIds.add(new TaskMoodleId(task, newCategory.get(), result.questionid));
                LOG.info("Created question with qid {}", result.questionid);
            } catch (URISyntaxException | RuntimeException | InterruptedException | IOException ex) {
                LOG.error("Failed to create Question {}.", task.getId(), ex);
                return CompletableFuture.completedFuture(Optional.empty());
            }
        }
        return CompletableFuture.completedFuture(Optional.of(moodleIds));
    }

    private Map<String, String> buildTaskParameterMap(Task task, TaskCategory category) {
        StringBuilder builder = new StringBuilder("<span lang=\"de\" class=\"multilang\">");
        TaskGroup taskGroup = null;
        if (task.getTaskGroup() != null && task.getTaskGroup().getId() != null)
            taskGroup = this.taskGroupRepository.findById(task.getTaskGroup().getId()).orElse(null);

        // german
        if (taskGroup != null)
            builder.append(taskGroup.getDescriptionDe()).append("<div style=\"margin-bottom: 1.5em;\"></div>");
        builder.append(task.getDescriptionDe()).append("</span>");

        // english
        builder.append("<span lang=\"en\" class=\"multilang\">");
        if (taskGroup != null)
            builder.append(taskGroup.getDescriptionEn()).append("<div style=\"margin-bottom: 1.5em;\"></div>");
        builder.append(task.getDescriptionEn()).append("</span>");

        // data
        Map<String, String> body_question = new HashMap<>();
        body_question.put("data[category_id]", String.valueOf(category.getMoodleId()));
        body_question.put("data[id]", task.getId().toString());
        body_question.put("data[name]", task.getTitle());
        body_question.put("data[questiontext]", builder.toString());
        body_question.put("data[course_category_id]", task.getOrganizationalUnit().getMoodleId().toString());
        body_question.put("data[points]", task.getMaxPoints().toString());
        body_question.put("data[coderunnertype]", "etutor-" + task.getTaskType());
        body_question.put("data[templateparams]", "{" + "\"TASK_ID\": " + task.getId() + ", \"FEEDBACK_LEVEL\":2}");
        body_question.put("data[examTask]", task.isExamTask() + "");
        return body_question;
    }

    private record Question(long questionid) { // do not rename to questionId!!
    }
}
