package net.shoreline.loader.impl.stage.antidump;

import net.shoreline.loader.context.UserContext;
import net.shoreline.loader.impl.stage.classloading.ClassLoadingStage;
import net.shoreline.loader.impl.stage.LoadingStage;

public final class AntiDumpStage extends LoadingStage
{
    private static final AntiDumpStage instance = new AntiDumpStage();

    @Override
    public void run() throws Throwable
    {
        Measure.runAllMeasures();
    }

    @Override
    public void error(UserContext context,
                      Throwable throwable)
    {
        context.alert(throwable.getMessage());
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
