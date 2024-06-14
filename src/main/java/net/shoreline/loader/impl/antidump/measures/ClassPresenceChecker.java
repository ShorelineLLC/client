package net.shoreline.loader.impl.antidump.measures;

import net.shoreline.loader.impl.antidump.Measure;

public final class ClassPresenceChecker extends Measure
{
    @Override
    public void execute() throws Throwable
    {
        crash_block:
        {
            try
            {
                Class.forName("net.shoreline.client.ShorelineMod");
            } catch (Throwable t)
            {
                break crash_block;
            }

            throw new Throwable("Shoreline client class present too early.");
        }
    }
}
