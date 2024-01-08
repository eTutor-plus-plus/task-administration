package at.jku.dke.etutor.task_administration.data.repositories;

import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnitUser;
import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnitUserId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

/**
 * Repository for entity {@link OrganizationalUnitUser}.
 */
public interface OrganizationalUnitUserRepository extends JpaRepository<OrganizationalUnitUser, OrganizationalUnitUserId> {
    /**
     * Returns all entries for the specified user.
     *
     * @param id The user identifier.
     * @return All entries of the specified user.
     */
    Set<OrganizationalUnitUser> findByUser_Id(Long id);
}
