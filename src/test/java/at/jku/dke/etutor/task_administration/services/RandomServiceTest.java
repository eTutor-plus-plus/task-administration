package at.jku.dke.etutor.task_administration.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RandomServiceTest {

    @Test
    void randomString() {
        // Act
        var result = RandomService.INSTANCE.randomString(30);

        // Assert
        assertEquals(30, result.length());
    }

}
