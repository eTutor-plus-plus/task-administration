package at.jku.dke.etutor.task_administration.services;

import at.jku.dke.etutor.task_administration.auth.AuthConstants;
import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnit;
import at.jku.dke.etutor.task_administration.data.repositories.OrganizationalUnitRepository;
import at.jku.dke.etutor.task_administration.dto.ModifyOrganizationalUnitDto;
import at.jku.dke.etutor.task_administration.dto.OrganizationalUnitDto;
import at.jku.dke.etutor.task_administration.moodle.CourseCategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * This class provides methods for managing {@link OrganizationalUnit}s.
 */
@Service
public class OrganizationalUnitService {
    private static final Logger LOG = LoggerFactory.getLogger(OrganizationalUnitService.class);

    private final OrganizationalUnitRepository repository;
    private final CourseCategoryService courseCategoryService;

    /**
     * Creates a new instance of class {@link OrganizationalUnitService}.
     *
     * @param repository            The organizational unit repository.
     * @param courseCategoryService The course category service.
     */
    public OrganizationalUnitService(OrganizationalUnitRepository repository, CourseCategoryService courseCategoryService) {
        this.repository = repository;
        this.courseCategoryService = courseCategoryService;
    }

    //#region --- View ---

    /**
     * Returns all organizational units for the requested page.
     *
     * @param page   The page and sorting information.
     * @param filter Optional filter string (applies contains to name attributes).
     * @return List of organizational units
     */
    @Transactional(readOnly = true)
    public Page<OrganizationalUnitDto> getOrganizationalUnits(Pageable page, String filter) {
        LOG.debug("Loading organizational units for page {}", page);
        return (filter == null || filter.isBlank() ?
            this.repository.findAll(page) :
            this.repository.findByNameContainsIgnoreCase(filter, page)).map(OrganizationalUnitDto::new);
    }

    /**
     * Returns the organizational unit with the specified identifier.
     *
     * @param id The identifier.
     * @return The organizational unit or an empty result if the organizational unit does not exist.
     */
    @Transactional(readOnly = true)
    public Optional<OrganizationalUnitDto> getOrganizationalUnit(long id) {
        LOG.debug("Loading organizational unit {}", id);
        return this.repository.findById(id).map(OrganizationalUnitDto::new);
    }

    //#endregion

    //#region --- Modify ---

    /**
     * Creates a new organizational unit.
     *
     * @param dto The organizational unit data.
     * @return The created organizational unit.
     */
    @Transactional
    @PreAuthorize(AuthConstants.AUTHORITY_FULL_ADMIN)
    public OrganizationalUnit create(ModifyOrganizationalUnitDto dto) {
        LOG.info("Creating organizational unit {}", dto.name());

        var organizationalUnit = new OrganizationalUnit();
        organizationalUnit.setName(dto.name());
        organizationalUnit = this.repository.save(organizationalUnit);

        this.createMoodleObjectsForOrganizationalUnit(organizationalUnit);
        return organizationalUnit;
    }

    /**
     * Updates an existing organizational unit.
     *
     * @param id               The organizational unit identifier.
     * @param dto              The new organizational unit data.
     * @param concurrencyToken The concurrency token.
     * @throws ConcurrencyFailureException If the concurrency check failed.
     */
    @Transactional
    @PreAuthorize(AuthConstants.AUTHORITY_FULL_ADMIN)
    public void update(long id, ModifyOrganizationalUnitDto dto, Instant concurrencyToken) {
        var organizationalUnit = this.repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Organizational unit " + id + " does not exist."));
        if (concurrencyToken != null && organizationalUnit.getLastModifiedDate() != null && organizationalUnit.getLastModifiedDate().isAfter(concurrencyToken))
            throw new ConcurrencyFailureException("Organizational unit has been modified in the meantime");

        LOG.info("Updating organizational unit {}", id);
        organizationalUnit.setName(dto.name());
        organizationalUnit = this.repository.save(organizationalUnit);
        this.updateMoodleObjectsForOrganizationalUnit(organizationalUnit);
    }

    /**
     * Deletes the organizational unit with the specified identifier.
     *
     * @param id The identifier of the organizational unit to delete.
     */
    @Transactional
    @PreAuthorize(AuthConstants.AUTHORITY_FULL_ADMIN)
    public void delete(long id) {
        LOG.info("Deleting organizational unit {}", id);
        this.repository.deleteById(id);
    }

    //#endregion

    //#region --- Moodle ---

    /**
     * Called when an organizational unit has been created.
     *
     * @param organizationalUnit The organizational unit.
     */
    public void createMoodleObjectsForOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        if (organizationalUnit.getMoodleId() != null)
            return;

        this.courseCategoryService.createCourseCategory(organizationalUnit).thenAccept(moodleId -> {
            if (moodleId.isPresent()) {
                organizationalUnit.setMoodleId(moodleId.get());
                this.repository.save(organizationalUnit);
            }
        });
    }

    /**
     * Called when an organizational unit has been updated.
     *
     * @param organizationalUnit The organizational unit.
     */
    public void updateMoodleObjectsForOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        this.courseCategoryService.updateCourseCategory(organizationalUnit);
    }

    /**
     * Synchronizes the organizational unit with the Moodle course category.
     *
     * @param id The organizational unit id.
     * @throws EntityNotFoundException If the organizational unit does not exist.
     */
    @Transactional(readOnly = true)
    public void createMoodleObjectsForOrganizationalUnit(long id) {
        var organizationalUnit = this.repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Organizational unit " + id + " does not exist."));
        this.createMoodleObjectsForOrganizationalUnit(organizationalUnit);
    }

    //#endregion
}
