package net.shoreline.server.route.frontend;

import io.javalin.apibuilder.ApiBuilder;
import io.javalin.apibuilder.EndpointGroup;
import net.shoreline.server.route.frontend.routes.*;

public final class FrontendEndpoint implements EndpointGroup
{
    @Override
    public void addEndpoints()
    {
        ApiBuilder.get("/", new GenericPageRoute("index"));
        ApiBuilder.get("/login", new GenericPageRoute("login"));
        ApiBuilder.get("/purchase", new GenericPageRoute("purchase"));
        ApiBuilder.get("/register", new GenericPageRoute("register"));
        ApiBuilder.get("/changelogs", new GenericPageRoute("changelogs"));
        ApiBuilder.get("/tos", new GenericPageRoute("tos"));
        ApiBuilder.get("/reset-password", new GenericPageRoute("reset-password"));
        ApiBuilder.get("/natives", new NativesRoute());

        ApiBuilder.get("/{asset}", new GenericAssetRoute(""));

        ApiBuilder.get("/_next/image", new ImageQueryRoute());
        ApiBuilder.get("/_next/static/<asset>", new GenericAssetRoute("_next/static/"));

        ApiBuilder.get("/images/<asset>", new GenericAssetRoute("images/"));

        ///home/container/assets/webserver/_next/static/chunks/app/layout-d7020f5df63f5a58.js
    }
}
