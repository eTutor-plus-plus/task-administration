package at.jku.dke.etutor.task_administration.data.repositories;

import at.jku.dke.etutor.task_administration.data.entities.TaskGroup;
import at.jku.dke.etutor.task_administration.data.entities.TaskMoodleid;
import at.jku.dke.etutor.task_administration.data.entities.TaskMoodleidId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for entity {@link TaskMoodleid}.
 */
public interface TaskMoodleidRepository extends JpaRepository<TaskMoodleid, TaskMoodleidId> {


    Optional<TaskMoodleid> findById_TaskIdAndId_TaskCategoryId(Long taskId, Long taskCategoryId);

    List<TaskMoodleid> findById_TaskId(Long taskId);

    void deleteByTaskId(Long TaskId);

}
