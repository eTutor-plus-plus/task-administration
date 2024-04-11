package at.jku.dke.etutor.task_administration.data;

import at.jku.dke.etutor.task_administration.auth.WithMockJwtUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
class AuditorAwareImplTest {

    @Test
    @WithMockUser(username = "getCurrentAuditorFromUser")
    void getCurrentAuditorFromUser() {
        // Arrange
        var auditorAware = new AuditorAwareImpl();
        var expected = "getCurrentAuditorFromUser";

        // Act
        var actual = auditorAware.getCurrentAuditor().orElse(null);

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    @WithMockJwtUser(sub = "getCurrentAuditorFromJwt")
    void getCurrentAuditorFromJwt() {
        // Arrange
        var auditorAware = new AuditorAwareImpl();
        var expected = "getCurrentAuditorFromJwt";

        // Act
        var actual = auditorAware.getCurrentAuditor().orElse(null);

        // Assert
        assertEquals(expected, actual);
    }

//    @Test
//    @WithUserDetails(value = "getCurrentAuditorFromUserDetails")
//    void getCurrentAuditorFromUserDetails() {
//        // Arrange
//        var auditorAware = new AuditorAwareImpl();
//        var expected = "getCurrentAuditorFromUserDetails";
//
//        // Act
//        var actual = auditorAware.getCurrentAuditor().orElse(null);
//
//        // Assert
//        assertEquals(expected, actual);
//    }

    @Test
    void getCurrentAuditorFromNothing() {
        // Arrange
        var auditorAware = new AuditorAwareImpl();
        var expected = "SYSTEM";

        // Act
        var actual = auditorAware.getCurrentAuditor().orElse(null);

        // Assert
        assertEquals(expected, actual);
    }
}
