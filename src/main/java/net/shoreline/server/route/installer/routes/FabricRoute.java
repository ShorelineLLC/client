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
public final class FabricRoute extends Route
{
    @Override
    public void doHandle(Context context) throws Exception
    {
        String userAgent = context.header("User-Agent");

        if (!"shoreline-installer".equals(userAgent))
        {
            throw new NotFoundResponse();
        }

        String usertype = context.sessionAttribute("User-Type");

        if (usertype == null)
        {
            throw new UnauthorizedResponse();
        }

        String path = "/home/container/assets/installer/fabric-api.jar";

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
