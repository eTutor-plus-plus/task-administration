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
        if (field1 == null && field2 == null)
            return true;
        return field1 != null && field1.equals(field2);
    }
}
