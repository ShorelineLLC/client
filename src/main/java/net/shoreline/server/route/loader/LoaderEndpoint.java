package net.shoreline.server.route.loader;

import io.javalin.apibuilder.ApiBuilder;
import io.javalin.apibuilder.EndpointGroup;
import net.shoreline.server.route.loader.route.*;
import net.shoreline.server.token.TokenManager;

public final class LoaderEndpoint implements EndpointGroup
{
    public static TokenManager DEV_TOKEN_MANAGER = new TokenManager();
    public static TokenManager BETA_TOKEN_MANAGER = new TokenManager();
    public static TokenManager RELEASE_TOKEN_MANAGER = new TokenManager();

    @Override
    public void addEndpoints()
    {
        ApiBuilder.get("natives", new NativesRoute());
        ApiBuilder.get("auth", new AuthRoute());
        ApiBuilder.get("version", new VersionRoute());
        ApiBuilder.get("integrity", new IntegrityRoute());
        ApiBuilder.get("download", new DownloadRoute());
    }
}
