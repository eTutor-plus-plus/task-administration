package at.jku.dke.etutor.task_administration.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for combining a DTO with additional data.
 *
 * @param dto            The DTO.
 * @param additionalData The additional data.
 * @param <TDto>         The type of the DTO.
 */
public record CombinedDto<TDto>(@NotNull TDto dto, @Nullable Object additionalData) {
}
