package at.jku.dke.etutor.task_administration.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates fields to be equal.
 */
public class ValueEqualsValidator implements ConstraintValidator<ValuesEquals, Object> {

    private String field1;
    private String field2;

    /**
     * Creates a new instance of class Valid task group number validator.
     */
    public ValueEqualsValidator() {
    }

    @Override
    public void initialize(ValuesEquals constraintAnnotation) {
        this.field1 = constraintAnnotation.field1();
        this.field2 = constraintAnnotation.field2();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            var objField1 = value.getClass().getDeclaredField(this.field1);
            var objField2 = value.getClass().getDeclaredField(this.field2);

            objField1.setAccessible(true);
            objField2.setAccessible(true);

            var value1 = objField1.get(value);
            var value2 = objField2.get(value);

            if (value1 == null && value2 == null)
                return true;
            return value1 != null && value1.equals(value2);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
