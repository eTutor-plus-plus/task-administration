package at.jku.dke.etutor.task_administration.data.entities;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationalUnitTest {

    @Test
    void testGetSetName() {
        // Arrange
        var organizationalUnit = new OrganizationalUnit();
        final String value = "name";

        // Act
        organizationalUnit.setName(value);
        var result = organizationalUnit.getName();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testInitUsers() {
        // Arrange
        var organizationalUnit = new OrganizationalUnit();

        // Act
        var result = organizationalUnit.getUsers();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetSetUsers() {
        // Arrange
        var organizationalUnit = new OrganizationalUnit();
        Set<OrganizationalUnitUser> value = Set.of(new OrganizationalUnitUser());

        // Act
        organizationalUnit.setUsers(value);
        var result = organizationalUnit.getUsers();

        // Assert
        assertEquals(value, result);
    }
}
