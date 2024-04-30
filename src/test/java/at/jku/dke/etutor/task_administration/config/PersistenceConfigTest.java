package at.jku.dke.etutor.task_administration.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PersistenceConfigTest {

    @Test
    void auditorProvider() {
        assertNotNull(new PersistenceConfig().auditorProvider());
    }

}
