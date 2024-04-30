package at.jku.dke.etutor.task_administration.moodle;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MoodleServiceTest {

    @Test
    void createHttpClient() {
        // Arrange
        var config = new MoodleConfig();
        config.setUrl("http://test.com");
        config.setToken("test-token");
        var service = new MoodleServiceDefaultImpl(config);

        // Act
        var actual = service.createHttpClient();

        // Assert
        assertNotNull(actual);
    }

    @Test
    void getDefaultQueryParameters() {
        // Arrange
        var config = new MoodleConfig();
        config.setUrl("http://test.com");
        config.setToken("test-token");
        var service = new MoodleServiceDefaultImpl(config);
        var func = "test-function";

        // Act
        var result = service.getDefaultQueryParameters(func);

        // Assert
        assertThat(result)
            .containsEntry("wstoken", config.getToken())
            .containsEntry("moodlewsrestformat", "json")
            .containsEntry("wsfunction", func);
    }

    @Test
    void post_200() throws URISyntaxException, IOException, InterruptedException {
        // Arrange
        var response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("test-body");

        var client = mock(HttpClient.class);
        when(client.send(any(), any())).thenReturn(response);

        var config = new MoodleConfig();
        config.setUrl("http://test.com");
        config.setToken("test-token");
        var service = new MoodleServiceTestImpl(config, client);

        // Act
        var actual = service.post(service.getDefaultQueryParameters("test-func"), Map.of("id", "3"));

        // Assert
        assertThat(actual).isEqualTo("test-body");
    }

    @Test
    void post_500() throws IOException, InterruptedException {
        // Arrange
        var response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(500);
        when(response.body()).thenReturn("test-body");

        var client = mock(HttpClient.class);
        when(client.send(any(), any())).thenReturn(response);

        var config = new MoodleConfig();
        config.setUrl("http://test.com");
        config.setToken("test-token");
        var service = new MoodleServiceTestImpl(config, client);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.post(service.getDefaultQueryParameters("test-func"), Map.of("id", "3")));
    }

    @Test
    void post_200_withException() throws IOException, InterruptedException {
        // Arrange
        var response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"exception\":\"test-exception\"}");

        var client = mock(HttpClient.class);
        when(client.send(any(), any())).thenReturn(response);

        var config = new MoodleConfig();
        config.setUrl("http://test.com");
        config.setToken("test-token");
        var service = new MoodleServiceTestImpl(config, client);

        // Act
        assertThrows(RuntimeException.class, () -> service.post(service.getDefaultQueryParameters("test-func"), Map.of("id", "3")));
    }

    private static class MoodleServiceTestImpl extends MoodleService {

        private final HttpClient client;

        protected MoodleServiceTestImpl(MoodleConfig config, HttpClient client) {
            super(config, null);
            this.client = client;
        }

        @Override
        protected HttpClient createHttpClient() {
            return this.client;
        }
    }

    private static class MoodleServiceDefaultImpl extends MoodleService {
        protected MoodleServiceDefaultImpl(MoodleConfig config) {
            super(config, null);
        }
    }
}
