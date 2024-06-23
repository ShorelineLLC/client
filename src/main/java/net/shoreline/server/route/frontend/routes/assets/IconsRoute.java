package net.shoreline.server.route.frontend.routes.assets;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import net.shoreline.server.route.Route;

public final class IconsRoute extends Route
{
    @Override
    public void doHandle(Context context) throws Exception
    {
        String path = String.format(
                "/home/container/assets/webserver/assets/icons/%s",
                context.pathParam("icon")
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
