package at.jku.dke.etutor.task_administration.data.repositories;

import at.jku.dke.etutor.task_administration.data.entities.TaskGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository for entity {@link TaskGroup}.
 */
public interface TaskGroupRepository extends JpaRepository<TaskGroup, Long>, JpaSpecificationExecutor<TaskGroup> {
    /**
     * Returns all task group types.
     *
     * @return All used task group types.
     */
    @Query("SELECT DISTINCT t.taskGroupType FROM TaskGroup t")
    List<String> findDistinctTaskGroupTypes();
}
