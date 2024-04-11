package at.jku.dke.etutor.task_administration.dto;

import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnit;
import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnitUser;
import at.jku.dke.etutor.task_administration.data.entities.User;
import at.jku.dke.etutor.task_administration.data.entities.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserDtoTest {
    @Test
    void constructor_withoutFilteredOUs() {
        // Arrange
        var user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setFirstName("Max");
        user.setLastName("Test");
        user.setEmail("test@example.com");
        user.setEnabled(true);
        user.setActivatedDate(OffsetDateTime.now());
        user.setFullAdmin(true);
        user.setFailedLoginCount(1);
        user.setLockoutEnd(null);
        user.setOrganizationalUnits(Set.of(new OrganizationalUnitUser(new OrganizationalUnit(4L), user, UserRole.INSTRUCTOR)));
        user.setCreatedBy("test");
        user.setCreatedDate(Instant.now().minusSeconds(40));
        user.setLastModifiedBy("system");
        user.setLastModifiedDate(Instant.now());

        // Act
        var userDto = new UserDto(user);

        // Assert
        assertEquals(user.getId(), userDto.id());
        assertEquals(user.getUsername(), userDto.username());
        assertEquals(user.getFirstName(), userDto.firstName());
        assertEquals(user.getLastName(), userDto.lastName());
        assertEquals(user.getEmail(), userDto.email());
        assertEquals(user.getEnabled(), userDto.enabled());
        assertEquals(user.getActivatedDate(), userDto.activated());
        assertEquals(user.isFullAdmin(), userDto.fullAdmin());
        assertEquals(user.getFailedLoginCount(), userDto.failedLoginCount());
        assertEquals(user.getLockoutEnd(), userDto.lockoutEnd());
        assertEquals(user.getOrganizationalUnits().size(), userDto.organizationalUnits().size());
        assertEquals(user.getCreatedBy(), userDto.createdBy());
        assertEquals(user.getCreatedDate(), userDto.createdDate());
        assertEquals(user.getLastModifiedBy(), userDto.lastModifiedBy());
        assertEquals(user.getLastModifiedDate(), userDto.lastModifiedDate());
    }

    @Test
    void constructor_withFilteredOUs() {
        // Arrange
        var user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setFirstName("Max");
        user.setLastName("Test");
        user.setEmail("test@example.com");
        user.setEnabled(true);
        user.setActivatedDate(OffsetDateTime.now());
        user.setFullAdmin(true);
        user.setFailedLoginCount(1);
        user.setLockoutEnd(null);
        user.setOrganizationalUnits(Set.of(
            new OrganizationalUnitUser(new OrganizationalUnit(4L), user, UserRole.INSTRUCTOR),
            new OrganizationalUnitUser(new OrganizationalUnit(2L), user, UserRole.TUTOR)
        ));
        user.setCreatedBy("test");
        user.setCreatedDate(Instant.now().minusSeconds(40));
        user.setLastModifiedBy("system");
        user.setLastModifiedDate(Instant.now());

        var ous = Set.of(2L);

        // Act
        var userDto = new UserDto(user, ous);

        // Assert
        assertEquals(user.getId(), userDto.id());
        assertEquals(user.getUsername(), userDto.username());
        assertEquals(user.getFirstName(), userDto.firstName());
        assertEquals(user.getLastName(), userDto.lastName());
        assertEquals(user.getEmail(), userDto.email());
        assertEquals(user.getEnabled(), userDto.enabled());
        assertEquals(user.getActivatedDate(), userDto.activated());
        assertEquals(user.isFullAdmin(), userDto.fullAdmin());
        assertEquals(user.getFailedLoginCount(), userDto.failedLoginCount());
        assertEquals(user.getLockoutEnd(), userDto.lockoutEnd());
        assertEquals(1, userDto.organizationalUnits().size());
        assertEquals(user.getCreatedBy(), userDto.createdBy());
        assertEquals(user.getCreatedDate(), userDto.createdDate());
        assertEquals(user.getLastModifiedBy(), userDto.lastModifiedBy());
        assertEquals(user.getLastModifiedDate(), userDto.lastModifiedDate());
    }
}
