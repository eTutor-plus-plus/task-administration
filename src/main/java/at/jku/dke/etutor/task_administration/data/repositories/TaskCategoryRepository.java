package at.jku.dke.etutor.task_administration.data.repositories;

import at.jku.dke.etutor.task_administration.data.entities.TaskCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Set;

/**
 * Repository for entity {@link TaskCategory}.
 */
public interface TaskCategoryRepository extends JpaRepository<TaskCategory, Long>, JpaSpecificationExecutor<TaskCategory> {
    /**
     * Finds all task categories by the ID of a task.
     *
     * @param id The ID of the task.
     * @return The list of task categories.
     */
    Set<TaskCategory> findByTasks_Id(Long id);
}
