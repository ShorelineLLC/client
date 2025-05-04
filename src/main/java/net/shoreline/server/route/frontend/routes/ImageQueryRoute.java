package net.shoreline.server.route.frontend.routes;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import net.shoreline.server.route.Route;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class ImageQueryRoute extends Route
{
    @Override
    public void doHandle(Context context) throws Exception
    {
        String query = context.queryParam("url");

        if (query == null)
        {
            throw new NotFoundResponse();
        }

        String path = String.format(
                "/home/container/assets/webserver/%s",
                URLDecoder.decode(query, StandardCharsets.UTF_8)
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
