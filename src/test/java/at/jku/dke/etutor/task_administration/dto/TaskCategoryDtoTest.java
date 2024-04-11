package at.jku.dke.etutor.task_administration.dto;

import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnit;
import at.jku.dke.etutor.task_administration.data.entities.TaskCategory;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TaskCategoryDtoTest {
    @Test
    void constructor_withParent() {
        // Arrange
        var ou = new OrganizationalUnit();
        ou.setId(99L);

        var parent = new TaskCategory();
        parent.setId(1L);

        var category = new TaskCategory();
        category.setId(2L);
        category.setName("Test");
        category.setParent(parent);
        category.setOrganizationalUnit(ou);
        category.setMoodleId(190);
        category.setCreatedBy("creator");
        category.setCreatedDate(Instant.now().minusSeconds(60));
        category.setLastModifiedBy("modifier");
        category.setLastModifiedDate(Instant.now());

        // Act
        var dto = new TaskCategoryDto(category);

        // Assert
        assertEquals(category.getId(), dto.id());
        assertEquals(category.getName(), dto.name());
        assertEquals(category.getParent().getId(), dto.parentId());
        assertEquals(category.getOrganizationalUnit().getId(), dto.organizationalUnitId());
        assertEquals(category.getMoodleId() != null, dto.moodleSynced());
        assertEquals(category.getCreatedBy(), dto.createdBy());
        assertEquals(category.getCreatedDate(), dto.createdDate());
        assertEquals(category.getLastModifiedBy(), dto.lastModifiedBy());
        assertEquals(category.getLastModifiedDate(), dto.lastModifiedDate());
    }

    @Test
    void constructor_withoutParent() {
        // Arrange
        var ou = new OrganizationalUnit();
        ou.setId(99L);

        var category = new TaskCategory();
        category.setId(2L);
        category.setName("Test");
        category.setParent(null);
        category.setOrganizationalUnit(ou);
        category.setMoodleId(null);
        category.setCreatedBy("creator");
        category.setCreatedDate(Instant.now().minusSeconds(60));
        category.setLastModifiedBy("modifier");
        category.setLastModifiedDate(Instant.now());

        // Act
        var dto = new TaskCategoryDto(category);

        // Assert
        assertEquals(category.getId(), dto.id());
        assertEquals(category.getName(), dto.name());
        assertNull(dto.parentId());
        assertEquals(category.getOrganizationalUnit().getId(), dto.organizationalUnitId());
        assertEquals(category.getMoodleId() != null, dto.moodleSynced());
        assertEquals(category.getCreatedBy(), dto.createdBy());
        assertEquals(category.getCreatedDate(), dto.createdDate());
        assertEquals(category.getLastModifiedBy(), dto.lastModifiedBy());
        assertEquals(category.getLastModifiedDate(), dto.lastModifiedDate());
    }
}
