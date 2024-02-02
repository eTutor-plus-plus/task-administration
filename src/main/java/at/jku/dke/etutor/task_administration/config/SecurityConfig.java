package at.jku.dke.etutor.task_administration.config;

import at.jku.dke.etutor.task_administration.TaskAdministrationApplication;
import at.jku.dke.etutor.task_administration.auth.AuthConstants;
import at.jku.dke.etutor.task_administration.auth.AuthJWKSource;
import at.jku.dke.etutor.task_administration.auth.CustomJwtAuthenticationConverter;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * The application security configuration.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Creates a new instance of class {@link SecurityConfig}.
     */
    public SecurityConfig() {
    }

    /**
     * Configures the applications' security filter chain.
     *
     * @param http The HTTP security configuration.
     * @param env  The environment.
     * @return The security filter chain.
     * @throws Exception If the configuration fails.
     */
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder, Environment env) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(Customizer.withDefaults());

        http.headers(conf -> {
            conf.referrerPolicy(c -> c.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER));
            conf.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny);
            conf.xssProtection(c -> c.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK));
        });

        http.authorizeHttpRequests(reg -> {
            reg.requestMatchers(HttpMethod.OPTIONS).permitAll();

            // Actuator
            reg.requestMatchers("/actuator/health").permitAll();
            reg.requestMatchers("/actuator/health/liveness").permitAll();
            reg.requestMatchers("/actuator/health/readiness").permitAll();
            reg.requestMatchers("/actuator/info").authenticated();
            reg.requestMatchers("/actuator/**").hasAuthority(AuthConstants.ROLE_FULL_ADMIN);

            // AUTH
            reg.requestMatchers("/auth/refresh").authenticated();

            // API
            reg.requestMatchers("/api/forwardPublic/**").permitAll();
            reg.requestMatchers("/api/**").authenticated();

            // Other
            reg.anyRequest().permitAll();
        });

        http.sessionManagement(conf -> conf.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.oauth2ResourceServer(conf -> conf.jwt(jwtConf -> {
            jwtConf.decoder(jwtDecoder);
            jwtConf.jwkSetUri(TaskAdministrationApplication.determineLocalAddress(env) + "auth/jwk");
            jwtConf.jwtAuthenticationConverter(customJwtAuthenticationConverter());
        }));

        return http.build();
    }

    /**
     * Configures the CORS configuration source.
     *
     * @return The CORS configuration source.
     */
    @Bean
    @Profile("dev")
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader(CorsConfiguration.ALL);
        config.addAllowedMethod(CorsConfiguration.ALL);
        config.addAllowedOrigin(CorsConfiguration.ALL);
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Provides the authentication manager.
     *
     * @param userDetailsService The service that provides the user details.
     * @param passwordEncoder    The password encoder.
     * @return The authentication manager.
     */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    /**
     * Provides the password encoder.
     *
     * @return The password encoder for encoding passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Provides a decoder for JWT tokens.
     *
     * @return JWT decoder
     */
    @Bean
    public JwtDecoder jwtDecoder(AuthJWKSource jwkSource) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        return NimbusJwtDecoder.withPublicKey(jwkSource.getPublicKey()).build();
    }

    /**
     * Provides an encoder for JWT tokens.
     *
     * @return JWT encoder
     */
    @Bean
    public JwtEncoder jwtEncoder(AuthJWKSource jwkSource) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        JWK jwk = new RSAKey.Builder(jwkSource.getPublicKey()).privateKey(jwkSource.getPrivateKey()).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    /**
     * Provides a custom JWT authentication converter.
     *
     * @return Custom JWT converter.
     */
    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> customJwtAuthenticationConverter() {
        return new CustomJwtAuthenticationConverter();
    }

}
