package net.shoreline.server.route.frontend.routes;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import net.shoreline.server.route.Route;

public final class HomeRoute extends Route
{
    @Override
    public void doHandle(Context context) throws Exception
    {
        String path = "/home/container/assets/webserver/pages/index.html";

        try
        {
            sendFile(context, path);
        } catch (Throwable t)
        {
            throw new NotFoundResponse();
        }
    }
}
