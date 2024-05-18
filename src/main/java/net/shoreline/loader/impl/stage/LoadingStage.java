package net.shoreline.loader.impl.stage;

import net.shoreline.loader.context.UserContext;

public abstract class LoadingStage
{
    public abstract void run() throws Throwable;

    public abstract void error(UserContext context,
                               Throwable throwable);

    public abstract LoadingStage next();
}
