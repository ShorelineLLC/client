package net.shoreline.loader.impl.antidump;

import net.shoreline.loader.impl.antidump.measures.*;

import java.util.Set;

public abstract class Measure
{
    public abstract void execute() throws Throwable;

    public static void runAllMeasures() throws Throwable
    {
        Set<Measure> measures = Set.of(
                new AntiClassSaveDebug(),
                new AntiVirtualMachine(),
                new ClassPresenceChecker(),
                new ClassReflectionDisabler(),
                new LaunchArgumentChecker()
        );

        for (Measure measure : measures)
        {
            measure.execute();
        }
    }
}
