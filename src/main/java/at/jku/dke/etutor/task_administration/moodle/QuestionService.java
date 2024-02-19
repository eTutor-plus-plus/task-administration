package at.jku.dke.etutor.task_administration.moodle;

import at.jku.dke.etutor.task_administration.data.entities.AuditedEntity;
import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnit;
import at.jku.dke.etutor.task_administration.data.entities.Task;
import at.jku.dke.etutor.task_administration.data.entities.TaskCategory;
import at.jku.dke.etutor.task_administration.data.repositories.OrganizationalUnitRepository;
import at.jku.dke.etutor.task_administration.data.repositories.TaskCategoryRepository;
import at.jku.dke.etutor.task_administration.data.repositories.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class QuestionService extends MoodleService {


    private final OrganizationalUnitRepository ouRepository;
    private final TaskCategoryRepository categoryRepository;
    private final TaskRepository taskRepository;

    /**
     * Creates a new instance of class {@link MoodleService}.
     *
     * @param config       The moodle configuration.
     * @param objectMapper The object mapper.
     */
    protected QuestionService(MoodleConfig config, ObjectMapper objectMapper, OrganizationalUnitRepository ouRepository, TaskCategoryRepository categoryRepository, TaskRepository taskRepository) {
        super(config, objectMapper);

        this.ouRepository = ouRepository;
        this.categoryRepository = categoryRepository;
        this.taskRepository = taskRepository;
    }

    @Async
    public CompletableFuture<Optional<int[]>> createQuestionFromTask(Task task) {
        LOG.info("starting moodle Task sync");
        if (this.config.isDisabled()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        if(task.getTaskCategories().isEmpty()){
            return CompletableFuture.completedFuture(Optional.empty());
        }
        //vulnerable to org.hibernate.LazyInitializationException
        ArrayList<Integer> moodleIds = new ArrayList<>();
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
                moodleIds.add(result.questionid);
                LOG.info("Creating question with qid {}", result.questionid);

            } catch (URISyntaxException | RuntimeException | InterruptedException | IOException ex) {
                LOG.error("Failed to create Question {}.", task.getId(), ex);
                //return CompletableFuture.completedFuture(Optional.empty());
            }
        }
        return CompletableFuture.completedFuture(Optional.of(moodleIds.stream().mapToInt(i -> i).toArray()));
    }

    private record Question(int questionid, String name) {
    }
}
