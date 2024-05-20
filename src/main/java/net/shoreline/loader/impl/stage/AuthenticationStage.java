package net.shoreline.loader.impl.stage;

import net.shoreline.loader.Loader;
import net.shoreline.loader.Natives;
import net.shoreline.loader.context.UserContext;

public final class AuthenticationStage extends LoadingStage
{
    private static final AuthenticationStage instance = new AuthenticationStage();

    @Override
    public void run() throws Throwable
    {
        String res = (String) Natives.stop_decompiling_5(this);

        String[] user = res.split(":");

        Loader.getContext()
                .setUsername(user[0])
                .setUid(user[1]);

        Loader.LOGGER.info("Welcome, {}!", Loader.getContext().username());
    }

    @Override
    public void error(UserContext context,
                      Throwable throwable)
    {
        throw new RuntimeException(throwable);
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
