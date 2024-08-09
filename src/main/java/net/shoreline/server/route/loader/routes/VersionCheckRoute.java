package net.shoreline.server.route.loader.routes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.http.*;
import net.shoreline.server.ServerMain;
import net.shoreline.server.route.Route;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Throws:
 *   NotFoundResponse (404) -> Headers not set properly, session attribute not set
 *   ConflictResponse (409) -> The versions of the loader don't match the server
 *   InternalServerErrorResponse (500) -> Some internal error happened
 */
public final class VersionCheckRoute extends Route
{
    @Override
    public void doHandle(Context context)
    {
        String userAgent = context.header("User-Agent");

        if (!"shoreline-client".equals(userAgent))
        {
            throw new NotFoundResponse();
        }

        String contentType = context.contentType();

        if (!"application/json".equals(contentType))
        {
            throw new NotFoundResponse();
        }

        String usertype = context.sessionAttribute("User-Type");

        if (usertype == null)
        {
            throw new NotFoundResponse();
        }

        String currentVersion;
        try
        {
            JsonObject request = JsonParser.parseString(context.body()).getAsJsonObject();

            currentVersion = request.get("Current-Version").getAsString();
        } catch (Throwable t)
        {
            throw new NotFoundResponse();
        }

        String path = String.format(
                "/home/container/assets/loader/%s/%s-loader-version.txt",
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
