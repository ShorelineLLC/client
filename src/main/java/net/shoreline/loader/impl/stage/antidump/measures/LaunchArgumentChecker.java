package net.shoreline.loader.impl.stage.antidump.measures;

import net.shoreline.loader.impl.stage.antidump.Measure;

import java.lang.management.ManagementFactory;

public final class LaunchArgumentChecker extends Measure
{
    @Override
    public void execute() throws Throwable
    {
        String[] disallowedArgs = {
                "-XBootclasspath",
                "-javaagent",
                "-Xdebug",
                "-agentlib",
                "-Xrunjdwp",
                "-Xnoagent",
                "-verbose",
                "-noverify",
                "-Xverify:none",
                "-DproxySet",
                "-DproxyHost",
                "-DproxyPort",
                "-Djavax.net.ssl.trustStore",
                "-Djavax.net.ssl.trustStorePassword"
        };
        for (String arg : disallowedArgs)
        {
            for (String inArg : ManagementFactory.getRuntimeMXBean().getInputArguments())
            {
                if (inArg.contains(arg))
                {
                    throw new Throwable("Strange launch arg: " + arg);
                }
            }
        }
    }
}
