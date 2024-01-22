package at.jku.dke.etutor.task_administration.data.repositories;

import at.jku.dke.etutor.task_administration.data.entities.TaskApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Repository for entity {@link TaskApp}.
 */
public interface TaskAppRepository extends JpaRepository<TaskApp, Long>, JpaSpecificationExecutor<TaskApp> {
    /**
     * Returns the task app for the given task group type.
     *
     * @param taskType The task type.
     * @return The task app or an empty result.
     */
    Optional<TaskApp> findByTaskType(String taskType);

    /**
     * Returns whether a task app for the given task group type exists.
     *
     * @param taskType The task type.
     * @return {@code true} if a task app for the given task group type exists, {@code false} otherwise.
     */
    boolean existsByTaskType(String taskType);
}
