package at.jku.dke.etutor.task_administration.dto;

import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnit;
import at.jku.dke.etutor.task_administration.data.entities.TaskGroup;
import at.jku.dke.etutor.task_administration.data.entities.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskGroupDtoTest {

    @Test
    void constructor() {
        // Arrange
        var group = new TaskGroup();
        group.setId(1L);
        group.setName("Test");
        group.setDescriptionDe("EN Test");
        group.setDescriptionEn("DE Test");
        group.setTaskGroupType("sql");
        group.setStatus(TaskStatus.APPROVED);
        group.setOrganizationalUnit(new OrganizationalUnit(5L));
        group.setCreatedBy("Creator");
        group.setCreatedDate(Instant.now().minusSeconds(60));
        group.setLastModifiedBy("Modifier");
        group.setLastModifiedDate(Instant.now().minusSeconds(30));
        group.setApprovedBy("Approval");
        group.setApprovedDate(OffsetDateTime.now());

        // Act
        var dto = new TaskGroupDto(group);

        // Assert
        assertEquals(group.getId(), dto.id());
        assertEquals(group.getName(), dto.name());
        assertEquals(group.getDescriptionDe(), dto.descriptionDe());
        assertEquals(group.getDescriptionEn(), dto.descriptionEn());
        assertEquals(group.getTaskGroupType(), dto.taskGroupType());
        assertEquals(group.getStatus(), dto.status());
        assertEquals(group.getOrganizationalUnit().getId(), dto.organizationalUnitId());
        assertEquals(group.getCreatedBy(), dto.createdBy());
        assertEquals(group.getCreatedDate(), dto.createdDate());
        assertEquals(group.getLastModifiedBy(), dto.lastModifiedBy());
        assertEquals(group.getLastModifiedDate(), dto.lastModifiedDate());
        assertEquals(group.getApprovedBy(), dto.approvedBy());
        assertEquals(group.getApprovedDate(), dto.approvedDate());
    }
}
