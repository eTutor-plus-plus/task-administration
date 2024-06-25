package at.jku.dke.etutor.task_administration.services;

import at.jku.dke.etutor.task_administration.auth.SecurityHelpers;
import at.jku.dke.etutor.task_administration.data.entities.Task;
import at.jku.dke.etutor.task_administration.data.entities.TaskGroup;
import at.jku.dke.etutor.task_administration.data.entities.TaskMoodleId;
import at.jku.dke.etutor.task_administration.data.entities.TaskStatus;
import at.jku.dke.etutor.task_administration.data.repositories.*;
import at.jku.dke.etutor.task_administration.dto.CombinedDto;
import at.jku.dke.etutor.task_administration.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_administration.dto.SubmitSubmissionDto;
import at.jku.dke.etutor.task_administration.dto.TaskDto;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private final TaskMoodleIdRepository taskMoodleIdRepository;

    private final QuestionService questionService;

    /**
     * Creates a new instance of class {@link TaskService}.
     *
     * @param repository                   The task repository.
     * @param taskGroupRepository          The task group repository.
     * @param taskCategoryRepository       The task category repository.
     * @param organizationalUnitRepository The organizational unit repository.
     * @param taskAppCommunicationService  The task app communication service.
     * @param taskMoodleIdRepository       The MoodleId Repository for Tasks.
     * @param questionService              The Question Service.
     */
    public TaskService(TaskRepository repository, TaskGroupRepository taskGroupRepository, TaskCategoryRepository taskCategoryRepository, OrganizationalUnitRepository organizationalUnitRepository, TaskAppCommunicationService taskAppCommunicationService, TaskMoodleIdRepository taskMoodleIdRepository, QuestionService questionService) {
        this.repository = repository;
        this.taskGroupRepository = taskGroupRepository;
        this.taskCategoryRepository = taskCategoryRepository;
        this.organizationalUnitRepository = organizationalUnitRepository;
        this.taskAppCommunicationService = taskAppCommunicationService;
        this.taskMoodleIdRepository = taskMoodleIdRepository;
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
        return this.repository
            .findAll(new FilterSpecification(nameFilter, statusFilter, taskTypeFilter, orgUnitFilter, taskGroupFilter), page)
            .map(task -> {
                if (task.isExamTask() && SecurityHelpers.isTutor(task.getOrganizationalUnit().getId()))
                    return null;
                return new TaskDto(task);
            });
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
            if (dto.get().examTask() && SecurityHelpers.isTutor(dto.get().organizationalUnitId()))
                throw new EntityNotFoundException();

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

    /**
     * Returns details of all task groups.
     *
     * @return List of all task groups with details
     */
    @Transactional(readOnly = true)
    public List<CombinedDto<TaskDto>> export() {
        LOG.debug("Exporting tasks");
        var tasks = this.repository.findAll(new FilterSpecification(null, null, null, null, null));
        List<CombinedDto<TaskDto>> result = new ArrayList<>(tasks.size());
        for (var task : tasks) {
            if (SecurityHelpers.isFullAdmin() || SecurityHelpers.isAdmin(task.getOrganizationalUnit().getId())) {
                try {
                    var details = this.taskAppCommunicationService.getTaskAdditionalData(task.getId(), task.getTaskType());
                    result.add(new CombinedDto<>(new TaskDto(task), details));
                } catch (ResponseStatusException ex) {
                    result.add(new CombinedDto<>(new TaskDto(task), null));
                }
            }
        }
        return result;
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
        if (!SecurityHelpers.isFullAdmin() && !SecurityHelpers.getOrganizationalUnits().contains(dto.organizationalUnitId())) {
            LOG.warn("User {} tried to create a task for organizational unit {}.", SecurityHelpers.getUserId(), dto.organizationalUnitId());
            throw new ValidationException("Unknown organizational unit");
        }

        LOG.info("Creating task {}", dto.title());
        var task = new Task();
        TaskGroup tg = null;
        task.setOrganizationalUnit(this.organizationalUnitRepository.getReferenceById(dto.organizationalUnitId()));
        task.setTitle(dto.title());
        task.setDescriptionDe(dto.descriptionDe());
        task.setDescriptionEn(dto.descriptionEn());
        task.setDifficulty(dto.difficulty());
        task.setMaxPoints(dto.maxPoints());
        task.setTaskType(dto.taskType());
        task.setExamTask(dto.examTask());
        if (dto.taskGroupId() != null) {
            tg = this.taskGroupRepository.findById(dto.taskGroupId()).orElse(null);
            task.setTaskGroup(tg);
        }

        if (dto.taskCategoryIds() != null) {
            for (Long id : dto.taskCategoryIds()) {
                task.getTaskCategories().add(this.taskCategoryRepository.getReferenceById(id));
            }
        }

        if (SecurityHelpers.isTutor(dto.organizationalUnitId())) {
            task.setStatus(dto.status().equals(TaskStatus.APPROVED) ? TaskStatus.DRAFT : dto.status());
            task.setExamTask(false);
        } else {
            if (tg != null && tg.getStatus() != TaskStatus.APPROVED && dto.status() == TaskStatus.APPROVED)
                task.setStatus(TaskStatus.READY_FOR_APPROVAL);
            else
                task.setStatus(dto.status());
            if (dto.status().equals(TaskStatus.APPROVED)) {
                task.setApprovedBy(SecurityHelpers.getUserName());
                task.setApprovedDate(OffsetDateTime.now());
            } else {
                task.setApprovedBy(null);
                task.setApprovedDate(null);
            }
        }
        task.setMoodleSynced(task.getTaskCategories().isEmpty() || task.getStatus() != TaskStatus.APPROVED);

        task = this.repository.save(task);
        var result = this.taskAppCommunicationService.createTask(task.getId(), dto);
        if (result != null) {
            boolean modified = false;
            if (result.descriptionDe() != null && (task.getDescriptionDe().trim().isEmpty() || Pattern.matches("<p>[\\s\\r\\n]*</p>", task.getDescriptionDe()))) {
                LOG.debug("Setting german description for task {} with value received from task app", task.getId());
                task.setDescriptionDe(result.descriptionDe());
                modified = true;
            }
            if (result.descriptionEn() != null && (task.getDescriptionEn().trim().isEmpty() || Pattern.matches("<p>[\\s\\r\\n]*</p>", task.getDescriptionEn()))) {
                LOG.debug("Setting english description for task {} with value received from task app", task.getId());
                task.setDescriptionEn(result.descriptionEn());
                modified = true;
            }
            if (result.difficulty() != null && result.difficulty() >= 1 && result.difficulty() <= 4) {
                LOG.debug("Setting difficulty for task {} with value received from task app", task.getId());
                task.setDifficulty(result.difficulty());
                modified = true;
            }
            if (result.maxPoints() != null && result.maxPoints().compareTo(BigDecimal.ZERO) > 0) {
                LOG.debug("Setting maximum points for task {} with value received from task app", task.getId());
                task.setMaxPoints(result.maxPoints());
                modified = true;
            }
            if (modified)
                this.repository.save(task);

            // only syncing to moodle if the task is approved
            if (task.getStatus() == TaskStatus.APPROVED) {
                this.createMoodleObjectsForTask(task);
            }
        }

        return task;
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
        if (!SecurityHelpers.isFullAdmin() && !SecurityHelpers.getOrganizationalUnits().contains(task.getOrganizationalUnit().getId())) {
            LOG.warn("User {} tried to update task {}", SecurityHelpers.getUserId(), id);
            throw new EntityNotFoundException();
        }
        if (!SecurityHelpers.isFullAdmin() && !SecurityHelpers.getOrganizationalUnits().contains(dto.organizationalUnitId())) {
            LOG.warn("User {} tried to move task {} to organizational unit {}", SecurityHelpers.getUserId(), id, dto.organizationalUnitId());
            throw new ValidationException("Unknown organizational unit");
        }
        if (task.getStatus().equals(TaskStatus.APPROVED) && SecurityHelpers.isTutor(task.getOrganizationalUnit().getId())) {
            LOG.warn("User {} tried to update approved task {}", SecurityHelpers.getUserId(), id);
            throw new InsufficientAuthenticationException("User is not allowed to modify the task");
        }
        if (task.isExamTask() && SecurityHelpers.isTutor(task.getOrganizationalUnit().getId())) {
            LOG.warn("User {} tried to update exam task {}", SecurityHelpers.getUserId(), id);
            throw new InsufficientAuthenticationException("User is not allowed to modify the task");
        }
        if (!task.getTaskType().equals(dto.taskType()))
            throw new ValidationException("Changing the task type is not supported.");
        if (concurrencyToken != null && task.getLastModifiedDate() != null && task.getLastModifiedDate().isAfter(concurrencyToken)) {
            LOG.debug("A user tried to update task with ID {} but the concurrency token expired", id);
            throw new ConcurrencyFailureException("Task has been modified in the meantime");
        }

        LOG.info("Updating task {}", id);
        TaskGroup tg = null;
        task.setOrganizationalUnit(this.organizationalUnitRepository.getReferenceById(dto.organizationalUnitId()));
        task.setTitle(dto.title());
        task.setDescriptionDe(dto.descriptionDe());
        task.setDescriptionEn(dto.descriptionEn());
        task.setDifficulty(dto.difficulty());
        task.setMaxPoints(dto.maxPoints());
        task.setTaskType(dto.taskType());
        task.setExamTask(dto.examTask());
        if (dto.taskGroupId() != null) {
            tg = this.taskGroupRepository.findById(dto.taskGroupId()).orElse(null);
            task.setTaskGroup(tg);
        }

        if (dto.taskCategoryIds() != null) {
            var toRemove = task.getTaskCategories().stream().filter(x -> !dto.taskCategoryIds().contains(x.getId())).toList();
            var toAdd = dto.taskCategoryIds().stream().filter(x -> task.getTaskCategories().stream().noneMatch(y -> y.getId().equals(x))).map(this.taskCategoryRepository::getReferenceById).toList();
            toRemove.forEach(task.getTaskCategories()::remove);
            toAdd.forEach(task.getTaskCategories()::add);
        } else {
            task.getTaskCategories().clear();
        }

        if (SecurityHelpers.isTutor(dto.organizationalUnitId())) {
            task.setStatus(dto.status().equals(TaskStatus.APPROVED) ? TaskStatus.DRAFT : dto.status());
            task.setExamTask(false);
        } else {
            if (!task.getStatus().equals(dto.status())) {
                if (tg != null && tg.getStatus() != TaskStatus.APPROVED && dto.status() == TaskStatus.APPROVED)
                    task.setStatus(TaskStatus.READY_FOR_APPROVAL);
                else
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
        task.setMoodleSynced(task.getTaskCategories().isEmpty() || task.getStatus() != TaskStatus.APPROVED);

        var result = this.taskAppCommunicationService.updateTask(task.getId(), dto);
        if (result != null) {
            if (result.descriptionDe() != null && (task.getDescriptionDe().trim().isEmpty() || Pattern.matches("<p>[\\s\\r\\n]*</p>", task.getDescriptionDe()))) {
                LOG.debug("Setting german description for task {} with value received from task app", task.getId());
                task.setDescriptionDe(result.descriptionDe());
            }
            if (result.descriptionEn() != null && (task.getDescriptionEn().trim().isEmpty() || Pattern.matches("<p>[\\s\\r\\n]*</p>", task.getDescriptionEn()))) {
                LOG.debug("Setting english description for task {} with value received from task app", task.getId());
                task.setDescriptionEn(result.descriptionEn());
            }
            if (result.difficulty() != null && result.difficulty() >= 1 && result.difficulty() <= 4) {
                LOG.debug("Setting difficulty for task {} with value received from task app", task.getId());
                task.setDifficulty(result.difficulty());
            }
            if (result.maxPoints() != null && result.maxPoints().compareTo(BigDecimal.ZERO) > 0) {
                LOG.debug("Setting maximum points for task {} with value received from task app", task.getId());
                task.setMaxPoints(result.maxPoints());
            }
        }

        this.repository.save(task);

        // only syncing to moodle if the task is approved
        if (task.getStatus() == TaskStatus.APPROVED) {
            this.updateMoodleObjectsForTask(task.getId());
        }
    }

    /**
     * Deletes the task with the specified identifier.
     *
     * @param id The identifier of the task to delete.
     */
    @Transactional
    public void delete(long id) {
        var task = this.repository.findById(id).orElse(null);
        if (task == null)
            return;

        var orgUnit = SecurityHelpers.getOrganizationalUnits();
        if (SecurityHelpers.isFullAdmin() || orgUnit.contains(task.getOrganizationalUnit().getId())) {
            if ((task.getStatus().equals(TaskStatus.APPROVED) || task.isExamTask()) && SecurityHelpers.isTutor(task.getOrganizationalUnit().getId())) {
                LOG.warn("User {} tried to delete approved task {}", SecurityHelpers.getUserId(), id);
                throw new InsufficientAuthenticationException("User is not allowed to delete the task");
            }

            LOG.info("Deleting task {}", id);
            this.taskAppCommunicationService.deleteTask(task.getId(), task.getTaskType());
            this.questionService.markQuestionAsDeleted(task);
            this.repository.deleteById(id);
        } else {
            LOG.warn("User {} tried to delete task {}", SecurityHelpers.getUserId(), id);
            throw new InsufficientAuthenticationException("User is not allowed to delete the task");
        }
    }

    /**
     * Called when a task has been created.
     *
     * @param task The task.
     */
    @Transactional
    public void createMoodleObjectsForTask(Task task) {
        if (!taskMoodleIdRepository.findById_TaskId(task.getId()).isEmpty())
            return;

        LOG.debug("Triggering question creation for task {}", task.getId());
        this.questionService.createQuestionFromTask(task).thenAccept(moodleIds -> {
            if (moodleIds.isPresent() && !moodleIds.get().isEmpty()) {
                LOG.info("Setting moodle-ids for task {} to {}", task.getId(), moodleIds.get().stream().map(TaskMoodleId::getMoodleId).map(x -> x + "").collect(Collectors.joining(",")));
                this.taskMoodleIdRepository.saveAll(moodleIds.get()); // moodleSync flag of task is updated in databases by trigger
            }
        }).exceptionally(ex -> {
            LOG.error("Error while creating Moodle objects for task {}", task.getId(), ex);
            return null;
        });
    }

    /**
     * Called when a task has been updated.
     *
     * @param id The task id.
     * @throws EntityNotFoundException If the task does not exist.
     */
    @Transactional
    public void updateMoodleObjectsForTask(long id) {
        var task = this.repository.findByIdAndOrganizationalUnit(id).orElseThrow(() -> new EntityNotFoundException("Task " + id + " does not exist."));
        LOG.debug("Triggering question update for task {}", task.getId());
        this.questionService.updateQuestionFromTask(task).thenAccept(moodleIds -> {
            LOG.debug("Deleting all moodle ids for task {}", task.getId());
            this.taskMoodleIdRepository.deleteByTaskId(task.getId());

            if (moodleIds.isPresent() && !moodleIds.get().isEmpty()) {
                LOG.info("Setting moodle-ids for task {} to {}", task.getId(), moodleIds.get().stream().map(TaskMoodleId::getMoodleId).map(x -> x + "").collect(Collectors.joining(",")));
                this.taskMoodleIdRepository.saveAll(moodleIds.get());
            }  // moodleSync flag of task is updated in databases by trigger
        }).exceptionally(ex -> {
            LOG.error("Error while updating Moodle objects for task {}", id, ex);
            return null;
        });
    }

    /**
     * Marks the task as deleted in Moodle.
     *
     * @param id The identifier of the task.
     */
    @Async
    public void markMoodleObjectsForTaskAsDeleted(long id) {
        var task = this.repository.findById(id);
        task.ifPresent(this.questionService::markQuestionAsDeleted);
    }

    //#endregion

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

    //#region --- Specifications ---
    private record FilterSpecification(String name, TaskStatus status, String taskType, Long orgUnit, Long taskGroup) implements Specification<Task> {

        @Override
        public Predicate toPredicate(Root<Task> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            var predicates = new ArrayList<Predicate>();

            // User-specified filters
            if (this.name != null) predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), '%' + this.name.toLowerCase() + '%'));
            if (this.status != null) predicates.add(criteriaBuilder.equal(root.get("status"), criteriaBuilder.literal(this.status)));
            if (this.taskType != null) predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("taskType")), this.taskType.toLowerCase()));
            if (this.orgUnit != null) predicates.add(criteriaBuilder.equal(root.get("organizationalUnit").get("id"), this.orgUnit));
            if (this.taskGroup != null) predicates.add(criteriaBuilder.equal(root.get("taskGroup").get("id"), this.taskGroup));

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
