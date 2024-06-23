package net.shoreline.server.route.frontend.routes.pages;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import net.shoreline.server.route.Route;

public final class PagesRoute extends Route
{
    @Override
    public void doHandle(Context context) throws Exception
    {
        String path = String.format(
                "/home/container/assets/webserver/pages/%s",
                context.pathParam("page")
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
