package at.jku.dke.etutor.task_administration.data.converters;

import at.jku.dke.etutor.task_administration.data.entities.TokenType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenTypeConverterTest {

    //#region --- convertToDatabaseColumn ---
    @Test
    void testConvertToDatabaseColumn() {
        // Arrange
        var converter = new TokenTypeConverter();
        var status = TokenType.ACTIVATE_ACCOUNT;

        // Act
        var result = converter.convertToDatabaseColumn(status);

        // Assert
        assertEquals(status.name().toLowerCase(), result);
    }

    @Test
    void testConvertToDatabaseColumnNullValue() {
        // Arrange
        var converter = new TokenTypeConverter();

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
        var converter = new TokenTypeConverter();
        var status = TokenType.ACTIVATE_ACCOUNT;

        // Act
        var result = converter.convertToEntityAttribute(status.name().toLowerCase());

        // Assert
        assertEquals(status, result);
    }

    @Test
    void convertToEntityAttributeNullValue() {
        // Arrange
        var converter = new TokenTypeConverter();

        // Act
        var result = converter.convertToEntityAttribute(null);

        // Assert
        assertNull(result);
    }

    @Test
    void convertToEntityAttributeInvalidValueThrowsException() {
        // Arrange
        var converter = new TokenTypeConverter();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("invalid"));
    }
    //#endregion

}
