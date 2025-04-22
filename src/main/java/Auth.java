// this code is for proof-of-concept only
// this class  wont compile

import java.io.IOException;
import java.util.UUID;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JsonObject;
import org.json.JsonParser;
import org.json.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Assuming these classes/methods exist in your project
// import com.mojang.authlib.minecraft.Minecraft;
// import com.mojang.authlib.Session;
// import static path.to.YourClass.setSession; // Replace with the actual path

public class Auth {

    private static final Logger logger = LogManager.getLogger(Auth.class);

    /**
     * Converts a string to a UUID. It handles both the standard UUID format
     * (with hyphens) and the compact format (without hyphens).
     *
     * @param uuid The string representation of the UUID.
     * @return The UUID object.
     * @throws IllegalArgumentException If the input string is not a valid UUID.
     */
    public static UUID getIdFromString(String uuid) throws IllegalArgumentException {
        if (uuid == null || uuid.isEmpty()) {
            throw new IllegalArgumentException("UUID string cannot be null or empty");
        }

        if (uuid.contains("-")) {
            return UUID.fromString(uuid);
        } else {
            // Ensure the input string has the correct length before attempting to format it
            if (uuid.length() != 32) {
                throw new IllegalArgumentException("UUID string without hyphens must be 32 characters long");
            }
            return UUID.fromString(
                uuid.replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
                    "$1-$2-$3-$4-$5"));
        }
    }

    /**
     * Retrieves a Minecraft profile (UUID and username) from the Mojang API
     * and sets the client's session.
     *
     * @param username The Minecraft username.
     * @throws IllegalArgumentException If the username is invalid or if there are issues
     * parsing the API response.
     * @throws IllegalStateException    If there are issues setting the session.
     */
    static void spoofClientSession(String username) throws IllegalArgumentException, IllegalStateException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        String apiUrl = String.format("https://api.mojang.com/users/profiles/minecraft/%s", username);

        try (CloseableHttpResponse response = HttpClient.getInstance().getClosableHttpClient().execute(new HttpGet(apiUrl))) {
            String responseBody = EntityUtils.toString(response.getEntity());
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

            UUID uuid = getIdFromString(jsonResponse.get("id").getAsString());
            String correctUsername = jsonResponse.get("name").getAsString();

            // Assuming setSession is a method that sets the Minecraft session
            // and Session class exists
            // and Minecraft.getMinecraft().getSession().getToken() returns the current session token
            setSession(
                new Session(
                    correctUsername,
                    uuid.toString(),
                    Minecraft.getMinecraft().getSession().getToken(),
                    "mojang"));

        } catch (IOException e) {
            logger.error("Error fetching or processing profile for username: {}", username, e);
            throw new IllegalStateException("Failed to fetch or process Minecraft profile", e);
        }  catch (ParseException e) {
            logger.error("Error parsing response for username: {}", username, e);
            throw new IllegalArgumentException("Failed to parse API response", e);
        }
    }
}
