package net.shoreline.server.route.loader.routes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotAcceptableResponse;
import io.javalin.http.NotFoundResponse;
import net.shoreline.server.ServerMain;
import net.shoreline.server.encryption.Encryption;
import net.shoreline.server.route.Route;
import net.shoreline.server.route.loader.LoaderEndpoint;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Throws:
 *   NotFoundResponse (404) -> User agent not set properly, session attribute not set, invalid json body
 *   NotAcceptableResponse (406) -> Loader hash does not match that on the server (it's been tampered with)
 *   InternalServerErrorResponse (500) -> Some internal error happened
 */

public final class GenTokenRoute extends Route
{
    @Override
    public void doHandle(Context context)
    {
        String userAgent = context.header("User-Agent");

        if (!"shoreline-client".equals(userAgent))
        {
            throw new NotFoundResponse();
        }

        String username = context.sessionAttribute("Username");

        if (username == null)
        {
            throw new NotFoundResponse();
        }

        String usertype = context.sessionAttribute("User-Type");

        if (usertype == null)
        {
            throw new NotFoundResponse();
        }

        String loaderHash;
        try
        {
            JsonObject request = JsonParser.parseString(context.body()).getAsJsonObject();

            loaderHash = request.get("Loader-Hash").getAsString();
        } catch (Throwable t)
        {
            throw new NotFoundResponse();
        }

        String path = String.format(
                "/home/container/assets/loader/%s/loader.jar",
                usertype
        );

        String encrypted;

        try
        {
            byte[] bytes = Files.readAllBytes(Paths.get(path));

            String unencrypted = Arrays.toString(bytes);
            encrypted = Encryption.encrypt(unencrypted);
        } catch (Throwable t)
        {
            ServerMain.LOGGER.error("Failed to complete integrity check: ", t);
            throw new InternalServerErrorResponse();
        }


        if (!encrypted.equals(loaderHash))
        {
            if (usertype.equals("dev"))
            {
                ServerMain.LOGGER.info("Allowing {} to launch from a malformed loader due to dev status", username);
            } else
            {
                ServerMain.LOGGER.warn("[ALERT] {}'s loader has been tampered with. They have been auto-banned.", username);
                throw new NotAcceptableResponse();
            }
        }

        String token = switch (usertype)
        {
            case "dev" -> LoaderEndpoint.DEV_TOKEN_MANAGER.getAndAuthorizeNewToken();
            case "beta" -> LoaderEndpoint.BETA_TOKEN_MANAGER.getAndAuthorizeNewToken();
            case "release" -> LoaderEndpoint.RELEASE_TOKEN_MANAGER.getAndAuthorizeNewToken();
            default ->
            {
                ServerMain.LOGGER.error("Unrecognized session usertype: {}", usertype);
                throw new InternalServerErrorResponse();
            }
        };

        JsonObject response = new JsonObject();

        response.add("Token", new JsonPrimitive(token));

        context.result(response.toString()).contentType("application/json");
    }
}
