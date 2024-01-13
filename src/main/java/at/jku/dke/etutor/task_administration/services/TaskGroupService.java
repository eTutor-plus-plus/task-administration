package at.jku.dke.etutor.task_administration.services;

import at.jku.dke.etutor.task_administration.auth.SecurityHelpers;
import at.jku.dke.etutor.task_administration.data.entities.TaskGroup;
import at.jku.dke.etutor.task_administration.data.entities.TaskStatus;
import at.jku.dke.etutor.task_administration.data.repositories.OrganizationalUnitRepository;
import at.jku.dke.etutor.task_administration.data.repositories.TaskGroupRepository;
import at.jku.dke.etutor.task_administration.dto.CombinedDto;
import at.jku.dke.etutor.task_administration.dto.ModifyTaskGroupDto;
import at.jku.dke.etutor.task_administration.dto.TaskGroupDto;
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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class provides methods for managing {@link TaskGroup}s.
 */
@Service
public class TaskGroupService {
    private static final Logger LOG = LoggerFactory.getLogger(TaskGroupService.class);

    private final TaskGroupRepository repository;
    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final TaskAppCommunicationService taskAppCommunicationService;

    /**
     * Creates a new instance of class {@link TaskGroupService}.
     *
     * @param repository                   The task group repository.
     * @param organizationalUnitRepository The organizational unit repository.
     * @param taskAppCommunicationService  The task app communication service.
     */
    public TaskGroupService(TaskGroupRepository repository, OrganizationalUnitRepository organizationalUnitRepository, TaskAppCommunicationService taskAppCommunicationService) {
        this.repository = repository;
        this.organizationalUnitRepository = organizationalUnitRepository;
        this.taskAppCommunicationService = taskAppCommunicationService;
    }

    //#region --- View ---

    /**
     * Returns all task groups for the requested page.
     *
     * @param page                The page and sorting information.
     * @param nameFilter          Optional name filter string (applies contains to name attribute).
     * @param statusFilter        Optional status filter (applies equals to status attribute).
     * @param taskGroupTypeFilter Optional task group type filter (applies equals to task group type attribute).
     * @param orgUnitFilter       Optional organizational unit filter (applies equals to organizational unit attribute).
     * @return List of task groups
     */
    @Transactional(readOnly = true)
    public Page<TaskGroupDto> getTaskGroups(Pageable page, String nameFilter, TaskStatus statusFilter, String taskGroupTypeFilter, Long orgUnitFilter) {
        LOG.debug("Loading task groups for page {}", page);
        return this.repository.findAll(new FilterSpecification(nameFilter, statusFilter, taskGroupTypeFilter, orgUnitFilter), page).map(TaskGroupDto::new);
    }

    /**
     * Returns the task group with the specified identifier.
     *
     * @param id The identifier.
     * @return The task group or an empty result if the task group does not exist.
     */
    @Transactional(readOnly = true)
    public Optional<CombinedDto<TaskGroupDto>> getTaskGroup(long id) {
        LOG.debug("Loading task group {}", id);
        var dto = this.repository.findOne(new SingleSpecification(id)).map(TaskGroupDto::new);
        if (dto.isPresent()) {
            var additionalData = this.taskAppCommunicationService.getTaskGroupAdditionalData(dto.get().id(), dto.get().taskGroupType());
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
        LOG.debug("Loading task types for groups");
        return this.repository.findDistinctTaskGroupTypes();
    }

    //#endregion

    //#region --- Modify ---

    /**
     * Creates a new task group.
     *
     * @param dto The task group data.
     * @return The created task group.
     */
    @Transactional
    public TaskGroup create(ModifyTaskGroupDto dto) {
        if (!SecurityHelpers.isFullAdmin() && !SecurityHelpers.getOrganizationalUnits().contains(dto.organizationalUnitId()))
            throw new ValidationException("Unknown organizational unit");

        LOG.info("Creating task group {}", dto.name());
        var taskGroup = new TaskGroup();
        taskGroup.setName(dto.name());
        taskGroup.setOrganizationalUnit(this.organizationalUnitRepository.getReferenceById(dto.organizationalUnitId()));
        taskGroup.setTaskGroupType(dto.taskGroupType());
        taskGroup.setDescriptionDe(dto.descriptionDe());
        taskGroup.setDescriptionEn(dto.descriptionEn());

        if (SecurityHelpers.isTutor(dto.organizationalUnitId()))
            taskGroup.setStatus(dto.status().equals(TaskStatus.APPROVED) ? TaskStatus.DRAFT : dto.status());
        else {
            taskGroup.setStatus(dto.status());
            if (dto.status().equals(TaskStatus.APPROVED)) {
                taskGroup.setApprovedBy(SecurityHelpers.getUserName());
                taskGroup.setApprovedDate(OffsetDateTime.now());
            } else {
                taskGroup.setApprovedBy(null);
                taskGroup.setApprovedDate(null);
            }
        }

        taskGroup = this.repository.save(taskGroup);
        var result = this.taskAppCommunicationService.createTaskGroup(taskGroup.getId(), dto);
        if (result != null) {
            boolean modified = false;
            if (result.descriptionDe() != null && taskGroup.getDescriptionDe().trim().isBlank()) {
                taskGroup.setDescriptionDe(result.descriptionDe());
                modified = true;
            }
            if (result.descriptionEn() != null && taskGroup.getDescriptionEn().trim().isBlank()) {
                taskGroup.setDescriptionEn(result.descriptionEn());
                modified = true;
            }
            if (modified)
                this.repository.save(taskGroup);
        }

        return taskGroup;
    }

    /**
     * Updates an existing task group.
     *
     * @param id               The task group identifier.
     * @param dto              The new task group data.
     * @param concurrencyToken The concurrency token.
     * @throws ConcurrencyFailureException If the concurrency check failed.
     */
    @Transactional
    public void update(long id, ModifyTaskGroupDto dto, Instant concurrencyToken) {
        var taskGroup = this.repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Task group " + id + " does not exist."));
        if (concurrencyToken != null && taskGroup.getLastModifiedDate() != null && taskGroup.getLastModifiedDate().isAfter(concurrencyToken))
            throw new ConcurrencyFailureException("Task group has been modified in the meantime");
        if (!SecurityHelpers.isFullAdmin() && !SecurityHelpers.getOrganizationalUnits().contains(taskGroup.getOrganizationalUnit().getId()))
            throw new EntityNotFoundException();
        if (!SecurityHelpers.isFullAdmin() && !SecurityHelpers.getOrganizationalUnits().contains(dto.organizationalUnitId()))
            throw new ValidationException("Unknown organizational unit");
        if (taskGroup.getStatus().equals(TaskStatus.APPROVED) && SecurityHelpers.isTutor(taskGroup.getOrganizationalUnit().getId()))
            throw new InsufficientAuthenticationException("User is not allowed to modify the task group");
        if (!taskGroup.getTaskGroupType().equals(dto.taskGroupType()))
            throw new ValidationException("Changing the task group type is not supported.");

        LOG.info("Updating task group {}", id);
        taskGroup.setName(dto.name());
        taskGroup.setOrganizationalUnit(this.organizationalUnitRepository.getReferenceById(dto.organizationalUnitId()));
        taskGroup.setTaskGroupType(dto.taskGroupType());
        taskGroup.setDescriptionDe(dto.descriptionDe());
        taskGroup.setDescriptionEn(dto.descriptionEn());

        if (SecurityHelpers.isTutor(dto.organizationalUnitId()))
            taskGroup.setStatus(dto.status().equals(TaskStatus.APPROVED) ? TaskStatus.DRAFT : dto.status());
        else {
            if (!taskGroup.getStatus().equals(dto.status())) {
                taskGroup.setStatus(dto.status());
                if (dto.status().equals(TaskStatus.APPROVED)) {
                    taskGroup.setApprovedBy(SecurityHelpers.getUserName());
                    taskGroup.setApprovedDate(OffsetDateTime.now());
                } else {
                    taskGroup.setApprovedBy(null);
                    taskGroup.setApprovedDate(null);
                }
            }
        }

        var result = this.taskAppCommunicationService.updateTaskGroup(taskGroup.getId(), dto);
        if (result != null) {
            if (result.descriptionDe() != null && taskGroup.getDescriptionDe().trim().isBlank())
                taskGroup.setDescriptionDe(result.descriptionDe());
            if (result.descriptionEn() != null && taskGroup.getDescriptionEn().trim().isBlank())
                taskGroup.setDescriptionEn(result.descriptionEn());
        }
        this.repository.save(taskGroup);
    }

    /**
     * Deletes the task group with the specified identifier.
     *
     * @param id The identifier of the task group to delete.
     */
    @Transactional
    public void delete(long id) {
        var orgs = SecurityHelpers.getOrganizationalUnitsAsAdminOrInstructor();
        var taskGroup = this.repository.findById(id).orElse(null);
        if (taskGroup == null)
            return;

        if (SecurityHelpers.isFullAdmin() || orgs.contains(taskGroup.getOrganizationalUnit().getId())) {
            if (taskGroup.getStatus().equals(TaskStatus.APPROVED) && SecurityHelpers.isTutor(taskGroup.getOrganizationalUnit().getId()))
                throw new InsufficientAuthenticationException("User is not allowed to delete the task group");

            LOG.info("Deleting task group {}", id);
            this.taskAppCommunicationService.deleteTaskGroup(taskGroup.getId(), taskGroup.getTaskGroupType());
            this.repository.deleteById(id);
        }
    }

    //#endregion

    //#region --- Specifications ---
    private record FilterSpecification(String name, TaskStatus status, String taskGroupType, Long orgUnit) implements Specification<TaskGroup> {

        @Override
        public Predicate toPredicate(Root<TaskGroup> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            var predicates = new ArrayList<Predicate>();

            // User-specified filters
            if (this.name != null)
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), '%' + this.name.toLowerCase() + '%'));
            if (this.status != null)
                predicates.add(criteriaBuilder.equal(root.get("status"), criteriaBuilder.literal(this.status)));
            if (taskGroupType != null)
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("taskGroupType")), this.taskGroupType.toLowerCase()));
            if (orgUnit != null)
                predicates.add(criteriaBuilder.equal(root.get("organizationalUnit").get("id"), orgUnit));

            // Security related filters
            if (!SecurityHelpers.isFullAdmin()) {
                var orgs = SecurityHelpers.getOrganizationalUnits(); // TODO: prevent access also with ACLs
                predicates.add(criteriaBuilder.in(root.get("organizationalUnit").get("id")).value(orgs));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }

    }

    private record SingleSpecification(long id) implements Specification<TaskGroup> {

        @Override
        public Predicate toPredicate(Root<TaskGroup> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            var predicates = new ArrayList<Predicate>();

            // User-specified filters
            predicates.add(criteriaBuilder.equal(root.get("id"), this.id));

            // Security related filters
            if (!SecurityHelpers.isFullAdmin()) {
                var orgs = SecurityHelpers.getOrganizationalUnits();
                predicates.add(criteriaBuilder.in(root.get("organizationalUnit").get("id")).value(orgs));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }

    }
    //#endregion
}
