package at.jku.dke.etutor.task_administration.data.repositories;

import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for entity {@link OrganizationalUnit}.
 */
public interface OrganizationalUnitRepository extends JpaRepository<OrganizationalUnit, Long> {
    /**
     * Returns a filtered paged list of organizational units.
     *
     * @param name     The filter text.
     * @param pageable The paging information.
     * @return List of organizational units.
     */
    Page<OrganizationalUnit> findByNameContainsIgnoreCase(String name, Pageable pageable);
}
