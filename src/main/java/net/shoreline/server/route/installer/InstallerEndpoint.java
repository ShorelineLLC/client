package net.shoreline.server.route.installer;

import io.javalin.apibuilder.ApiBuilder;
import io.javalin.apibuilder.EndpointGroup;
import net.shoreline.server.route.installer.routes.*;

public final class InstallerEndpoint implements EndpointGroup
{
    @Override
    public void addEndpoints()
    {
        ApiBuilder.post("login", new LoginRoute());
        ApiBuilder.post("logout", new LogoutRoute());

        ApiBuilder.get("install", new InstallRoute());
        ApiBuilder.get("natives", new NativesRoute());
        ApiBuilder.get("fabric", new FabricRoute());
        ApiBuilder.get("baritone", new BaritoneRoute());
        ApiBuilder.get("vulture", new VultureRoute());
    }
}
