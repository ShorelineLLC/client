package net.shoreline.loader.impl.stage;

import net.shoreline.loader.context.UserContext;

public final class AntiDumpStage extends LoadingStage
{
    private static final AntiDumpStage instance = new AntiDumpStage();

    @Override
    public void run() throws Throwable
    {

    }

    @Override
    public void error(UserContext context,
                      Throwable throwable)
    {

    }

    @Override
    public LoadingStage next()
    {
        return ClassLoadingStage.getInstance();
    }

    public static AntiDumpStage getInstance()
    {
        return instance;
    }
}
