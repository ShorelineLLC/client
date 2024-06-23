package net.shoreline.server.route.frontend.routes.assets;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import net.shoreline.server.route.Route;

public final class FontsRoute extends Route
{
    @Override
    public void doHandle(Context context) throws Exception
    {
        String path = String.format(
                "/home/container/assets/webserver/assets/fonts/%s",
                context.pathParam("font")
        );

        try
        {
            sendFile(context, path);
        } catch (Throwable t)
        {
            throw new NotFoundResponse();
        }
    }
}
