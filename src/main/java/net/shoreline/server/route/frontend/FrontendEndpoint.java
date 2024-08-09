package net.shoreline.server.route.frontend;

import io.javalin.apibuilder.ApiBuilder;
import io.javalin.apibuilder.EndpointGroup;
import net.shoreline.server.route.frontend.routes.*;
import net.shoreline.server.route.frontend.routes.assets.*;
import net.shoreline.server.route.frontend.routes.pages.*;
import net.shoreline.server.route.frontend.routes.styles.*;

public final class FrontendEndpoint implements EndpointGroup
{
    @Override
    public void addEndpoints()
    {
        ApiBuilder.get("/", new HomeRoute());
        ApiBuilder.get("/changelogs", new ChangelogsRoute());
        ApiBuilder.get("/login", new LoginRoute());
        ApiBuilder.get("/purchase", new PurchaseRoute());
        ApiBuilder.get("/register", new RegisterRoute());

        ApiBuilder.get("/pages/{page}", new PagesRoute());
        ApiBuilder.get("/styles/{style}", new StylesRoute());

        ApiBuilder.get("/assets/fonts/{font}", new FontsRoute());
        ApiBuilder.get("/assets/icons/{icon}", new IconsRoute());
        ApiBuilder.get("/assets/images/{image}", new ImagesRoute());
    }
}
