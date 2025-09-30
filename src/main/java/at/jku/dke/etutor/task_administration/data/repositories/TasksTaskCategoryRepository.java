package at.jku.dke.etutor.task_administration.data.repositories;

import at.jku.dke.etutor.task_administration.data.entities.TasksTaskCategory;
import at.jku.dke.etutor.task_administration.data.entities.TasksTaskCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TasksTaskCategoryRepository extends JpaRepository<TasksTaskCategory, TasksTaskCategoryId> {
    @Query("select t from TasksTaskCategory t where t.id.taskId = ?1")
    List<TasksTaskCategory> findById_TaskId(Long taskId);
}
