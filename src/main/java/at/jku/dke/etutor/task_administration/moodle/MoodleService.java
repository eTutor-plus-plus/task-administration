package at.jku.dke.etutor.task_administration.moodle;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Base-class for moodle services.
 */
public abstract class MoodleService {
    /**
     * The logger.
     */
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * The moodle configuration.
     */
    protected final MoodleConfig config;

    /**
     * The object mapper.
     */
    protected final ObjectMapper objectMapper;

    private final String url;

    /**
     * Creates a new instance of class {@link MoodleService}.
     *
     * @param config       The moodle configuration.
     * @param objectMapper The object mapper.
     */
    protected MoodleService(MoodleConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;

        var tmp = this.config.getUrl();
        if (tmp == null)
            tmp = "";
        if (!tmp.endsWith("/"))
            tmp += "/";
        this.url = tmp + "webservice/rest/server.php";
    }

    /**
     * Returns the default query parameters for moodle requests.
     *
     * @param function The moodle function to call.
     * @return The default query parameters.
     */
    protected Map<String, String> getDefaultQueryParameters(String function) {
        return Map.of(
            "wstoken", config.getToken(),
            "moodlewsrestformat", "json",
            "wsfunction", function
        );
    }

    /**
     * Sends a POST request to moodle.
     *
     * @param queryParameters The query parameters.
     * @param body            The body.
     * @return The response body.
     * @throws URISyntaxException   If the URL is invalid.
     * @throws IOException          If the body could not be converted to JSON or if the connection failed.
     * @throws InterruptedException If the connection was interrupted.
     * @throws RuntimeException     If the request failed.
     */
    protected String post(Map<String, String> queryParameters, Map<String, String> body) throws URISyntaxException, IOException, InterruptedException {
        // Build URL
        String query = queryParameters.entrySet().stream()
            .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), Charset.defaultCharset()))
            .reduce((a, b) -> a + "&" + b)
            .orElse("");
        var uri = new URI(url + "?" + query);

        // Convert body to urlencoded string
        var encoded = body.entrySet().stream()
            .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), Charset.defaultCharset()))
            .reduce((a, b) -> a + "&" + b)
            .orElse("");

        // Send request
        var request = HttpRequest.newBuilder(uri)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(encoded))
            .build();
        try (HttpClient client = this.createHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String bodyString = response.body();
            if (response.statusCode() != 200) {
                LOG.error("Request {} failed with status code {}: {}", uri, response.statusCode(), bodyString);
                throw new RuntimeException("Request failed: " + bodyString);
            }

            // Check for errors
            if (bodyString.contains("\"exception\":")) {
                LOG.error("Request {} failed with status code {}: {}", uri, response.statusCode(), bodyString);
                throw new RuntimeException("Request failed: " + bodyString);
            }

            return bodyString;
        }
    }

    /**
     * Creates a new HTTP client.
     *
     * @return The HTTP client.
     */
    protected HttpClient createHttpClient() {
        return HttpClient.newBuilder().build();
    }
}
