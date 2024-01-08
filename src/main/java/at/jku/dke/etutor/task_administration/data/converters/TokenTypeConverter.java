package at.jku.dke.etutor.task_administration.data.converters;

import at.jku.dke.etutor.task_administration.data.entities.TokenType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

/**
 * Converts the database "token_type"-enum to the {@link TokenType} enum.
 */
@Converter(autoApply = true)
public class TokenTypeConverter implements AttributeConverter<TokenType, String> {
    /**
     * Creates a new instance of class {@link TokenTypeConverter}.
     */
    public TokenTypeConverter() {
    }

    @Override
    public String convertToDatabaseColumn(TokenType type) {
        if (type == null)
            return null;
        return type.name().toLowerCase();
    }

    @Override
    public TokenType convertToEntityAttribute(String value) {
        if (value == null)
            return null;

        return Stream.of(TokenType.values())
            .filter(g -> g.name().toLowerCase().equals(value))
            .findAny().orElseThrow(IllegalArgumentException::new);
    }
}
