package net.shoreline.server.route.frontend.routes;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import net.shoreline.server.route.Route;

public final class GenericPageRoute extends Route
{
    private final String pageName;

    public GenericPageRoute(String pageName)
    {
        this.pageName = pageName;
    }

    @Override
    public void doHandle(Context context) throws Exception
    {
        String path = String.format(
                "/home/container/assets/webserver/%s.html",
                this.pageName
        );

        try
        {
            sendFile(context, path);
        } catch (Throwable t)
        {
            t.printStackTrace();
            throw new NotFoundResponse();
        }
    }
}
