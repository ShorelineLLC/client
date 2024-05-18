package net.shoreline.loader.impl.stage;

import net.shoreline.loader.context.UserContext;

public final class AuthenticationStage extends LoadingStage
{
    private static final AuthenticationStage instance = new AuthenticationStage();

    @Override
    public void run() throws Throwable
    {
        // Get user credentials
        // Build user context
    }

    @Override
    public void error(UserContext context,
                      Throwable throwable)
    {

    }

    @Override
    public LoadingStage next()
    {
        return AntiDumpStage.getInstance();
    }

    public static AuthenticationStage getInstance()
    {
        return instance;
    }
}
