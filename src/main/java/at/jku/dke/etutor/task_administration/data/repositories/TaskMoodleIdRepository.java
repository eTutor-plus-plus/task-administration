package at.jku.dke.etutor.task_administration.data.repositories;

import at.jku.dke.etutor.task_administration.data.entities.TaskMoodleId;
import at.jku.dke.etutor.task_administration.data.entities.TaskMoodleIdId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for entity {@link TaskMoodleId}.
 */
public interface TaskMoodleIdRepository extends JpaRepository<TaskMoodleId, TaskMoodleIdId> {

    Optional<TaskMoodleId> findById_TaskIdAndId_TaskCategoryId(Long taskId, Long taskCategoryId);

    List<TaskMoodleId> findById_TaskId(Long taskId);

    void deleteByTaskId(Long TaskId);

}
