package com.nhackindustries.leakyleaky;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class provides an implementation of the MinecraftSessionService,
 * wrapping another implementation and adding custom authentication logic.
 */
public class EpicAuthenticationService implements MinecraftSessionService {

    private static final Logger logger = LogManager.getLogger(EpicAuthenticationService.class);
    private final MinecraftSessionService wrappedService;

    /**
     * Constructs an EpicAuthenticationService instance.
     *
     * @param wrappedService The MinecraftSessionService instance to wrap.
     * This allows for chaining or decorating existing
     * authentication services.
     */
    public EpicAuthenticationService(MinecraftSessionService wrappedService) {
        this.wrappedService = wrappedService;
    }

    /**
     * Performs server join authentication with the Minecraft session server.
     *
     * @param profile             The GameProfile of the player joining the server.
     * @param authenticationToken The player's authentication token.
     * @param serverId            The server ID.
     * @throws AuthenticationException            If authentication fails.
     * @throws AuthenticationUnavailableException If the authentication server is unavailable.
     */
    @Override
    public void joinServer(GameProfile profile, String authenticationToken, String serverId)
        throws AuthenticationException, AuthenticationUnavailableException {

        // Validate input parameters
        if (profile == null || profile.getName() == null || profile.getName().trim().isEmpty()
            || authenticationToken == null || authenticationToken.trim().isEmpty()
            || serverId == null || serverId.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid input parameters for joinServer");
        }

        String authUrl = String.format(
            "http://session.minecraft.net/game/joinserver.jsp?user=%s&sessionId=%s&serverId=%s",
            profile.getName(), authenticationToken, serverId);

        logger.info("Attempting to authenticate with URL: {}", authUrl);

        try (CloseableHttpResponse response = HttpClient.getInstance().getClosableHttpClient().execute(new HttpGet(authUrl))) {
            String responseBody = EntityUtils.toString(response.getEntity());

            if (responseBody == null || !responseBody.equalsIgnoreCase("OK")) {
                logger.error("Server join authentication failed. Response: {}", responseBody);
                throw new AuthenticationException("Failed to authenticate with Minecraft server.  Response was: " + responseBody);
            }

            logger.info("Server join authentication successful.");

        } catch (IOException e) {
            logger.error("Error communicating with authentication server.", e);
            throw new AuthenticationUnavailableException("Cannot contact Minecraft authentication server", e);
        }
    }

    /**
     * Checks if a player has joined a server.
     * This method delegates to the wrapped MinecraftSessionService.
     *
     * @param user     The player's GameProfile.
     * @param serverId The server ID.
     * @param address  The player's IP address.
     * @return The GameProfile if the player has joined, otherwise null.
     * @throws AuthenticationUnavailableException If the authentication server is unavailable.
     */
    @Override
    public GameProfile hasJoinedServer(GameProfile user, String serverId, InetAddress address)
        throws AuthenticationUnavailableException {
        // Consider adding input validation here if needed, similar to joinServer
        return this.wrappedService.hasJoinedServer(user, serverId, address);
    }

    /**
     * Gets the player's skin and cape textures.
     * This method delegates to the wrapped MinecraftSessionService.
     *
     * @param profile        The player's GameProfile.
     * @param requireSecure  Whether to require a secure connection.
     * @return A map of texture types to MinecraftProfileTexture objects.
     */
    @Override
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(
        GameProfile profile, boolean requireSecure) {
        // Consider adding input validation here if needed
        return this.wrappedService.getTextures(profile, requireSecure);
    }

    /**
     * Fills the player's profile properties.
     * This method delegates to the wrapped MinecraftSessionService.
     *
     * @param profile        The player's GameProfile.
     * @param requireSecure  Whether to require a secure connection.
     * @return The filled GameProfile.
     */
    @Override
    public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) {
        // Consider adding input validation here if needed
        return wrappedService.fillProfileProperties(profile, requireSecure);
    }
}
