package net.shoreline.server.route.loader.routes;

import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import net.shoreline.server.ServerMain;
import net.shoreline.server.route.Route;

public final class NativesRoute extends Route
{
    @Override
    public void doHandle(Context context) throws Exception
    {
        String userAgent = context.header("User-Agent");

        if (!"shoreline-client".equals(userAgent))
        {
            throw new NotFoundResponse();
        }

        String libType = context.header("Library-Type");

        if (!"dll".equals(libType) && !"dylib".equals(libType) && !"so".equals(libType))
        {
            throw new NotFoundResponse();
        }

        String path = String.format(
                "/home/container/assets/loader/natives/shoreline_loader.%s",
                libType
        );

        try
        {
            sendFile(context, path);
        } catch (Throwable t)
        {
            ServerMain.LOGGER.error("Couldn't send {} : ", path, t);
            throw new InternalServerErrorResponse();
        }
    }
}
