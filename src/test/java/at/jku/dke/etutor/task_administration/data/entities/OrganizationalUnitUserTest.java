package at.jku.dke.etutor.task_administration.data.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationalUnitUserTest {

    @Test
    void testGetSetId() {
        // Arrange
        var organizationalUnitUser = new OrganizationalUnitUser();
        final OrganizationalUnitUserId value = new OrganizationalUnitUserId();
        value.setUserId(1L);
        value.setOrganizationalUnitId(2L);

        // Act
        organizationalUnitUser.setId(value);
        var result = organizationalUnitUser.getId();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetOrganizationalUnit() {
        // Arrange
        var organizationalUnitUser = new OrganizationalUnitUser();
        final OrganizationalUnit value = new OrganizationalUnit();
        value.setId(1L);

        // Act
        organizationalUnitUser.setOrganizationalUnit(value);
        var result = organizationalUnitUser.getOrganizationalUnit();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetUser() {
        // Arrange
        var organizationalUnitUser = new OrganizationalUnitUser();
        final User value = new User();
        value.setId(1L);

        // Act
        organizationalUnitUser.setUser(value);
        var result = organizationalUnitUser.getUser();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetRole() {
        // Arrange
        var organizationalUnitUser = new OrganizationalUnitUser();
        final UserRole value = UserRole.ADMIN;

        // Act
        organizationalUnitUser.setRole(value);
        var result = organizationalUnitUser.getRole();

        // Assert
        assertEquals(value, result);
    }
}
