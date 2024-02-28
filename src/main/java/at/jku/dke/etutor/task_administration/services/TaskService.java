package at.jku.dke.etutor.task_administration.services;

import at.jku.dke.etutor.task_administration.auth.SecurityHelpers;
import at.jku.dke.etutor.task_administration.data.entities.Task;
import at.jku.dke.etutor.task_administration.data.entities.TaskCategory;
import at.jku.dke.etutor.task_administration.data.entities.TaskMoodleid;
import at.jku.dke.etutor.task_administration.data.entities.TaskStatus;
import at.jku.dke.etutor.task_administration.data.repositories.*;
import at.jku.dke.etutor.task_administration.dto.CombinedDto;
import at.jku.dke.etutor.task_administration.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_administration.dto.SubmitSubmissionDto;
import at.jku.dke.etutor.task_administration.dto.TaskDto;
import at.jku.dke.etutor.task_administration.moodle.QuestionCategoryService;
import at.jku.dke.etutor.task_administration.moodle.QuestionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * This class provides methods for managing {@link Task}s.
 */
@Service
public class TaskService {
    private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository repository;
    private final TaskGroupRepository taskGroupRepository;
    private final TaskCategoryRepository taskCategoryRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final TaskAppCommunicationService taskAppCommunicationService;

    private final TaskMoodleidRepository taskMoodleidRepository;

    private final QuestionService questionService;
    /**
     * Creates a new instance of class {@link TaskService}.
     *
     * @param repository                   The task repository.
     * @param taskGroupRepository          The task group repository.
     * @param taskCategoryRepository       The task category repository.
     * @param organizationalUnitRepository The organizational unit repository.
     * @param taskAppCommunicationService  The task app communication service.
     */
    public TaskService(TaskRepository repository, TaskGroupRepository taskGroupRepository, TaskCategoryRepository taskCategoryRepository,
                       OrganizationalUnitRepository organizationalUnitRepository, TaskAppCommunicationService taskAppCommunicationService, TaskMoodleidRepository taskMoodleidRepository, QuestionService questionService) {
        this.repository = repository;
        this.taskGroupRepository = taskGroupRepository;
        this.taskCategoryRepository = taskCategoryRepository;
        this.organizationalUnitRepository = organizationalUnitRepository;
        this.taskAppCommunicationService = taskAppCommunicationService;
        this.taskMoodleidRepository = taskMoodleidRepository;
        this.questionService = questionService;
    }

    //#region --- View ---

    /**
     * Returns all tasks for the requested page.
     *
     * @param page            The page and sorting information.
     * @param nameFilter      Optional name filter string (applies contains to name attribute).
     * @param statusFilter    Optional status filter (applies equals to status attribute).
     * @param taskTypeFilter  Optional task type filter (applies equals to task type attribute).
     * @param taskGroupFilter Optional task group filter (applies equals to task group attribute).
     * @param orgUnitFilter   Optional organizational unit filter (applies equals to organizational unit attribute).
     * @return List of tasks
     */
    @Transactional(readOnly = true)
    public Page<TaskDto> getTasks(Pageable page, String nameFilter, TaskStatus statusFilter, String taskTypeFilter, Long orgUnitFilter, Long taskGroupFilter) {
        LOG.debug("Loading tasks for page {}", page);
        return this.repository.findAll(new FilterSpecification(nameFilter, statusFilter, taskTypeFilter, orgUnitFilter, taskGroupFilter), page).map(TaskDto::new);
    }

    /**
     * Returns the task with the specified identifier.
     *
     * @param id The identifier.
     * @return The task or an empty result if the task does not exist.
     */
    @Transactional(readOnly = true)
    public Optional<CombinedDto<TaskDto>> getTask(long id) {
        LOG.debug("Loading task {}", id);
        var dto = this.repository.findOne(new SingleSpecification(id)).map(task -> new TaskDto(task, task.getTaskCategories()));
        if (dto.isPresent()) {
            var additionalData = this.taskAppCommunicationService.getTaskAdditionalData(dto.get().id(), dto.get().taskType());
            return Optional.of(new CombinedDto<>(dto.get(), additionalData));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns a list of all available task types.
     *
     * @return The task type list.
     */
    @Transactional(readOnly = true)
    public List<String> getTaskTypes() {
        LOG.debug("Loading task types for tasks");
        return this.repository.findDistinctTaskTypes();
    }

    //#endregion

    //#region --- Modify ---

    /**
     * Creates a new task.
     *
     * @param dto The task data.
     * @return The created task.
     */
    @Transactional
    public Task create(ModifyTaskDto dto) {
        if (!SecurityHelpers.isFullAdmin() && !SecurityHelpers.getOrganizationalUnits().contains(dto.organizationalUnitId()))
            throw new ValidationException("Unknown organizational unit");

        LOG.info("Creating task {}", dto.title());
        var task = new Task();
        task.setOrganizationalUnit(this.organizationalUnitRepository.getReferenceById(dto.organizationalUnitId()));
        task.setTitle(dto.title());
        task.setDescriptionDe(dto.descriptionDe());
        task.setDescriptionEn(dto.descriptionEn());
        task.setDifficulty(dto.difficulty());
        task.setMaxPoints(dto.maxPoints());
        task.setTaskType(dto.taskType());

        if (dto.taskGroupId() != null)
            task.setTaskGroup(this.taskGroupRepository.getReferenceById(dto.taskGroupId()));

        if (dto.taskCategoryIds() != null) {
            for (Long id : dto.taskCategoryIds()) {
                task.getTaskCategories().add(this.taskCategoryRepository.getReferenceById(id));
            }
        }

        if (SecurityHelpers.isTutor(dto.organizationalUnitId()))
            task.setStatus(dto.status().equals(TaskStatus.APPROVED) ? TaskStatus.DRAFT : dto.status());
        else {
            task.setStatus(dto.status());
            if (dto.status().equals(TaskStatus.APPROVED)) {
                task.setApprovedBy(SecurityHelpers.getUserName());
                task.setApprovedDate(OffsetDateTime.now());
            } else {
                task.setApprovedBy(null);
                task.setApprovedDate(null);
            }
        }

        task = this.repository.save(task);
        var result = this.taskAppCommunicationService.createTask(task.getId(), dto);
        if (result != null) {
            boolean modified = false;
            if (result.descriptionDe() != null && task.getDescriptionDe().trim().isBlank()) {
                task.setDescriptionDe(result.descriptionDe());
                modified = true;
            }
            if (result.descriptionEn() != null && task.getDescriptionEn().trim().isBlank()) {
                task.setDescriptionEn(result.descriptionEn());
                modified = true;
            }
            if (result.difficulty() != null && result.difficulty() >= 1 && result.difficulty() <= 4) {
                task.setDifficulty(result.difficulty());
                modified = true;
            }
            if (result.maxPoints() != null && result.maxPoints().compareTo(BigDecimal.ZERO) > 0) {
                task.setMaxPoints(result.maxPoints());
                modified = true;
            }
            if (modified)
                this.repository.save(task);


            //only syncing to moodle if the task is approved
            if(task.getStatus()==TaskStatus.APPROVED) {
                this.createMoodleObjectsForTaskCategory(task);
            }
        }

        return task;
    }
    @Transactional
    public void createMoodleObjectsForTaskCategory(Task task) {
        if (!taskMoodleidRepository.findById_TaskId(task.getId()).isEmpty())
            return;

        this.questionService.createQuestionFromTask(task).thenAccept(moodleIds -> {
            moodleIds.ifPresent(this.taskMoodleidRepository::saveAll);
        });
    }
    @Transactional
    public void updateMoodleObjectsForTaskCategory(Task task) {

        this.questionService.updateQuestionFromTask(task).thenAccept(moodleIds -> {
            if (moodleIds.isPresent()) {
                //this.taskMoodleidRepository.deleteByTaskId(task.getId());
                LOG.info("All tasks got from Task {} got deleted", task.getId());
                this.taskMoodleidRepository.saveAll(moodleIds.get());

            }
        });
    }



    /**
     * Updates an existing task.
     *
     * @param id               The task identifier.
     * @param dto              The new task data.
     * @param concurrencyToken The concurrency token.
     * @throws ConcurrencyFailureException If the concurrency check failed.
     */
    @Transactional
    public void update(long id, ModifyTaskDto dto, Instant concurrencyToken) {
        var task = this.repository.findByIdAndTaskCategories(id).orElseThrow(() -> new EntityNotFoundException("Task " + id + " does not exist."));
        if (concurrencyToken != null && task.getLastModifiedDate() != null && task.getLastModifiedDate().isAfter(concurrencyToken))
            throw new ConcurrencyFailureException("Task has been modified in the meantime");
        if (!SecurityHelpers.isFullAdmin() && !SecurityHelpers.getOrganizationalUnits().contains(task.getOrganizationalUnit().getId()))
            throw new EntityNotFoundException();
        if (!SecurityHelpers.isFullAdmin() && !SecurityHelpers.getOrganizationalUnits().contains(dto.organizationalUnitId()))
            throw new ValidationException("Unknown organizational unit");
        if (task.getStatus().equals(TaskStatus.APPROVED) && SecurityHelpers.isTutor(task.getOrganizationalUnit().getId()))
            throw new InsufficientAuthenticationException("User is not allowed to modify the task");
        if (!task.getTaskType().equals(dto.taskType()))
            throw new ValidationException("Changing the task type is not supported.");

        LOG.info("Updating task {}", id);
        task.setOrganizationalUnit(this.organizationalUnitRepository.getReferenceById(dto.organizationalUnitId()));
        task.setTitle(dto.title());
        task.setDescriptionDe(dto.descriptionDe());
        task.setDescriptionEn(dto.descriptionEn());
        task.setDifficulty(dto.difficulty());
        task.setMaxPoints(dto.maxPoints());
        task.setTaskType(dto.taskType());
        task.setTaskGroup(dto.taskGroupId() == null ? null : this.taskGroupRepository.getReferenceById(dto.taskGroupId()));


        if (dto.taskCategoryIds() != null) {
            var toRemove = task.getTaskCategories().stream().filter(x -> !dto.taskCategoryIds().contains(x.getId())).toList();
            var toAdd = dto.taskCategoryIds().stream()
                .filter(x -> task.getTaskCategories().stream().noneMatch(y -> y.getId().equals(x)))
                .map(this.taskCategoryRepository::getReferenceById)
                .toList();
            toRemove.forEach(task.getTaskCategories()::remove);
            toAdd.forEach(task.getTaskCategories()::add);
        } else {
            task.getTaskCategories().clear();
        }

        if (SecurityHelpers.isTutor(dto.organizationalUnitId()))
            task.setStatus(dto.status().equals(TaskStatus.APPROVED) ? TaskStatus.DRAFT : dto.status());
        else {
            if (!task.getStatus().equals(dto.status())) {
                task.setStatus(dto.status());
                if (dto.status().equals(TaskStatus.APPROVED)) {
                    task.setApprovedBy(SecurityHelpers.getUserName());
                    task.setApprovedDate(OffsetDateTime.now());
                } else {
                    task.setApprovedBy(null);
                    task.setApprovedDate(null);
                }
            }
        }

        var result = this.taskAppCommunicationService.updateTask(task.getId(), dto);
        if (result != null) {
            if (result.descriptionDe() != null && task.getDescriptionDe().trim().isBlank())
                task.setDescriptionDe(result.descriptionDe());
            if (result.descriptionEn() != null && task.getDescriptionEn().trim().isBlank())
                task.setDescriptionEn(result.descriptionEn());
            if (result.difficulty() != null && result.difficulty() >= 1 && result.difficulty() <= 4)
                task.setDifficulty(result.difficulty());
            if (result.maxPoints() != null && result.maxPoints().compareTo(BigDecimal.ZERO) > 0)
                task.setMaxPoints(result.maxPoints());
        }

        this.repository.save(task);


        if(task.getStatus()==TaskStatus.APPROVED) {
            this.updateMoodleObjectsForTaskCategory(task);
        }
    }

    /**
     * Deletes the task with the specified identifier.
     *
     * @param id The identifier of the task to delete.
     */
    @Transactional
    public void delete(long id) {
        var orgUnit = SecurityHelpers.getOrganizationalUnitsAsAdminOrInstructor();
        var task = this.repository.findById(id).orElse(null);
        if (task == null)
            return;

        if (SecurityHelpers.isFullAdmin() || orgUnit.contains(task.getOrganizationalUnit().getId())) {
            if (task.getStatus().equals(TaskStatus.APPROVED) && SecurityHelpers.isTutor(task.getOrganizationalUnit().getId()))
                throw new InsufficientAuthenticationException("User is not allowed to delete the task");

            LOG.info("Deleting task {}", id);
            this.taskAppCommunicationService.deleteTask(task.getId(), task.getTaskType());
            this.repository.deleteById(id);
        }
    }

    /**
     * Submits the specified submission.
     *
     * @param submission The submission.
     * @return The submission response.
     */
    public Serializable submit(SubmitSubmissionDto submission) {
        var task = this.repository.findById(submission.taskId()).orElseThrow(() -> new EntityNotFoundException("Task with id " + submission.taskId() + " does not exist"));
        if (!SecurityHelpers.isFullAdmin() && !SecurityHelpers.getOrganizationalUnits().contains(task.getOrganizationalUnit().getId()))
            throw new EntityNotFoundException("Task with id " + submission.taskId() + " does not exist");

        LOG.info("Submitting task {}", submission.taskId());
        return this.taskAppCommunicationService.submit(task.getTaskType(), submission);
    }

    //#endregion

    //#region --- Specifications ---
    private record FilterSpecification(String name, TaskStatus status, String taskType, Long orgUnit, Long taskGroup) implements Specification<Task> {

        @Override
        public Predicate toPredicate(Root<Task> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            var predicates = new ArrayList<Predicate>();

            // User-specified filters
            if (this.name != null)
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), '%' + this.name.toLowerCase() + '%'));
            if (this.status != null)
                predicates.add(criteriaBuilder.equal(root.get("status"), criteriaBuilder.literal(this.status)));
            if (taskType != null)
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("taskType")), this.taskType.toLowerCase()));
            if (orgUnit != null)
                predicates.add(criteriaBuilder.equal(root.get("organizationalUnit").get("id"), orgUnit));
            if (taskGroup != null)
                predicates.add(criteriaBuilder.equal(root.get("taskGroup").get("id"), taskGroup));

            // Security related filters
            if (!SecurityHelpers.isFullAdmin()) {
                var orgUnits = SecurityHelpers.getOrganizationalUnits();
                predicates.add(criteriaBuilder.in(root.get("organizationalUnit").get("id")).value(orgUnits));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }

    }

    private record SingleSpecification(long id) implements Specification<Task> {

        @Override
        public Predicate toPredicate(Root<Task> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            var predicates = new ArrayList<Predicate>();

            // User-specified filters
            predicates.add(criteriaBuilder.equal(root.get("id"), this.id));

            // Security related filters
            if (!SecurityHelpers.isFullAdmin()) {
                var orgUnits = SecurityHelpers.getOrganizationalUnits();
                predicates.add(criteriaBuilder.in(root.get("organizationalUnit").get("id")).value(orgUnits));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }

    }
    //#endregion
}
