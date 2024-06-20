package at.jku.dke.etutor.task_administration.data.repositories;

import at.jku.dke.etutor.task_administration.data.entities.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Repository for entity {@link UserToken}.
 */
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    /**
     * Finds a user token by its token.
     *
     * @param token The token.
     * @return The user token.
     */
    Optional<UserToken> findByToken(String token);

    /**
     * Deletes all user tokens that expire before the specified date.
     *
     * @param expiresAt The date.
     * @return The number of deleted tokens.
     */
    long deleteByExpiresAt(OffsetDateTime expiresAt);

    /**
     * Counts the amount of active password reset tokens of the specified user.
     *
     * @param id The user identifier.
     * @return Token count
     */
    @Query("SELECT COUNT(t.id) FROM UserToken t WHERE t.user.id = ?1 AND t.expiresAt > CURRENT_TIMESTAMP AND t.type = 'reset_password'")
    long countActivePasswordResetTokensForUser(Long id);
}
