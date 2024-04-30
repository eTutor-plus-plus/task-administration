package at.jku.dke.etutor.task_administration.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValueEqualsValidatorTest {

    @Test
    void isValid_equal() {
        // Arrange
        var validator = new ValueEqualsValidator();
        var annotation = TestClass.class.getAnnotation(ValuesEquals.class);

        // Act
        validator.initialize(annotation);
        var result = validator.isValid(new TestClass("test", "test"), null);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValid_notEqual() {
        // Arrange
        var validator = new ValueEqualsValidator();
        var annotation = TestClass.class.getAnnotation(ValuesEquals.class);

        // Act
        validator.initialize(annotation);
        var result = validator.isValid(new TestClass("test", "test2"), null);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValid_firstNull() {
        // Arrange
        var validator = new ValueEqualsValidator();
        var annotation = TestClass.class.getAnnotation(ValuesEquals.class);

        // Act
        validator.initialize(annotation);
        var result = validator.isValid(new TestClass(null, "test2"), null);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValid_bothNull() {
        // Arrange
        var validator = new ValueEqualsValidator();
        var annotation = TestClass.class.getAnnotation(ValuesEquals.class);

        // Act
        validator.initialize(annotation);
        var result = validator.isValid(new TestClass(null, null), null);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValid_invalidField() {
        // Arrange
        var validator = new ValueEqualsValidator();
        var annotation = InvalidTestClass.class.getAnnotation(ValuesEquals.class);

        // Act & Assert
        validator.initialize(annotation);
        assertThrows(RuntimeException.class, () -> validator.isValid(new InvalidTestClass("test"), null));
    }

    @ValuesEquals(field1 = "field1", field2 = "field2")
    private static class TestClass {
        private final String field1;
        private final String field2;

        public TestClass(String field1, String field2) {
            this.field1 = field1;
            this.field2 = field2;
        }
    }

    @ValuesEquals(field1 = "field1", field2 = "field2")
    private static class InvalidTestClass {
        private final String field1;

        public InvalidTestClass(String field1) {
            this.field1 = field1;
        }
    }
}
