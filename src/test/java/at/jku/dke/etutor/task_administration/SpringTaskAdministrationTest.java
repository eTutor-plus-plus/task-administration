package at.jku.dke.etutor.task_administration;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.Retention;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@SpringBootTest
@ExtendWith(DatabaseSetupExtension.class)
@ActiveProfiles("test")
public @interface SpringTaskAdministrationTest {
}
