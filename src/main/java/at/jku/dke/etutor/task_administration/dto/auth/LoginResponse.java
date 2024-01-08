package at.jku.dke.etutor.task_administration.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * Result of user authentication.
 *
 * @param accessToken  The access token.
 * @param refreshToken The refresh token.
 * @param tokenType    The type of the access token.
 * @param expiresIn    The lifetime of the access token in seconds.
 */
public record LoginResponse(@NotNull @JsonProperty("access_token") String accessToken,
                            @NotNull @JsonProperty("refresh_token") String refreshToken,
                            @NotNull @JsonProperty("token_type") String tokenType,
                            @NotNull @JsonProperty("expires_in") long expiresIn) {
}
