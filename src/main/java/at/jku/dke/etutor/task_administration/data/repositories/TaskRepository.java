package at.jku.dke.etutor.task_administration.data.repositories;

import at.jku.dke.etutor.task_administration.data.entities.Task;
import at.jku.dke.etutor.task_administration.dto.TaskCategoryFeedbackLevelDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * Returns the task with the given id and fetches the organizational unit.
     *
     * @param id The id of the task.
     * @return The task with the given id.
     */
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.organizationalUnit WHERE t.id = ?1")
    Optional<Task> findByIdAndOrganizationalUnit(Long id);

    /**
     * Returns the raw feedback levels of the task in all of its task categories.
     *
     * @param taskId The id of the task.
     * @return Rows of {@code [task_category_id, submit_feedback_level, diagnose_feedback_level]}.
     */
    @Query(value = "SELECT task_category_id, submit_feedback_level, diagnose_feedback_level FROM tasks_task_categories WHERE task_id = :taskId", nativeQuery = true)
    List<Object[]> findRawFeedbackLevelsByTaskId(@Param("taskId") long taskId);

    /**
     * Returns the feedback levels of the task in all of its task categories.
     *
     * @param taskId The id of the task.
     * @return The feedback levels.
     */
    default List<TaskCategoryFeedbackLevelDto> findFeedbackLevelsByTaskId(long taskId) {
        return this.findRawFeedbackLevelsByTaskId(taskId).stream()
            .map(row -> new TaskCategoryFeedbackLevelDto(
                ((Number) row[0]).longValue(),
                ((Number) row[1]).shortValue(),
                ((Number) row[2]).shortValue()))
            .toList();
    }

    /**
     * Updates the feedback levels of the task in the given task category.
     *
     * @param taskId         The id of the task.
     * @param taskCategoryId The id of the task category.
     * @param submit         The feedback level for mode submit.
     * @param diagnose       The feedback level for mode diagnose.
     * @return The number of updated rows.
     */
    @Modifying
    @Query(value = "UPDATE tasks_task_categories SET submit_feedback_level = :submit, diagnose_feedback_level = :diagnose WHERE task_id = :taskId AND task_category_id = :taskCategoryId", nativeQuery = true)
    int updateFeedbackLevels(@Param("taskId") long taskId, @Param("taskCategoryId") long taskCategoryId, @Param("submit") short submit, @Param("diagnose") short diagnose);
}
