package at.jku.dke.etutor.task_administration.services;

import at.jku.dke.etutor.task_administration.auth.AuthConstants;
import at.jku.dke.etutor.task_administration.data.entities.TaskApp;
import at.jku.dke.etutor.task_administration.data.repositories.TaskAppRepository;
import at.jku.dke.etutor.task_administration.dto.ModifyTaskAppDto;
import at.jku.dke.etutor.task_administration.dto.TaskAppDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
 * This class provides methods for managing {@link TaskApp}s.
 */
@Service
@PreAuthorize(AuthConstants.AUTHORITY_FULL_ADMIN)
public class TaskAppService {
    private static final Logger LOG = LoggerFactory.getLogger(TaskAppService.class);

    private final TaskAppRepository repository;

    /**
     * Creates a new instance of class {@link TaskAppService}.
     *
     * @param repository The task app repository.
     */
    public TaskAppService(TaskAppRepository repository) {
        this.repository = repository;
    }

    //#region --- View ---

    /**
     * Returns all task apps for the requested page.
     *
     * @param page           The page and sorting information.
     * @param urlFilter      Optional URL filter string (applies contains to url attribute).
     * @param taskTypeFilter Optional task type filter string (applies contains to task type attribute).
     * @return List of task apps
     */
    @Transactional(readOnly = true)
    public Page<TaskAppDto> getTaskApps(Pageable page, String urlFilter, String taskTypeFilter) {
        LOG.debug("Loading task apps for page {}", page);
        return this.repository.findAll(new FilterSpecification(urlFilter, taskTypeFilter), page).map(TaskAppDto::new);
    }

    /**
     * Returns the task app with the specified identifier.
     *
     * @param id The identifier.
     * @return The task app or an empty result if the task app does not exist.
     */
    @Transactional(readOnly = true)
    public Optional<TaskAppDto> getTaskApp(long id) {
        LOG.debug("Loading task app {}", id);
        return this.repository.findById(id).map(TaskAppDto::new);
    }

    //#endregion

    //#region --- Modify ---

    /**
     * Creates a new task app.
     *
     * @param dto The task app data.
     * @return The created task app.
     */
    @Transactional
    public TaskApp create(ModifyTaskAppDto dto) {
        LOG.info("Creating task app {}", dto.url());

        var taskApp = new TaskApp();
        taskApp.setTaskType(dto.taskType());
        taskApp.setUrl(dto.url());
        taskApp.setApiKey(dto.apiKey());
        taskApp = this.repository.save(taskApp);

        return taskApp;
    }

    /**
     * Updates an existing task app.
     *
     * @param id               The task app identifier.
     * @param dto              The new task app data.
     * @param concurrencyToken The concurrency token.
     * @throws ConcurrencyFailureException If the concurrency check failed.
     */
    @Transactional
    public void update(long id, ModifyTaskAppDto dto, Instant concurrencyToken) {
        var taskApp = this.repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Task app " + id + " does not exist."));
        if (concurrencyToken != null && taskApp.getLastModifiedDate() != null && taskApp.getLastModifiedDate().isAfter(concurrencyToken))
            throw new ConcurrencyFailureException("Task app has been modified in the meantime");

        LOG.info("Updating task app {}", id);
        taskApp.setTaskType(dto.taskType());
        taskApp.setUrl(dto.url());
        taskApp.setApiKey(dto.apiKey());
        this.repository.save(taskApp);
    }

    /**
     * Deletes the task app with the specified identifier.
     *
     * @param id The identifier of the task app to delete.
     */
    @Transactional
    public void delete(long id) {
        LOG.info("Deleting task app {}", id);
        this.repository.deleteById(id);
    }

    //#endregion

    //#region --- Specifications ---
    private record FilterSpecification(String url, String taskType) implements Specification<TaskApp> {

        @Override
        public Predicate toPredicate(Root<TaskApp> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            var predicates = new ArrayList<Predicate>();

            // User-specified filters
            if (this.url != null)
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("url")), "%" + this.url.toLowerCase() + "%"));
            if (this.taskType != null)
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("taskType")), "%" + this.taskType.toLowerCase() + "%"));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }

    }
    //#endregion
}
