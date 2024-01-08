package at.jku.dke.etutor.task_administration.data.repositories;

import at.jku.dke.etutor.task_administration.data.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository for entity {@link Task}.
 */
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    /**
     * Returns all task types.
     *
     * @return All used task types.
     */
    @Query("SELECT DISTINCT t.taskType FROM Task t")
    List<String> findDistinctTaskTypes();

    /**
     * Returns the task with the given id and fetches the task categories.
     *
     * @param id The id of the task.
     * @return The task with the given id.
     */
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.taskCategories WHERE t.id = ?1")
    Optional<Task> findByIdAndTaskCategories(Long id);
}
