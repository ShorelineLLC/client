package net.shoreline.server.route.frontend;

import io.javalin.apibuilder.ApiBuilder;
import io.javalin.apibuilder.EndpointGroup;
import net.shoreline.server.route.frontend.routes.HomeRoute;
import net.shoreline.server.route.frontend.routes.assets.*;
import net.shoreline.server.route.frontend.routes.pages.PagesRoute;
import net.shoreline.server.route.frontend.routes.styles.StylesRoute;

public final class FrontendEndpoint implements EndpointGroup
{
    @Override
    public void addEndpoints()
    {
        ApiBuilder.get("/", new HomeRoute());

        ApiBuilder.get("/pages/{page}", new PagesRoute());
        ApiBuilder.get("/styles/{style}", new StylesRoute());

        ApiBuilder.get("/assets/fonts/{font}", new FontsRoute());
        ApiBuilder.get("/assets/icons/{icon}", new IconsRoute());
        ApiBuilder.get("/assets/images/{image}", new ImagesRoute());
    }
}
