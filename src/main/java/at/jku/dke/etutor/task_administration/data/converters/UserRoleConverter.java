package at.jku.dke.etutor.task_administration.data.converters;

import at.jku.dke.etutor.task_administration.data.entities.UserRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

/**
 * Converts the database "roles"-enum to the {@link UserRole} enum.
 */
@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {
    /**
     * Creates a new instance of class {@link UserRoleConverter}.
     */
    public UserRoleConverter() {
    }

    @Override
    public String convertToDatabaseColumn(UserRole role) {
        if (role == null)
            return null;
        return role.name().toLowerCase();
    }

    @Override
    public UserRole convertToEntityAttribute(String value) {
        if (value == null)
            return null;

        return Stream.of(UserRole.values())
            .filter(g -> g.name().toLowerCase().equals(value))
            .findAny().orElseThrow(IllegalArgumentException::new);
    }
}
