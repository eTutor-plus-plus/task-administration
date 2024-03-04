package at.jku.dke.etutor.task_administration.moodle;

import at.jku.dke.etutor.task_administration.data.entities.*;
import at.jku.dke.etutor.task_administration.data.repositories.TaskCategoryRepository;
import at.jku.dke.etutor.task_administration.data.repositories.TaskMoodleidRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class QuestionService extends MoodleService {

    private final TaskCategoryRepository categoryRepository;

    private final TaskMoodleidRepository taskMoodleidRepository;

    /**
     * Creates a new instance of class {@link QuestionService}.
     *
     * @param config       The moodle configuration.
     * @param objectMapper The object mapper.
     * @param categoryRepository The category Repository.
     * @param taskMoodleidRepository The Moodleid Repository.
     */
    protected QuestionService(MoodleConfig config, ObjectMapper objectMapper, TaskCategoryRepository categoryRepository, TaskMoodleidRepository taskMoodleidRepository) {
        super(config, objectMapper);


        this.categoryRepository = categoryRepository;

        this.taskMoodleidRepository = taskMoodleidRepository;
    }


    /**
     * Creates a question in Moodle for each category in the given Task
     *
     * @param task The task to create the questions.
     * @return A list of all created moodleIDs as List<TaskMoodleid>.
     */
    @Async
    public CompletableFuture<Optional<List<TaskMoodleid>>> createQuestionFromTask(Task task) {
        LOG.info("starting moodle Task sync");
        if (this.config.isDisabled()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        if (task.getTaskCategories().isEmpty()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        //vulnerable to org.hibernate.LazyInitializationException
        ArrayList<TaskMoodleid> moodleIds = new ArrayList<>();

        //Array of all categoryIds in which the task should exist
        Object[] categoryIds = task.getTaskCategories().stream().map(AuditedEntity::getId).toArray();

        for (int i = 0; i < categoryIds.length; i++) {


            int moodleId = categoryRepository.findById((long) categoryIds[i]).get().getMoodleId();

            //format questiontext
            String qtext = "<span lang='de'>" + task.getDescriptionDe() + "</span> " + "<span lang='en'>" + task.getDescriptionEn() + "</span>";

            Map<String, String> body_question = new HashMap<>();
            body_question.put("data[category_id]", String.valueOf(moodleId));
            body_question.put("data[id]", task.getId().toString());
            body_question.put("data[name]", task.getTitle());
            body_question.put("data[questiontext]", qtext);
            body_question.put("data[course_category_id]", task.getOrganizationalUnit().getMoodleId().toString());
            body_question.put("data[points]", task.getMaxPoints().toString());
            body_question.put("data[coderunnertype]", "etutor-" + task.getTaskType());
            body_question.put("data[templateparams]", "{" + "\"TASK_ID\": " + task.getId() + ", \"FEEDBACK_LEVEL\":3}");


            try {
                String responseBody = this.post(getDefaultQueryParameters("local_etutorsync_create_question"), body_question);
                Question result = objectMapper.readValue(responseBody, Question.class);
                moodleIds.add(new TaskMoodleid(task, categoryRepository.findById((long) categoryIds[i]).get(), result.questionid));
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
     * @return A list of all created moodleIDs as List<TaskMoodleid>.
     */
    public CompletableFuture<Optional<List<TaskMoodleid>>> updateQuestionFromTask(Task task) {
        LOG.info("starting moodle Task sync");
        if (this.config.isDisabled()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        //All categories in which the question should end up
        List<TaskCategory> newCategories = task.getTaskCategories().stream().toList();

        //List of all categories in which the question should no longer be contained
        List<TaskMoodleid> deprecatedCategories = new ArrayList<>(taskMoodleidRepository.findById_TaskId(task.getId()));
        deprecatedCategories = deprecatedCategories.stream().filter(x -> !task.getTaskCategories().contains(x.getTaskCategory())).toList();


        //List of all categories keeping the question
        List<TaskMoodleid> updateTaskCategories = new ArrayList<>(taskMoodleidRepository.findById_TaskId(task.getId()));
        updateTaskCategories = updateTaskCategories.stream().filter(x -> task.getTaskCategories().contains(x.getTaskCategory())).toList();

        //List of all category Ids which are either deprecated or already existing and persisting
        Set<Long> oldCategoryIds = new HashSet<>();
        deprecatedCategories.forEach(x -> oldCategoryIds.add(x.getTaskCategory().getId()));
        updateTaskCategories.forEach(x -> oldCategoryIds.add(x.getTaskCategory().getId()));

        //List of all Categories newly getting this question
        List<TaskCategory> newToCreateTaskCategories = newCategories.stream().filter(x -> !oldCategoryIds.contains(x.getId())).toList();


        ArrayList<TaskMoodleid> moodleIds = new ArrayList<>();

        //Creating the questions which are new
        Object[] newToCreateCategoryIds = newToCreateTaskCategories.stream().map(x -> x.getId()).toArray();

        for (int i = 0; i < newToCreateCategoryIds.length; i++) {
            int moodleId = categoryRepository.findById((long) newToCreateCategoryIds[i]).orElseThrow().getMoodleId();


            //format questiontext
            String qtext = "<span lang='de'>" + task.getDescriptionDe() + "</span> " + "<span lang='en'>" + task.getDescriptionEn() + "</span>";

            Map<String, String> body_question = new HashMap<>();
            body_question.put("data[category_id]", String.valueOf(moodleId));
            body_question.put("data[id]", task.getId().toString());
            body_question.put("data[name]", task.getTitle());
            body_question.put("data[questiontext]", qtext);
            body_question.put("data[points]", task.getMaxPoints().toString());
            body_question.put("data[coderunnertype]", "etutor-" + task.getTaskType());
            body_question.put("data[course_category_id]", task.getOrganizationalUnit().getMoodleId().toString());
            body_question.put("data[templateparams]", "{" + "\"TASK_ID\": " + task.getId() + ", \"FEEDBACK_LEVEL\":3}");


            try {
                String responseBody = this.post(getDefaultQueryParameters("local_etutorsync_create_question"), body_question);
                Question result = objectMapper.readValue(responseBody, Question.class);
                moodleIds.add(new TaskMoodleid(task, categoryRepository.findById((long) newToCreateCategoryIds[i]).get(), result.questionid));
                LOG.info("Created question with qid {}", result.questionid);

            } catch (URISyntaxException | RuntimeException | InterruptedException | IOException ex) {
                LOG.error("Failed to create Question {}.", task.getId(), ex);
                return CompletableFuture.completedFuture(Optional.empty());
            }
        }

        //Changing the title of old (no longer supported categories) questions to DEPRECATED_
        Object[] deprecatedQuestionIds = deprecatedCategories.stream().map(x -> x.getMoodleId()).toArray();

        for (int i = 0; i < deprecatedQuestionIds.length; i++) {


            Map<String, String> body_question = new HashMap<>();
            body_question.put("data[course_category_id]", task.getOrganizationalUnit().getMoodleId().toString());
            body_question.put("data[question_id]", deprecatedQuestionIds[i].toString());
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


        //Updates existing questions by creating a new question and updating the Version in the questionbank
        Object[] updateCategoryIds = updateTaskCategories.stream().map(x -> x.getTaskCategory().getId()).toArray();

        for (int i = 0; i < updateCategoryIds.length; i++) {
            long oldMdoodleId = taskMoodleidRepository.findById_TaskIdAndId_TaskCategoryId(task.getId(), (long) updateCategoryIds[i]).get().getMoodleId();
            int CatMoodleId = categoryRepository.findById((long) updateCategoryIds[i]).get().getMoodleId();

            //format questiontext
            String qtext = "<span lang='de'>" + task.getDescriptionDe() + "</span> " + "<span lang='en'>" + task.getDescriptionEn() + "</span>";

            Map<String, String> body_question = new HashMap<>();
            body_question.put("data[course_category_id]", task.getOrganizationalUnit().getMoodleId().toString());
            body_question.put("data[category_id]", String.valueOf(CatMoodleId));
            body_question.put("data[oldMoodleId]", String.valueOf(oldMdoodleId));
            body_question.put("data[id]", task.getId().toString());
            body_question.put("data[name]", task.getTitle());
            body_question.put("data[questiontext]", qtext);
            body_question.put("data[points]", task.getMaxPoints().toString());
            body_question.put("data[coderunnertype]", "etutor-" + task.getTaskType());
            body_question.put("data[templateparams]", "{" + "\"TASK_ID\": " + task.getId() + ", \"FEEDBACK_LEVEL\":3}");


            try {
                String responseBody = this.post(getDefaultQueryParameters("local_etutorsync_update_question"), body_question);
                Question result = objectMapper.readValue(responseBody, Question.class);
                moodleIds.add(new TaskMoodleid(task, categoryRepository.findById((long) updateCategoryIds[i]).get(), result.questionid));
                LOG.info("Created question with qid {}", result.questionid);

            } catch (URISyntaxException | RuntimeException | InterruptedException | IOException ex) {
                LOG.error("Failed to create Question {}.", task.getId(), ex);
                return CompletableFuture.completedFuture(Optional.empty());
            }
        }
        return CompletableFuture.completedFuture(Optional.of(moodleIds));
    }

    private record Question(long questionid) {
    }
}
