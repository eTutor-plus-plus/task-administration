package at.jku.dke.etutor.task_administration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(DatabaseSetupExtension.class)
class TaskAdministrationApplicationTests {

    @Test
    void contextLoads() {
    }

}
