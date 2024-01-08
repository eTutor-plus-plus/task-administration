package at.jku.dke.etutor.task_administration.data.repositories;

import at.jku.dke.etutor.task_administration.data.entities.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
