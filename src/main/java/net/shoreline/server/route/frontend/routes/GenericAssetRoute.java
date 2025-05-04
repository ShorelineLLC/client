package net.shoreline.server.route.frontend.routes;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import net.shoreline.server.route.Route;

public final class GenericAssetRoute extends Route
{
    private final String assetPath;

    public GenericAssetRoute(String assetPath)
    {
        this.assetPath = assetPath;
    }

    @Override
    public void doHandle(Context context) throws Exception
    {
        String path = String.format(
                "/home/container/assets/webserver/%s",
                this.assetPath + context.pathParam("asset")
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
