package net.shoreline.server.route.frontend.routes.styles;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import net.shoreline.server.route.Route;

public final class StylesRoute extends Route
{
    @Override
    public void doHandle(Context context) throws Exception
    {
        String path = String.format(
                "/home/container/assets/webserver/styles/%s",
                context.pathParam("style")
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
