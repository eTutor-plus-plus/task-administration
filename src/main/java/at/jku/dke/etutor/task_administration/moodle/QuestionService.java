package at.jku.dke.etutor.task_administration.moodle;

import at.jku.dke.etutor.task_administration.data.entities.*;
import at.jku.dke.etutor.task_administration.data.repositories.OrganizationalUnitRepository;
import at.jku.dke.etutor.task_administration.data.repositories.TaskCategoryRepository;
import at.jku.dke.etutor.task_administration.data.repositories.TaskMoodleidRepository;
import at.jku.dke.etutor.task_administration.data.repositories.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class QuestionService extends MoodleService {


    private final OrganizationalUnitRepository ouRepository;
    private final TaskCategoryRepository categoryRepository;
    private final TaskRepository taskRepository;
    private final TaskMoodleidRepository taskMoodleidRepository;

    /**
     * Creates a new instance of class {@link MoodleService}.
     *
     * @param config       The moodle configuration.
     * @param objectMapper The object mapper.
     */
    protected QuestionService(MoodleConfig config, ObjectMapper objectMapper, OrganizationalUnitRepository ouRepository, TaskCategoryRepository categoryRepository, TaskRepository taskRepository,
                              TaskMoodleidRepository taskMoodleidRepository) {
        super(config, objectMapper);

        this.ouRepository = ouRepository;
        this.categoryRepository = categoryRepository;
        this.taskRepository = taskRepository;
        this.taskMoodleidRepository = taskMoodleidRepository;
    }

    @Async
    public CompletableFuture<Optional<List<TaskMoodleid>>> createQuestionFromTask(Task task) {
        LOG.info("starting moodle Task sync");
        if (this.config.isDisabled()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        if(task.getTaskCategories().isEmpty()){
            return CompletableFuture.completedFuture(Optional.empty());
        }
        //vulnerable to org.hibernate.LazyInitializationException
        ArrayList<TaskMoodleid> moodleIds = new ArrayList<>();
        Object[] categoryIds = task.getTaskCategories().stream().map(AuditedEntity::getId).toArray();
        //Iterator<TaskCategory> taskCategoryIterator = task.getTaskCategories().iterator();
        for(int i =0; i< categoryIds.length;i++) {
            //TaskCategory taskCategory =taskCategoryIterator.next();
            int moodleId = categoryRepository.findById((long) categoryIds[i]).get().getMoodleId();
            LOG.info("Creating question from Task {} for category {}", task.getId(), moodleId);

            //format questiontext
            String qtext = "<span lang='de'>" + task.getDescriptionDe() + "</span> "
                + "<span lang='en'>" + task.getDescriptionEn() + "</span>";

            Map<String, String> body_question = new HashMap<>();
            body_question.put("data[category_id]", String.valueOf(moodleId));
            body_question.put("data[id]", task.getId().toString());
            body_question.put("data[name]", task.getTitle());
            body_question.put("data[questiontext]", qtext);
            body_question.put("data[points]", task.getMaxPoints().toString());
            body_question.put("data[coderunnertype]", "etutor-" + task.getTaskType());
            body_question.put("data[templateparams]", "{" + "\"TASK_ID\": " + task.getId() + ", \"FEEDBACK_LEVEL\":3}");
            LOG.info(body_question.toString());


            try {
                String responseBody = this.post(getDefaultQueryParameters("local_etutorsync_create_question"), body_question);
                Question result = objectMapper.readValue(responseBody, Question.class);
                moodleIds.add(new TaskMoodleid(task,categoryRepository.findById((long) categoryIds[i]).get(),result.questionid));
                LOG.info("Creating question with qid {}", result.questionid);

            } catch (URISyntaxException | RuntimeException | InterruptedException | IOException ex) {
                LOG.error("Failed to create Question {}.", task.getId(), ex);
                //return CompletableFuture.completedFuture(Optional.empty());
            }
        }
        return CompletableFuture.completedFuture(Optional.of(moodleIds));
    }

    public CompletableFuture<Optional<List<TaskMoodleid>>> updateQuestionFromTask(Task task) {
        LOG.info("starting moodle Task sync");
        if (this.config.isDisabled()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        List<TaskCategory> newCategories = task.getTaskCategories().stream().toList();

        List<TaskMoodleid> deprecatedCategories = new ArrayList<>(taskMoodleidRepository.findById_TaskId(task.getId()));
        deprecatedCategories =   deprecatedCategories.stream()
            .filter(x -> !task.getTaskCategories().contains(x.getTaskCategory()))
            .toList();

        List<TaskMoodleid> updateTaskCategories = new ArrayList<>(taskMoodleidRepository.findById_TaskId(task.getId()));
        updateTaskCategories = updateTaskCategories.stream()
            .filter(x -> task.getTaskCategories().contains(x.getTaskCategory()))
            .toList();

        Set<Long> oldCategoryIds = new HashSet<>(); // Adjust the type if the ID is not Long
        deprecatedCategories.forEach(x -> oldCategoryIds.add(x.getTaskCategory().getId()));
        updateTaskCategories.forEach(x -> oldCategoryIds.add(x.getTaskCategory().getId()));

        List<TaskCategory> newToCreateTaskCategories = newCategories.stream()
            .filter(x -> !oldCategoryIds.contains(x.getId()))
            .toList();


        ArrayList<TaskMoodleid> moodleIds = new ArrayList<>();

        //newQuestion
        Object[] newToCreateCategoryIds = newToCreateTaskCategories.stream().map(x -> x.getId()).toArray();

        for(int i =0; i< newToCreateCategoryIds.length;i++) {
            int moodleId = categoryRepository.findById((long) newToCreateCategoryIds[i]).get().getMoodleId();
            LOG.info("Creating question from Task {} for category {}", task.getId(), moodleId);

            //format questiontext
            String qtext = "<span lang='de'>" + task.getDescriptionDe() + "</span> "
                + "<span lang='en'>" + task.getDescriptionEn() + "</span>";

            Map<String, String> body_question = new HashMap<>();
            body_question.put("data[category_id]", String.valueOf(moodleId));
            body_question.put("data[id]", task.getId().toString());
            body_question.put("data[name]", task.getTitle());
            body_question.put("data[questiontext]", qtext);
            body_question.put("data[points]", task.getMaxPoints().toString());
            body_question.put("data[coderunnertype]", "etutor-" + task.getTaskType());
            body_question.put("data[templateparams]", "{" + "\"TASK_ID\": " + task.getId() + ", \"FEEDBACK_LEVEL\":3}");
            LOG.info(body_question.toString());


            try {
                String responseBody = this.post(getDefaultQueryParameters("local_etutorsync_create_question"), body_question);
                Question result = objectMapper.readValue(responseBody, Question.class);
                moodleIds.add(new TaskMoodleid(task,categoryRepository.findById((long) newToCreateCategoryIds[i]).get(),result.questionid));
                LOG.info("Creating question with qid {}", result.questionid);

            } catch (URISyntaxException | RuntimeException | InterruptedException | IOException ex) {
                LOG.error("Failed to create Question {}.", task.getId(), ex);
                //return CompletableFuture.completedFuture(Optional.empty());
            }
        }

        //oldQuestion
        Object[] deprecatedQuestionIds = deprecatedCategories.stream().map(x->x.getMoodleId()).toArray();

        for(int i =0; i< deprecatedQuestionIds.length;i++) {

            LOG.info("Deprecating question from Task {} for category {}", task.getId(), deprecatedQuestionIds[i]);

            Map<String, String> body_question = new HashMap<>();
            body_question.put("data[course_category_id]", task.getOrganizationalUnit().getMoodleId().toString());
            body_question.put("data[question_id]", deprecatedQuestionIds[i].toString());
            body_question.put("data[title_extension]", "DEPRECATED_");
            LOG.info(body_question.toString());


            try {
                String responseBody = this.post(getDefaultQueryParameters("local_etutorsync_deprecate_old_question"), body_question);
                Question result = objectMapper.readValue(responseBody, Question.class);
                LOG.info("Depreacte question with qid {}", result.questionid);

            } catch (URISyntaxException | RuntimeException | InterruptedException | IOException ex) {
                LOG.error("Failed to deprecate old Question {}.", task.getId(), ex);
                //return CompletableFuture.completedFuture(Optional.empty());
            }
        }


        //updateExistingQuestions
        Object[] updateCategoryIds = updateTaskCategories.stream().map(x -> x.getTaskCategory().getId()).toArray();

        for(int i =0; i< updateCategoryIds.length;i++) {
            //TaskCategory taskCategory =taskCategoryIterator.next();
            long oldMdoodleId = taskMoodleidRepository.findById_TaskIdAndId_TaskCategoryId(task.getId(),(long)updateCategoryIds[i]).get().getMoodleId();

            int CatMoodleId = categoryRepository.findById((long) updateCategoryIds[i]).get().getMoodleId();

            LOG.info("updating question from Task {} for category {}", task.getId(), CatMoodleId);

            //format questiontext
            String qtext = "<span lang='de'>" + task.getDescriptionDe() + "</span> "
                + "<span lang='en'>" + task.getDescriptionEn() + "</span>";

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
            LOG.info(body_question.toString());


            try {
                String responseBody = this.post(getDefaultQueryParameters("local_etutorsync_update_question"), body_question);
                Question result = objectMapper.readValue(responseBody, Question.class);
                moodleIds.add(new TaskMoodleid(task,categoryRepository.findById((long) updateCategoryIds[i]).get(),result.questionid));
                LOG.info("Creating question with qid {}", result.questionid);

            } catch (URISyntaxException | RuntimeException | InterruptedException | IOException ex) {
                LOG.error("Failed to create Question {}.", task.getId(), ex);
                //return CompletableFuture.completedFuture(Optional.empty());
            }
        }

        taskMoodleidRepository.deleteByTaskId((long)task.getId());
        return CompletableFuture.completedFuture(Optional.of(moodleIds));
    }

    private record Question(long questionid) {
    }
}
