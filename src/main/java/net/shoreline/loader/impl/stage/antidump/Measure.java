package net.shoreline.loader.impl.stage.antidump;

import net.shoreline.loader.impl.stage.antidump.measures.AntiClassSaveDebug;
import net.shoreline.loader.impl.stage.antidump.measures.ClassPresenceChecker;
import net.shoreline.loader.impl.stage.antidump.measures.ClassReflectionDisabler;
import net.shoreline.loader.impl.stage.antidump.measures.LaunchArgumentChecker;

import java.util.Set;

public abstract class Measure
{
    public abstract void execute() throws Throwable;

    public static void runAllMeasures() throws Throwable
    {
        Set<Measure> measures = Set.of(
                new AntiClassSaveDebug(),
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
