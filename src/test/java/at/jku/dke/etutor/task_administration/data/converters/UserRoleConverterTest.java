package at.jku.dke.etutor.task_administration.data.converters;

import at.jku.dke.etutor.task_administration.data.entities.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRoleConverterTest {

    //#region --- convertToDatabaseColumn ---
    @Test
    void testConvertToDatabaseColumn() {
        // Arrange
        var converter = new UserRoleConverter();
        var status = UserRole.ADMIN;

        // Act
        var result = converter.convertToDatabaseColumn(status);

        // Assert
        assertEquals(status.name().toLowerCase(), result);
    }

    @Test
    void testConvertToDatabaseColumnNullValue() {
        // Arrange
        var converter = new UserRoleConverter();

        // Act
        var result = converter.convertToDatabaseColumn(null);

        // Assert
        assertNull(result);
    }
    //#endregion

    //#region --- convertToEntityAttribute ---
    @Test
    void convertToEntityAttribute() {
        // Arrange
        var converter = new UserRoleConverter();
        var status = UserRole.ADMIN;

        // Act
        var result = converter.convertToEntityAttribute(status.name().toLowerCase());

        // Assert
        assertEquals(status, result);
    }

    @Test
    void convertToEntityAttributeNullValue() {
        // Arrange
        var converter = new UserRoleConverter();

        // Act
        var result = converter.convertToEntityAttribute(null);

        // Assert
        assertNull(result);
    }

    @Test
    void convertToEntityAttributeInvalidValueThrowsException() {
        // Arrange
        var converter = new UserRoleConverter();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("invalid"));
    }
    //#endregion

}
