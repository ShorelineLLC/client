package net.shoreline.server.route.installer.routes;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import net.shoreline.server.route.Route;

public final class LogoutRoute extends Route
{
    @Override
    public void doHandle(Context context) throws Exception
    {
        context.req().getSession().invalidate();

        throw new NotFoundResponse();
    }
}
