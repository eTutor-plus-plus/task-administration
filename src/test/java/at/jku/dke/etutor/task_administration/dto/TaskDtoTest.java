package at.jku.dke.etutor.task_administration.dto;

import at.jku.dke.etutor.task_administration.data.entities.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TaskDtoTest {

    @Test
    void constructor_withoutCategoryIds() {
        // Arrange
        var task = new Task();
        task.setId(16L);
        task.setOrganizationalUnit(new OrganizationalUnit(87L));
        task.setTitle("Test");
        task.setDescriptionDe("DE Test");
        task.setDescriptionEn("EN Test");
        task.setDifficulty((short) 1);
        task.setMaxPoints(BigDecimal.TEN);
        task.setTaskType("sql");
        task.setStatus(TaskStatus.APPROVED);
        task.setMoodleSynced(false);
        task.setCreatedBy("Creator");
        task.setCreatedDate(Instant.now().minusSeconds(60));
        task.setLastModifiedBy("Modifier");
        task.setLastModifiedDate(Instant.now().minusSeconds(30));
        task.setApprovedBy("Approval");
        task.setApprovedDate(OffsetDateTime.now());

        // Act
        var dto = new TaskDto(task);

        // Assert
        assertEquals(task.getId(), dto.id());
        assertEquals(task.getOrganizationalUnit().getId(), dto.organizationalUnitId());
        assertEquals(task.getTitle(), dto.title());
        assertEquals(task.getDescriptionDe(), dto.descriptionDe());
        assertEquals(task.getDescriptionEn(), dto.descriptionEn());
        assertEquals(task.getDifficulty(), dto.difficulty());
        assertEquals(task.getMaxPoints(), dto.maxPoints());
        assertEquals(task.getTaskType(), dto.taskType());
        assertEquals(task.getStatus(), dto.status());
        assertEquals(task.getTaskGroup() != null ? task.getTaskGroup().getId() : null, dto.taskGroupId());
        assertEquals(task.isMoodleSynced(), dto.moodleSynced());
        assertEquals(task.getCreatedBy(), dto.createdBy());
        assertEquals(task.getCreatedDate(), dto.createdDate());
        assertEquals(task.getLastModifiedBy(), dto.lastModifiedBy());
        assertEquals(task.getLastModifiedDate(), dto.lastModifiedDate());
        assertEquals(task.getApprovedBy(), dto.approvedBy());
        assertEquals(task.getApprovedDate(), dto.approvedDate());
        assertNull(dto.taskCategoryIds());
    }

    @Test
    void constructor_withCategoryIds() {
        // Arrange
        var tg = new TaskGroup();
        tg.setId(123L);

        var task = new Task();
        task.setId(16L);
        task.setOrganizationalUnit(new OrganizationalUnit(87L));
        task.setTaskGroup(tg);
        task.setTitle("Test");
        task.setDescriptionDe("DE Test");
        task.setDescriptionEn("EN Test");
        task.setDifficulty((short) 1);
        task.setMaxPoints(BigDecimal.TEN);
        task.setTaskType("sql");
        task.setStatus(TaskStatus.APPROVED);
        task.setMoodleSynced(false);
        task.setCreatedBy("Creator");
        task.setCreatedDate(Instant.now().minusSeconds(60));
        task.setLastModifiedBy("Modifier");
        task.setLastModifiedDate(Instant.now().minusSeconds(30));
        task.setApprovedBy("Approval");
        task.setApprovedDate(OffsetDateTime.now());

        var tc1 = new TaskCategory();
        tc1.setId(1L);
        var tcs = Set.of(tc1);

        // Act
        var dto = new TaskDto(task, tcs);

        // Assert
        assertEquals(task.getId(), dto.id());
        assertEquals(task.getOrganizationalUnit().getId(), dto.organizationalUnitId());
        assertEquals(task.getTitle(), dto.title());
        assertEquals(task.getDescriptionDe(), dto.descriptionDe());
        assertEquals(task.getDescriptionEn(), dto.descriptionEn());
        assertEquals(task.getDifficulty(), dto.difficulty());
        assertEquals(task.getMaxPoints(), dto.maxPoints());
        assertEquals(task.getTaskType(), dto.taskType());
        assertEquals(task.getStatus(), dto.status());
        assertEquals(task.getTaskGroup() != null ? task.getTaskGroup().getId() : null, dto.taskGroupId());
        assertEquals(task.isMoodleSynced(), dto.moodleSynced());
        assertEquals(task.getCreatedBy(), dto.createdBy());
        assertEquals(task.getCreatedDate(), dto.createdDate());
        assertEquals(task.getLastModifiedBy(), dto.lastModifiedBy());
        assertEquals(task.getLastModifiedDate(), dto.lastModifiedDate());
        assertEquals(task.getApprovedBy(), dto.approvedBy());
        assertEquals(task.getApprovedDate(), dto.approvedDate());
        assertEquals(1, dto.taskCategoryIds().size());
    }
}
