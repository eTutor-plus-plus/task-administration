package at.jku.dke.etutor.task_administration.config;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.boot.actuate.web.exchanges.HttpExchange;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.Locale;

/**
 * The application web configuration.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
        "classpath:/META-INF/resources/", "classpath:/resources/",
        "classpath:/static/", "classpath:/public/"
    };

    /**
     * Creates a new instance of class {@link WebConfig}.
     */
    public WebConfig() {
    }

    /**
     * Provides the http trace repository.
     *
     * @return The http trace repository.
     */
    @Bean
    public HttpExchangeRepository httpTraceRepository() {
        return new CustomHttpExchangeRepository();
    }

    /**
     * Provides the layout dialect for thymeleaf templates.
     *
     * @return The layout dialect.
     */
    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }

    /**
     * Provides the locale resolver.
     *
     * @return The locale resolver.
     */
    @Bean
    public LocaleResolver localeResolver() {
        var lr = new AcceptHeaderLocaleResolver();
        lr.setSupportedLocales(Arrays.asList(Locale.ENGLISH, Locale.GERMAN));
        return lr;
    }

    /**
     * Configures the resource handler to support webjars and static content.
     *
     * @param registry The resource handler registry.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
            .addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
    }

    /**
     * Provides the request context listener.
     *
     * @return The request context listener.
     */
    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }

    private static class CustomHttpExchangeRepository extends InMemoryHttpExchangeRepository {
        public CustomHttpExchangeRepository() {
            super();
            setCapacity(500);
        }

        @Override
        public void add(HttpExchange exchange) {
            if (exchange.getRequest().getUri().getPath().startsWith("/actuator") ||
                exchange.getRequest().getUri().getPath().startsWith("/app") ||
                exchange.getRequest().getUri().getPath().equals("/")) {
                return;
            }
            super.add(exchange);
        }
    }
}
