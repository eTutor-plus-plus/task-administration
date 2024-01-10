package at.jku.dke.etutor.task_administration.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Ensures that two fields have the same value.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ValueEqualsValidator.class})
public @interface ValuesEquals {
    /**
     * The name of the first field to validate.
     *
     * @return The field name.
     */
    String field1();

    /**
     * The name of the second field to validate.
     *
     * @return The field name.
     */
    String field2();

    String message() default "{jakarta.validation.constraints.ValueEquals.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
