package net.shoreline.server.route.loader.routes;

import io.javalin.http.*;
import net.shoreline.server.ServerMain;
import net.shoreline.server.encryption.Encryption;
import net.shoreline.server.route.Route;
import net.shoreline.server.route.loader.LoaderEndpoint;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Throws:
 *   NotFoundResponse (404) -> User agent not set properly, loader hash not set properly, session attribute not set
 *   NotAcceptableResponse (406) -> Loader hash does not match that on the server (it's been tampered with)
 *   InternalServerErrorResponse (500) -> Some internal error happened
 */
public final class IntegrityRoute extends Route
{
    @Override
    public void doHandle(Context context)
    {
        String userAgent = context.header("User-Agent");

        if (!"shoreline-client".equals(userAgent))
        {
            throw new NotFoundResponse();
        }

        String loaderHash = context.header("Loader-Hash");

        if (loaderHash == null)
        {
            throw new NotFoundResponse();
        }

        String usertype = context.sessionAttribute("User-Type");

        if (usertype == null)
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
            throw new NotAcceptableResponse();
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

        context.result(token).contentType("text/plain");
    }
}
