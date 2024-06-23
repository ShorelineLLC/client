package net.shoreline.loader.impl.antidump;

import net.shoreline.loader.Loader;
import net.shoreline.loader.impl.antidump.measures.*;

import java.util.Set;

public abstract class Measure
{
    public abstract void execute() throws Throwable;

    public static void runAllMeasures()
    {
        Set<Measure> measures = Set.of(
                new AntiClassSaveDebug(),
                new AntiVirtualMachine(),
                new ClassPresenceChecker(),
                new LaunchArgumentChecker()
        );

        for (Measure measure : measures)
        {
            try
            {
                measure.execute();
            } catch (Throwable t)
            {
                Loader.getContext().alert(t.getMessage());
            }
        }
    }
}
