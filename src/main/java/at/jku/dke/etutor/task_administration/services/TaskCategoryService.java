package at.jku.dke.etutor.task_administration.services;

import at.jku.dke.etutor.task_administration.auth.AuthConstants;
import at.jku.dke.etutor.task_administration.auth.SecurityHelpers;
import at.jku.dke.etutor.task_administration.data.entities.TaskCategory;
import at.jku.dke.etutor.task_administration.data.repositories.OrganizationalUnitRepository;
import at.jku.dke.etutor.task_administration.data.repositories.TaskCategoryRepository;
import at.jku.dke.etutor.task_administration.dto.ModifyTaskCategoryDto;
import at.jku.dke.etutor.task_administration.dto.TaskCategoryDto;
import at.jku.dke.etutor.task_administration.moodle.QuestionCategoryService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

/**
 * This class provides methods for managing {@link TaskCategory}s.
 */
@Service
public class TaskCategoryService {
    private static final Logger LOG = LoggerFactory.getLogger(TaskCategoryService.class);

    private final TaskCategoryRepository repository;
    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final QuestionCategoryService questionCategoryService;

    /**
     * Creates a new instance of class {@link TaskCategoryService}.
     *
     * @param repository                   The task category repository.
     * @param organizationalUnitRepository The organizational unit repository.
     * @param questionCategoryService      The question category service.
     */
    public TaskCategoryService(TaskCategoryRepository repository, OrganizationalUnitRepository organizationalUnitRepository,
                               QuestionCategoryService questionCategoryService) {
        this.repository = repository;
        this.organizationalUnitRepository = organizationalUnitRepository;
        this.questionCategoryService = questionCategoryService;
    }

    //#region --- View ---

    /**
     * Returns all task categories for the requested page.
     *
     * @param page                     The page and sorting information.
     * @param nameFilter               Optional name filter string (applies contains to name attribute).
     * @param parentFilter             Optional parent filter (applies equals to parent attribute).
     * @param organizationalUnitFilter Optional organizational unit filter (applies equals to organizational unit attribute).
     * @return List of task categories
     */
    @Transactional(readOnly = true)
    public Page<TaskCategoryDto> getTaskCategories(Pageable page, String nameFilter, Long parentFilter, Long organizationalUnitFilter) {
        LOG.debug("Loading task categories for page {}", page);
        return this.repository.findAll(new FilterSpecification(nameFilter, parentFilter, organizationalUnitFilter), page).map(TaskCategoryDto::new);
    }

    /**
     * Returns the task category with the specified identifier.
     *
     * @param id The identifier.
     * @return The task category or an empty result if the task category does not exist.
     */
    @Transactional(readOnly = true)
    public Optional<TaskCategoryDto> getTaskCategory(long id) {
        LOG.debug("Loading task category {}", id);
        return this.repository.findOne(new SingleSpecification(id)).map(TaskCategoryDto::new);
    }

    //#endregion

    //#region --- Modify ---

    /**
     * Creates a new task category.
     *
     * @param dto The task category data.
     * @return The created task category.
     */
    @Transactional
    @PreAuthorize(AuthConstants.AUTHORITY_INSTRUCTOR_OR_ABOVE)
    public TaskCategory create(ModifyTaskCategoryDto dto) {
        if (!SecurityHelpers.isFullAdmin() && !SecurityHelpers.getOrganizationalUnitsAsAdminOrInstructor().contains(dto.organizationalUnitId()))
            throw new ValidationException("Unknown organizational unit");

        var taskCategory = new TaskCategory();
        if (dto.parentId() != null) {
            var parent = this.repository.findById(dto.parentId()).orElse(null);
            if (parent != null && !parent.getOrganizationalUnit().getId().equals(dto.organizationalUnitId()))
                throw new ValidationException("Child must belong to same organizational unit as parent");
            taskCategory.setParent(parent);
        }

        LOG.info("Creating task category {}", dto.name());
        taskCategory.setName(dto.name());
        taskCategory.setOrganizationalUnit(this.organizationalUnitRepository.getReferenceById(dto.organizationalUnitId()));
        taskCategory = this.repository.save(taskCategory);
        this.createMoodleObjectsForTaskCategory(taskCategory);

        return taskCategory;
    }

    /**
     * Updates an existing task category.
     *
     * @param id               The task category identifier.
     * @param dto              The new task category data.
     * @param concurrencyToken The concurrency token.
     * @throws ConcurrencyFailureException If the concurrency check failed.
     */
    @Transactional
    @PreAuthorize(AuthConstants.AUTHORITY_INSTRUCTOR_OR_ABOVE)
    public void update(long id, ModifyTaskCategoryDto dto, Instant concurrencyToken) {
        var taskCategory = this.repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Task category " + id + " does not exist."));
        if (concurrencyToken != null && taskCategory.getLastModifiedDate() != null && taskCategory.getLastModifiedDate().isAfter(concurrencyToken))
            throw new ConcurrencyFailureException("Task category has been modified in the meantime");
        if (!SecurityHelpers.isFullAdmin() && !SecurityHelpers.getOrganizationalUnitsAsAdminOrInstructor().contains(taskCategory.getOrganizationalUnit().getId()))
            throw new EntityNotFoundException();
        if (!SecurityHelpers.isFullAdmin() && !SecurityHelpers.getOrganizationalUnitsAsAdminOrInstructor().contains(dto.organizationalUnitId()))
            throw new ValidationException("Unknown organizational unit");

        if (dto.parentId() != null) {
            var parent = this.repository.findById(dto.parentId()).orElse(null);
            if (parent != null && !parent.getOrganizationalUnit().getId().equals(dto.organizationalUnitId()))
                throw new ValidationException("Child must belong to same organizational unit as parent");
        }

        LOG.info("Updating task category {}", id);
        taskCategory.setName(dto.name());
        taskCategory.setOrganizationalUnit(this.organizationalUnitRepository.getReferenceById(dto.organizationalUnitId()));
        taskCategory.setParent(dto.parentId() == null ? null : this.repository.getReferenceById(dto.parentId()));
        this.repository.save(taskCategory);
    }

    /**
     * Deletes the task category with the specified identifier.
     *
     * @param id The identifier of the task category to delete.
     */
    @Transactional
    @PreAuthorize(AuthConstants.AUTHORITY_INSTRUCTOR_OR_ABOVE)
    public void delete(long id) {
        var orgs = SecurityHelpers.getOrganizationalUnitsAsAdminOrInstructor();
        var taskCategory = this.repository.findById(id).orElse(null);
        if (taskCategory == null)
            return;

        if (SecurityHelpers.isFullAdmin() || orgs.contains(taskCategory.getOrganizationalUnit().getId())) {
            LOG.info("Deleting task category {}", id);
            this.repository.delete(taskCategory);
        }
    }

    //#endregion

    //#region --- Moodle ---

    /**
     * Called when task category has been created.
     *
     * @param category The task category.
     */
    public void createMoodleObjectsForTaskCategory(TaskCategory category) {
        if (category.getMoodleId() != null)
            return;

        this.questionCategoryService.createQuestionCategory(category).thenAccept(moodleId -> {
            if (moodleId.isPresent()) {
                category.setMoodleId(moodleId.get());
                this.repository.save(category);
            }
        });
    }

    //#endregion

    //#region --- Specifications ---
    private record FilterSpecification(String name, Long parentId, Long organizationalUnitId) implements Specification<TaskCategory> {

        @Override
        public Predicate toPredicate(Root<TaskCategory> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            var predicates = new ArrayList<Predicate>();

            // User-specified filters
            if (this.name != null)
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + this.name.toLowerCase() + "%"));
            if (this.parentId != null)
                predicates.add(criteriaBuilder.equal(root.get("parent").get("id"), this.parentId));
            if (this.organizationalUnitId != null)
                predicates.add(criteriaBuilder.equal(root.get("organizationalUnit").get("id"), this.organizationalUnitId));

            // Security related filters
            if (!SecurityHelpers.isFullAdmin()) {
                var orgs = SecurityHelpers.getOrganizationalUnitsAsAdminOrInstructor();
                predicates.add(criteriaBuilder.in(root.get("organizationalUnit").get("id")).value(orgs));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }

    }

    private record SingleSpecification(long id) implements Specification<TaskCategory> {

        @Override
        public Predicate toPredicate(Root<TaskCategory> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            var predicates = new ArrayList<Predicate>();

            // User-specified filters
            predicates.add(criteriaBuilder.equal(root.get("id"), this.id));

            // Security related filters
            if (!SecurityHelpers.isFullAdmin()) {
                var orgs = SecurityHelpers.getOrganizationalUnitsAsAdminOrInstructor();
                predicates.add(criteriaBuilder.in(root.get("organizationalUnit").get("id")).value(orgs));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }

    }
    //#endregion
}
