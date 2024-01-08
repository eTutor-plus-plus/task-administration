package at.jku.dke.etutor.task_administration.data.repositories;

import at.jku.dke.etutor.task_administration.data.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

/**
 * Repository for entity {@link User}.
 */
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    /**
     * Finds the user with the specified name. This method will ignore username casing.
     * Additionally, loads all organizational units.
     *
     * @param username The username of the user to find.
     * @return The found user or an empty result.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.organizationalUnits WHERE LOWER(u.username) = LOWER(:username)")
    Optional<User> findByUsernameIgnoreCaseAndFetchOrganizationalUnits(@Param("username") String username);

    /**
     * Finds the user with the specified identifier in one of the specified organizational units.
     *
     * @param id                The user identifier.
     * @param organizationUnits The organizational units.
     * @return The user, if found.
     */
    @Query("""
        SELECT u
        FROM User u
            LEFT JOIN FETCH u.organizationalUnits
        WHERE u.id = :id AND
              EXISTS (
                    SELECT o
                    FROM  u.organizationalUnits o
                    WHERE o.organizationalUnit.id IN :organizationUnits
                  )""")
    Optional<User> findByIdOfOrganizationUnits(long id, Collection<Long> organizationUnits);

    /**
     * Finds the user with the specified name. This method will ignore username casing.
     *
     * @param username The username of the user to find.
     * @return The found user or an empty result.
     */
    Optional<User> findByUsernameIgnoreCase(String username);

    /**
     * Deletes all users that have not been activated and have been created before the specified date.
     *
     * @param createdDate The creation date.
     * @return The number of deleted users.
     */
    long deleteByActivatedDateNullAndCreatedDateLessThan(Instant createdDate);
}
