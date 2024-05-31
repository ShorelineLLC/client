package net.shoreline.loader.impl.antidump.measures;

import net.shoreline.loader.impl.antidump.Measure;

import java.lang.management.ManagementFactory;

public final class LaunchArgumentChecker extends Measure
{
    @Override
    public void execute() throws Throwable
    {
        String[] disallowedArgs = {
                "-Xbootclasspath",
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
                "-Dhttp.proxyHost",
                "-Dhttp.proxyPort",
                "-Dhttp.proxySet",
                "-Dhttps.proxyHost",
                "-Dhttps.proxyPort",
                "-Dhttps.proxySet",
                "-Djavax.net.ssl.trustStore",
                "-Djavax.net.ssl.trustStorePassword",
                "-Dcom.sun.management.jmxremote",
                "-Dcom.sun.management.jmxremote.port",
                "-Dcom.sun.management.jmxremote.authenticate",
                "-Dcom.sun.management.jmxremote.ssl",
                "-Dlegacy.debugClassLoading",
                "-Dlegacy.debugClassLoadingFiner",
                "-Dlegacy.debugClassLoadingSave",
                "-XX:+PrintClassHistogram",
                "-XX:+PrintClassHistogramAfterFullGC",
                "-XX:+TraceClassLoading",
                "-XX:+TraceClassUnloading",
                "-XX:TraceClassLoadingPreorder",
                "-XX:TraceClassUnloadingPreorder"
        };

        for (String arg : disallowedArgs)
        {
            for (String inArg : ManagementFactory.getRuntimeMXBean().getInputArguments())
            {
                if (inArg.toLowerCase().contains(arg.toLowerCase()))
                {
                    throw new Throwable("Strange launch arg: " + arg);
                }
            }
        }
    }
}
