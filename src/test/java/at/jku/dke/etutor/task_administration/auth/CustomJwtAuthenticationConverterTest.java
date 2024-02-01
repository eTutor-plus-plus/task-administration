package at.jku.dke.etutor.task_administration.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomJwtAuthenticationConverterTest {

    @Test
    void convert_subject() {
        // Arrange
        Jwt jwt = Jwt.withTokenValue("test")
            .subject("test")
            .headers(h -> h.put("typ", "JWT"))
            .build();
        CustomJwtAuthenticationConverter converter = new CustomJwtAuthenticationConverter();

        // Act
        var result = converter.convert(jwt);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("test");
        assertThat(result.getAuthorities()).isEmpty();
    }

    @Test
    void convert_fullAdmin() {
        // Arrange
        Jwt jwt = Jwt.withTokenValue("test")
            .subject("test")
            .headers(h -> h.put("typ", "JWT"))
            .claim(AuthConstants.CLAIM_FULL_ADMIN, true)
            .build();
        CustomJwtAuthenticationConverter converter = new CustomJwtAuthenticationConverter();

        // Act
        var result = converter.convert(jwt);

        // Assert
        assertNotNull(result);
        assertThat(result.getAuthorities()).allMatch(x -> x.getAuthority().equals(AuthConstants.ROLE_FULL_ADMIN));
    }

    @Test
    void convert_notFullAdmin() {
        // Arrange
        Jwt jwt = Jwt.withTokenValue("test")
            .subject("test")
            .headers(h -> h.put("typ", "JWT"))
            .claim(AuthConstants.CLAIM_FULL_ADMIN, false)
            .build();
        CustomJwtAuthenticationConverter converter = new CustomJwtAuthenticationConverter();

        // Act
        var result = converter.convert(jwt);

        // Assert
        assertNotNull(result);
        assertThat(result.getAuthorities()).noneMatch(x -> x.getAuthority().equals(AuthConstants.ROLE_FULL_ADMIN));
    }

    @Test
    void convert_roles() {
        // Arrange
        Jwt jwt = Jwt.withTokenValue("test")
            .subject("test")
            .headers(h -> h.put("typ", "JWT"))
            .claim(AuthConstants.CLAIM_ROLES, List.of(
                Map.of("role", AuthConstants.ROLE_INSTRUCTOR, "organizationalUnit", 1),
                Map.of("role", AuthConstants.ROLE_ADMIN, "organizationalUnit", 2)
            ))
            .build();
        CustomJwtAuthenticationConverter converter = new CustomJwtAuthenticationConverter();

        // Act
        var result = converter.convert(jwt);

        // Assert
        assertNotNull(result);
        assertThat(result.getAuthorities())
            .containsExactly(new SimpleGrantedAuthority(AuthConstants.ROLE_INSTRUCTOR), new SimpleGrantedAuthority(AuthConstants.ROLE_ADMIN));
    }

}
