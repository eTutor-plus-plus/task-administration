package at.jku.dke.etutor.task_administration.dto;

import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnit;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrganizationalUnitDtoTest {
    @Test
    void constructor() {
        // Arrange
        var ou = new OrganizationalUnit();
        ou.setId(1L);
        ou.setName("test");
        ou.setMoodleId(19);
        ou.setCreatedBy("creator");
        ou.setCreatedDate(Instant.now().minusSeconds(60));
        ou.setLastModifiedBy("updater");
        ou.setLastModifiedDate(Instant.now());

        // Act
        var dto = new OrganizationalUnitDto(ou);

        // Assert
        assertEquals(ou.getId(), dto.id());
        assertEquals(ou.getName(), dto.name());
        assertEquals(ou.getMoodleId() != null, dto.moodleSynced());
        assertEquals(ou.getCreatedBy(), dto.createdBy());
        assertEquals(ou.getCreatedDate(), dto.createdDate());
        assertEquals(ou.getLastModifiedBy(), dto.lastModifiedBy());
        assertEquals(ou.getLastModifiedDate(), dto.lastModifiedDate());
    }
}
