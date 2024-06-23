package net.shoreline.server.route.loader.route;

import io.javalin.http.*;
import net.shoreline.server.ServerMain;
import net.shoreline.server.route.Route;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Throws:
 *   NotFoundResponse (404) -> Headers not set properly, session attribute not set
 *   InternalServerErrorResponse (500) -> Some internal error happened
 */
public final class VersionRoute extends Route
{
    @Override
    public void doHandle(Context context)
    {
        String userAgent = context.header("User-Agent");

        if (!"shoreline-client".equals(userAgent))
        {
            throw new NotFoundResponse();
        }

        String currentVersion = context.header("Current-Version");

        if (currentVersion == null)
        {
            throw new NotFoundResponse();
        }

        String usertype = context.sessionAttribute("Usertype");

        if (usertype == null)
        {
            throw new NotFoundResponse();
        }

        String path = String.format(
                "/home/container/assets/%s/%s-loader-version.txt",
                usertype,
                usertype
        );

        String version;

        try
        {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            version = new String(bytes);
        } catch (Throwable t)
        {
            ServerMain.LOGGER.error("Couldn't read {} : ", path, t);
            throw new InternalServerErrorResponse();
        }

        if (!version.equals(currentVersion))
        {
            throw new ConflictResponse();
        }
    }
}
