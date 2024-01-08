package at.jku.dke.etutor.task_administration.data.entities;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuditedEntityTest {

    @Test
    void testGetSetId() {
        // Arrange
        var task = new AuditedTestEntity();
        final long value = 2;

        // Act
        task.setId(value);
        var result = task.getId();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetCreatedBy() {
        // Arrange
        var task = new AuditedTestEntity();
        final String value = "test";

        // Act
        task.setCreatedBy(value);
        var result = task.getCreatedBy();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetCreatedDate() {
        // Arrange
        var task = new AuditedTestEntity();
        final Instant value = OffsetDateTime.now().toInstant();

        // Act
        task.setCreatedDate(value);
        var result = task.getCreatedDate();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetLastModifiedBy() {
        // Arrange
        var task = new AuditedTestEntity();
        final String value = "test";

        // Act
        task.setLastModifiedBy(value);
        var result = task.getLastModifiedBy();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetLastModifiedDate() {
        // Arrange
        var task = new AuditedTestEntity();
        final Instant value = OffsetDateTime.now().toInstant();

        // Act
        task.setLastModifiedDate(value);
        var result = task.getLastModifiedDate();

        // Assert
        assertEquals(value, result);
    }

    private static class AuditedTestEntity extends AuditedEntity {
        public AuditedTestEntity() {
        }
    }
}
