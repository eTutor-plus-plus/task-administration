package at.jku.dke.etutor.task_administration.data.converters;

import at.jku.dke.etutor.task_administration.data.entities.TaskStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

/**
 * Converts the database "status"-enum to the {@link TaskStatus} enum.
 */
@Converter(autoApply = true)
public class TaskStatusConverter implements AttributeConverter<TaskStatus, String> {
    /**
     * Creates a new instance of class {@link TaskStatusConverter}.
     */
    public TaskStatusConverter() {
    }

    @Override
    public String convertToDatabaseColumn(TaskStatus status) {
        if (status == null)
            return null;
        return status.name().toLowerCase();
    }

    @Override
    public TaskStatus convertToEntityAttribute(String value) {
        if (value == null)
            return null;

        return Stream.of(TaskStatus.values())
            .filter(g -> g.name().toLowerCase().equals(value))
            .findAny().orElseThrow(IllegalArgumentException::new);
    }
}
