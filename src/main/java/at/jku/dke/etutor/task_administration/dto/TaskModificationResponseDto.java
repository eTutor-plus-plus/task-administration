package at.jku.dke.etutor.task_administration.dto;

import java.math.BigDecimal;

/**
 * Response data for task creation/modification.
 *
 * @param descriptionDe The german description to set (might be {@code null}).
 * @param descriptionEn The english description to set (might be {@code null}).
 * @param difficulty    The difficulty to set (might be {@code null}).
 * @param maxPoints     The maximum points to set (might be {@code null}).
 */
public record TaskModificationResponseDto(
    String descriptionDe,
    String descriptionEn,
    Short difficulty,
    BigDecimal maxPoints) {
}
