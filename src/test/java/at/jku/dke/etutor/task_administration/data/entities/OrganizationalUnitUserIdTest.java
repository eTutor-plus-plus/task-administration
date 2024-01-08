package at.jku.dke.etutor.task_administration.data.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationalUnitUserIdTest {

    @Test
    void testGetSetOrganizationalUnitId() {
        // Arrange
        var id = new OrganizationalUnitUserId();
        final long value = 2;

        // Act
        id.setOrganizationalUnitId(value);

        // Assert
        assertEquals(value, id.getOrganizationalUnitId());
    }

    @Test
    void testGetSetUserId() {
        // Arrange
        var id = new OrganizationalUnitUserId();
        final long value = 2;

        // Act
        id.setUserId(value);

        // Assert
        assertEquals(value, id.getUserId());
    }

    @Test
    void testEqualsSameReturnsTrue() {
        // Arrange
        var id = new OrganizationalUnitUserId();
        id.setOrganizationalUnitId(1L);
        id.setUserId(2L);
        var id2 = id;

        // Act
        var result = id.equals(id2);

        // Assert
        assertTrue(result);
    }

    @Test
    void testEqualsEqualReturnsTrue() {
        // Arrange
        var id = new OrganizationalUnitUserId();
        id.setOrganizationalUnitId(1L);
        id.setUserId(2L);
        var id2 = new OrganizationalUnitUserId();
        id2.setOrganizationalUnitId(1L);
        id2.setUserId(2L);

        // Act
        var result = id.equals(id2);

        // Assert
        assertTrue(result);
    }

    @Test
    void testEqualsNullReturnsFalse() {
        // Arrange
        var id = new OrganizationalUnitUserId();
        id.setOrganizationalUnitId(1L);
        id.setUserId(2L);

        // Act
        var result = id.equals(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testEqualsOtherUserIdReturnsFalse() {
        // Arrange
        var id = new OrganizationalUnitUserId();
        id.setOrganizationalUnitId(1L);
        id.setUserId(2L);
        var id2 = new OrganizationalUnitUserId();
        id2.setOrganizationalUnitId(1L);
        id2.setUserId(3L);

        // Act
        var result = id.equals(id2);

        // Assert
        assertFalse(result);
    }

    @Test
    void testEqualsOtherOrganizationalUnitIdReturnsFalse() {
        // Arrange
        var id = new OrganizationalUnitUserId();
        id.setOrganizationalUnitId(1L);
        id.setUserId(2L);
        var id2 = new OrganizationalUnitUserId();
        id2.setOrganizationalUnitId(3L);
        id2.setUserId(2L);

        // Act
        var result = id.equals(id2);

        // Assert
        assertFalse(result);
    }

    @Test
    void testEqualsOtherClassReturnsFalse() {
        // Arrange
        var id = new OrganizationalUnitUserId();
        id.setOrganizationalUnitId(1L);
        id.setUserId(2L);
        var id2 = "Test";

        // Act
        var result = id.equals(id2);

        // Assert
        assertFalse(result);
    }

    @Test
    void testHashCodeEqual() {
        // Arrange
        var id = new OrganizationalUnitUserId();
        id.setOrganizationalUnitId(1L);
        id.setUserId(2L);
        var id2 = new OrganizationalUnitUserId();
        id2.setOrganizationalUnitId(1L);
        id2.setUserId(2L);

        // Act
        var result1 = id.hashCode();
        var result2 = id2.hashCode();

        // Assert
        assertEquals(result1, result2);
    }

    @Test
    void testHashCodeOtherUserId(){
        // Arrange
        var id = new OrganizationalUnitUserId();
        id.setOrganizationalUnitId(1L);
        id.setUserId(2L);
        var id2 = new OrganizationalUnitUserId();
        id2.setOrganizationalUnitId(1L);
        id2.setUserId(3L);

        // Act
        var result1 = id.hashCode();
        var result2 = id2.hashCode();

        // Assert
        assertNotEquals(result1, result2);
    }

    @Test
    void testHashCodeOtherOrganizationalUnitId(){
        // Arrange
        var id = new OrganizationalUnitUserId();
        id.setOrganizationalUnitId(1L);
        id.setUserId(2L);
        var id2 = new OrganizationalUnitUserId();
        id2.setOrganizationalUnitId(3L);
        id2.setUserId(2L);

        // Act
        var result1 = id.hashCode();
        var result2 = id2.hashCode();

        // Assert
        assertNotEquals(result1, result2);
    }

    @Test
    void testToString() {
        // Arrange
        var id = new OrganizationalUnitUserId();
        id.setOrganizationalUnitId(1L);
        id.setUserId(2L);

        // Act
        var result = id.toString();

        // Assert
        assertEquals("OrganizationalUnitUserId[organizationalUnitId=1, userId=2]", result);
    }
}
