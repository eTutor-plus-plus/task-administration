package at.jku.dke.etutor.task_administration.services;

import at.jku.dke.etutor.task_administration.auth.AuthConstants;
import at.jku.dke.etutor.task_administration.auth.SecurityHelpers;
import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnitUser;
import at.jku.dke.etutor.task_administration.data.entities.TokenType;
import at.jku.dke.etutor.task_administration.data.entities.User;
import at.jku.dke.etutor.task_administration.data.entities.UserToken;
import at.jku.dke.etutor.task_administration.data.repositories.OrganizationalUnitRepository;
import at.jku.dke.etutor.task_administration.data.repositories.OrganizationalUnitUserRepository;
import at.jku.dke.etutor.task_administration.data.repositories.UserRepository;
import at.jku.dke.etutor.task_administration.data.repositories.UserTokenRepository;
import at.jku.dke.etutor.task_administration.dto.ModifyUserDto;
import at.jku.dke.etutor.task_administration.dto.ModifyUserPasswordDto;
import at.jku.dke.etutor.task_administration.dto.UserDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This class provides methods for managing {@link User}s.
 */
@Service
@PreAuthorize(AuthConstants.AUTHORITY_ADMIN_OR_ABOVE)
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private final UserRepository repository;
    private final UserTokenRepository userTokenRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final OrganizationalUnitUserRepository organizationalUnitUserRepository;
    private final MailService mailService;
    private final MessageSource messageSource;

    /**
     * Creates a new instance of class {@link UserService}.
     *
     * @param repository                       The user repository
     * @param userTokenRepository              The user token repository
     * @param organizationalUnitRepository     The organizational unit repository.
     * @param organizationalUnitUserRepository The organizational unit user repository.
     * @param mailService                      The mail service.
     */
    public UserService(UserRepository repository, UserTokenRepository userTokenRepository, OrganizationalUnitRepository organizationalUnitRepository,
                       OrganizationalUnitUserRepository organizationalUnitUserRepository, MailService mailService, MessageSource messageSource) {
        this.repository = repository;
        this.userTokenRepository = userTokenRepository;
        this.organizationalUnitRepository = organizationalUnitRepository;
        this.organizationalUnitUserRepository = organizationalUnitUserRepository;
        this.mailService = mailService;
        this.messageSource = messageSource;
    }

    //#region --- View ---

    /**
     * Returns all users for the requested page.
     *
     * @param page            The page and sorting information.
     * @param usernameFilter  Optional filter string (applies contains to username).
     * @param firstNameFilter Optional filter string (applies contains to first name).
     * @param lastNameFilter  Optional filter string (applies contains to last name).
     * @param emailFilter     Optional filter string (applies contains to email).
     * @param enabledFilter   Optional filter for enabled state.
     * @param fullAdminFilter Optional filter for full admin state.
     * @return List of users
     */
    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(Pageable page, String usernameFilter, String firstNameFilter, String lastNameFilter, String emailFilter, Boolean enabledFilter, Boolean fullAdminFilter) {
        LOG.debug("Loading users for page {}", page);
        return this.repository.findAll(new FilterSpecification(usernameFilter, firstNameFilter, lastNameFilter, emailFilter, enabledFilter, fullAdminFilter), page).map(UserDto::new);
    }

    /**
     * Returns the user with the specified identifier.
     *
     * @param id The identifier.
     * @return The user or an empty result if the user does not exist.
     */
    @Transactional(readOnly = true)
    public Optional<UserDto> getUser(long id) {
        LOG.debug("Loading user {}", id);
        var orgUnit = SecurityHelpers.getOrganizationalUnitsAsAdmin();
        return SecurityHelpers.isFullAdmin() ?
            this.repository.findById(id).map(UserDto::new) :
            this.repository.findByIdOfOrganizationUnits(id, orgUnit).map(d -> new UserDto(d, orgUnit));
    }

    //#endregion

    //#region --- Modify ---

    /**
     * Creates a new user and sends an activation email.
     *
     * @param dto The user data.
     * @return The created user.
     */
    @Transactional
    public User create(ModifyUserDto dto) {
        LOG.info("Creating user {}", dto.username());

        var fullAdmin = SecurityHelpers.isFullAdmin();
        var ids = SecurityHelpers.getOrganizationalUnitsAsAdmin();

        // create user
        var user = new User();
        user.setUsername(dto.username().toLowerCase());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setEmail(dto.email());
        user.setEnabled(dto.enabled());
        user.setActivatedDate(dto.activated());
        user.setPassword(dto.username() + UUID.randomUUID());
        if (fullAdmin) {
            user.setFullAdmin(dto.fullAdmin());
            user.setLockoutEnd(dto.lockoutEnd());
        }
        var dbUser = this.repository.save(user);

        // create ou role assignments
        this.organizationalUnitUserRepository.saveAll(dto.organizationalUnits()
            .stream()
            .filter(ou -> fullAdmin || ids.contains(ou.organizationalUnit())) // ensure that only valid OUs are assigned
            .map(ou -> new OrganizationalUnitUser(this.organizationalUnitRepository.getReferenceById(ou.organizationalUnit()), dbUser, ou.role()))
            .collect(Collectors.toSet()));

        // create activation token
        var token = new UserToken(TokenType.ACTIVATE_ACCOUNT, dbUser, RandomService.INSTANCE.randomString(50), OffsetDateTime.now().plusDays(7));
        this.userTokenRepository.save(token);

        // Send mail (if not yet activated)
        if(dbUser.getActivatedDate() == null) {
            this.mailService.sendMail(user.getEmail(),
                this.messageSource.getMessage("activateAccount.mail.subject", null, Locale.ENGLISH),
                this.messageSource.getMessage("activateAccount.mail.text", new Object[]{
                    String.format("%s %s", user.getFirstName(), user.getLastName()),
                    user.getUsername(),
                    ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString(),
                    token.getToken()
                }, Locale.ENGLISH));
        }
        return dbUser;
    }

    /**
     * Updates an existing user.
     *
     * @param id               The user identifier.
     * @param dto              The new user data.
     * @param concurrencyToken The concurrency token.
     * @throws ConcurrencyFailureException If the concurrency check failed.
     */
    @Transactional
    public void update(long id, ModifyUserDto dto, Instant concurrencyToken) {
        var user = this.repository.findById(id).orElseThrow(() -> new EntityNotFoundException("User " + id + " does not exist."));
        if (concurrencyToken != null && user.getLastModifiedDate() != null && user.getLastModifiedDate().isAfter(concurrencyToken))
            throw new ConcurrencyFailureException("User has been modified in the meantime");

        LOG.info("Updating user {}", user.getUsername());
        var fullAdmin = SecurityHelpers.isFullAdmin();
        var adminIds = SecurityHelpers.getOrganizationalUnitsAsAdmin();

        // Validate that current user is full admin or admin of at least one OU
        var ous = this.organizationalUnitUserRepository.findByUser_Id(user.getId()).stream()
            .filter(x -> fullAdmin || adminIds.contains(x.getId().getOrganizationalUnitId()))
            .toList();
        if (!fullAdmin && adminIds.stream().noneMatch(ouId -> ous.stream().anyMatch(x -> x.getId().getOrganizationalUnitId().equals(ouId))))
            throw new EntityNotFoundException("User " + id + " does not exist.");

        // Update user
        user.setUsername(dto.username().toLowerCase());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setEmail(dto.email());
        user.setEnabled(dto.enabled());
        if (fullAdmin) {
            user.setFullAdmin(dto.fullAdmin());
            user.setLockoutEnd(dto.lockoutEnd());
            user.setActivatedDate(dto.activated());
        }
        this.repository.save(user);

        // Update OUs
        var toRemove = ous.stream()
            .filter(u -> dto.organizationalUnits().stream().noneMatch(ou -> u.getOrganizationalUnit().getId().equals(ou.organizationalUnit())))
            .toList();
        var toAdd = dto.organizationalUnits().stream()
            .filter(ou -> fullAdmin || adminIds.contains(ou.organizationalUnit()))
            .filter(ou -> ous.stream().noneMatch(u -> u.getOrganizationalUnit().getId().equals(ou.organizationalUnit())))
            .map(ou -> new OrganizationalUnitUser(this.organizationalUnitRepository.getReferenceById(ou.organizationalUnit()), user, ou.role()))
            .toList();
        var toUpdate = ous.stream()
            .filter(u -> dto.organizationalUnits().stream().anyMatch(ou -> u.getOrganizationalUnit().getId().equals(ou.organizationalUnit()) && !ou.role().equals(u.getRole())))
            .peek(ou -> dto.organizationalUnits().stream().filter(x -> ou.getOrganizationalUnit().getId().equals(x.organizationalUnit())).findFirst().ifPresent(tmp -> ou.setRole(tmp.role())))
            .toList();

        this.organizationalUnitUserRepository.deleteAll(toRemove);
        this.organizationalUnitUserRepository.saveAll(toAdd);
        this.organizationalUnitUserRepository.saveAll(toUpdate);
    }

    /**
     * Updates the password of an existing user.
     *
     * @param id               The user identifier.
     * @param dto              The new password data.
     * @param concurrencyToken The concurrency token.
     * @throws ConcurrencyFailureException If the concurrency check failed.
     */
    @Transactional
    @PreAuthorize(AuthConstants.AUTHORITY_FULL_ADMIN)
    public void setPassword(long id, ModifyUserPasswordDto dto, Instant concurrencyToken) {
        var user = this.repository.findById(id).orElseThrow(() -> new EntityNotFoundException("User " + id + " does not exist."));
        if (concurrencyToken != null && user.getLastModifiedDate() != null && user.getLastModifiedDate().isAfter(concurrencyToken))
            throw new ConcurrencyFailureException("User has been modified in the meantime");

        // Update password
        LOG.info("Setting password for user {}", user.getUsername());
        user.setPassword(dto.password());
        this.repository.save(user);
    }

    /**
     * Deletes the user with the specified identifier.
     *
     * @param id The identifier of the user to delete.
     */
    @Transactional
    public void deleteUser(long id) {
        var userId = SecurityHelpers.getUserId();
        if (userId.isEmpty() || userId.get() == id)
            throw new ValidationException("A user cannot delete itself.");

        if (SecurityHelpers.isFullAdmin()) {
            LOG.info("Deleting user {}", id);
            this.repository.deleteById(id);
        } else {
            var orgUnits = SecurityHelpers.getOrganizationalUnitsAsAdmin();
            var user = this.repository.findById(id).orElse(null);
            if (user == null)
                return;

            var ouu = user.getOrganizationalUnits().stream().filter(x -> orgUnits.contains(x.getOrganizationalUnit().getId())).toList();
            if (ouu.size() == user.getOrganizationalUnits().size() && !user.getOrganizationalUnits().isEmpty() && !user.isFullAdmin()) { // user only exists for current organizational unit
                LOG.info("Deleting user {}", id);
                this.repository.delete(user);
            } else if (!ouu.isEmpty()) { // user also belongs to other organizational units
                LOG.info("Removing user {} from organizational units {}", id, ouu.stream().map(x -> x.getOrganizationalUnit().getId()).toList());
                this.organizationalUnitUserRepository.deleteAll(ouu);
            }
        }
    }

    /**
     * Deletes not activated users that are older than 30 days.
     */
    @Transactional
    @Scheduled(cron = "0 0 1 * * ?")
    public void deleteNoActivatedUsers() {
        var limit = Instant.now().minus(30, ChronoUnit.DAYS);
        LOG.info("Deleting not activated users created before {}", limit);
        this.repository.deleteByActivatedDateNullAndCreatedDateLessThan(limit);
    }

    //#endregion

    //#region --- Specifications ---
    private record FilterSpecification(String username, String firstName, String lastName, String email, Boolean enabled, Boolean fullAdmin) implements Specification<User> {

        @Override
        public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            var predicates = new ArrayList<Predicate>();

            // User-specified filters
            if (this.username != null)
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + this.username.toLowerCase() + "%"));
            if (this.firstName != null)
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + this.firstName.toLowerCase() + "%"));
            if (this.lastName != null)
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + this.lastName.toLowerCase() + "%"));
            if (this.email != null)
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + this.email.toLowerCase() + "%"));
            if (this.enabled != null)
                predicates.add(criteriaBuilder.equal(root.get("enabled"), this.enabled));
            if (this.fullAdmin != null)
                predicates.add(criteriaBuilder.equal(root.get("fullAdmin"), this.fullAdmin));

            // Permission related filters
            if (!SecurityHelpers.isFullAdmin()) {
                var in = criteriaBuilder.in(root.get("organizationalUnits").get("id").get("organizationalUnitId"));
                SecurityHelpers.getOrganizationalUnitsAsAdmin().forEach(in::value);
                predicates.add(in);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }

    }
    //#endregion
}
