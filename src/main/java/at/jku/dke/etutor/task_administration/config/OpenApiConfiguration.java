package at.jku.dke.etutor.task_administration.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration
 */
@Configuration
@OpenAPIDefinition(info = @Info(
    title = "eTutor Administration",
    version = "dev",
    description = "RESTful API for eTutor Administration"),
    security = {@SecurityRequirement(name = "auth")})
@SecurityScheme(name = "auth", type = SecuritySchemeType.OAUTH2,
    flows = @OAuthFlows(password = @OAuthFlow(tokenUrl = "/auth/login-swagger")))
public class OpenApiConfiguration {
    /**
     * Creates a new instance of class {@link OpenApiConfiguration}.
     */
    public OpenApiConfiguration() {
    }
}
