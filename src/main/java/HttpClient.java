import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

/**
 * A utility class for creating and managing an HttpClient instance
 * with predefined configuration. This class follows the Singleton pattern
 * to ensure only one instance of the HttpClient is used throughout the application.
 */
public class HttpClient {

    private static final Logger logger = LogManager.getLogger(HttpClient.class);
    private static final HttpClient instance = new HttpClient();
    private final CloseableHttpClient httpClient;

    /**
     * Private constructor to enforce the Singleton pattern.
     * Initializes the HttpClient with a default request configuration
     * that includes connection and socket timeout settings.
     */
    private HttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(30 * 1000)       // Connection timeout in milliseconds
            .setConnectionRequestTimeout(30 * 1000) // Socket timeout in milliseconds
            .setSocketTimeout(30 * 1000)
            .build();

        this.httpClient = HttpClientBuilder.create()
            .setDefaultRequestConfig(requestConfig)
            .build();

        logger.info("HttpClient initialized with connect timeout: 30000ms, connection request timeout: 30000ms, socket timeout: 30000ms");
    }

    /**
     * Returns the singleton instance of HttpClient.
     *
     * @return The HttpClient instance.
     */
    public static HttpClient getInstance() {
        return instance;
    }

    /**
     * Provides access to the CloseableHttpClient instance.
     * This should be used to execute HTTP requests.
     *
     * @return The CloseableHttpClient instance.
     */
    public CloseableHttpClient getClosableHttpClient() {
        return httpClient;
    }

    /**
     * Closes the HttpClient and releases its resources.
     * This should be called when the application shuts down
     * to prevent resource leaks.
     */
    public void closeHttpClient() {
        try {
            if (httpClient != null) {
                httpClient.close();
                logger.info("HttpClient closed successfully.");
            }
        } catch (IOException e) {
            logger.error("Error closing HttpClient", e);
        }
    }

    // Example Usage (Illustrative - not part of the class)
    public static void main(String[] args) {
        HttpClient client = HttpClient.getInstance();
        CloseableHttpClient httpClient = client.getClosableHttpClient();

        // Use httpClient to make requests...
        // Example:  httpClient.execute(new HttpGet("http://example.com"));

        // When you're done (e.g., on application shutdown):
        client.closeHttpClient();
    }
}
