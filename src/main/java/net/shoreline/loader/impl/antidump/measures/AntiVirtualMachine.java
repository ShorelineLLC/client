package net.shoreline.loader.impl.antidump.measures;

import net.shoreline.loader.Loader;
import net.shoreline.loader.Natives;
import net.shoreline.loader.context.UserContext;
import net.shoreline.loader.impl.antidump.Measure;

public final class AntiVirtualMachine extends Measure
{
    @Override
    public void execute() throws Throwable
    {
        UserContext context = Loader.getContext();

        // Internal VM checks
        Natives.stop_decompiling_9(context.getInformationArray());
    }
}
