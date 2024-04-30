package at.jku.dke.etutor.task_administration.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.web.exchanges.HttpExchange;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.net.URI;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebConfigTest {

    @Test
    void httpTraceRepository() {
        assertNotNull(new WebConfig().httpTraceRepository());
    }

    @Test
    void layoutDialect() {
        assertNotNull(new WebConfig().layoutDialect());
    }

    @Test
    void localeResolver() {
        // Arrange
        var config = new WebConfig();

        // Act
        var result = config.localeResolver();

        // Assert
        assertNotNull(result);
        assertThat(result).isInstanceOf(AcceptHeaderLocaleResolver.class);
        assertThat(((AcceptHeaderLocaleResolver) result).getSupportedLocales())
            .containsExactly(Locale.ENGLISH, Locale.GERMAN);
    }

    @Test
    void addResourceHandlers() {
        // Arrange
        var config = new WebConfig();
        var registry = new ResourceHandlerRegistry(mock(ApplicationContext.class), null);

        // Act
        config.addResourceHandlers(registry);

        // Assert
    }

    @Test
    void requestContextListener() {
        assertNotNull(new WebConfig().requestContextListener());
    }

    @Test
    void customRepository() {
        // Arrange
        var config = new WebConfig();
        var repo = config.httpTraceRepository();

        // Act
        var ex = mock(HttpExchange.class);
        var req = mock(HttpExchange.Request.class);
        when(req.getUri()).thenReturn(URI.create("http://localhost/actuator"));
        when(ex.getRequest()).thenReturn(req);
        repo.add(ex);

        ex = mock(HttpExchange.class);
        req = mock(HttpExchange.Request.class);
        when(req.getUri()).thenReturn(URI.create("http://localhost/app"));
        when(ex.getRequest()).thenReturn(req);
        repo.add(ex);

        ex = mock(HttpExchange.class);
        req = mock(HttpExchange.Request.class);
        when(req.getUri()).thenReturn(URI.create("http://localhost/docs"));
        when(ex.getRequest()).thenReturn(req);
        repo.add(ex);

        ex = mock(HttpExchange.class);
        req = mock(HttpExchange.Request.class);
        when(req.getUri()).thenReturn(URI.create("http://localhost/swagger-ui"));
        when(ex.getRequest()).thenReturn(req);
        repo.add(ex);

        ex = mock(HttpExchange.class);
        req = mock(HttpExchange.Request.class);
        when(req.getUri()).thenReturn(URI.create("http://localhost/"));
        when(ex.getRequest()).thenReturn(req);
        repo.add(ex);

        ex = mock(HttpExchange.class);
        req = mock(HttpExchange.Request.class);
        when(req.getUri()).thenReturn(URI.create("http://localhost/api"));
        when(ex.getRequest()).thenReturn(req);
        repo.add(ex);

        // Assert
        assertThat(repo.findAll()).hasSize(1);
    }
}
