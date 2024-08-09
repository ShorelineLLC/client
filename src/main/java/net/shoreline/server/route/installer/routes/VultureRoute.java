package net.shoreline.server.route.installer.routes;

import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UnauthorizedResponse;
import net.shoreline.server.ServerMain;
import net.shoreline.server.route.Route;

/**
 * Throws:
 *   NotFoundResponse (404) ->
 *      1) If the user agent doesn't match
 *   UnauthorizedResponse (401) ->
 *      1) If the user doesn't have an active session (aka, they accessed this route without logging in)
 *   InternalServerErrorResponse (500) ->
 *      1) If something went wrong here
 */
public final class VultureRoute extends Route
{
    @Override
    public void doHandle(Context context) throws Exception
    {
        String userAgent = context.header("User-Agent");

        if (!"shoreline-installer".equals(userAgent))
        {
            throw new NotFoundResponse();
        }

        String fileExtension = context.header("File-Extension");

        if (!".exe".equals(fileExtension) && !"".equals(fileExtension))
        {
            throw new NotFoundResponse();
        }

        String path = String.format(
                "/home/container/assets/installer/vulture/vulture%s",
                fileExtension
        );

        try
        {
            sendFile(context, path);
        } catch (Throwable t)
        {
            ServerMain.LOGGER.error("Couldn't read or send {}", path);
            throw new InternalServerErrorResponse();
        }
    }
}