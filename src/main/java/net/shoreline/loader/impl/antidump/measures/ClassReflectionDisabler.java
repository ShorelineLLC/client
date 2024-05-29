package net.shoreline.loader.impl.antidump.measures;

import net.shoreline.loader.Loader;
import net.shoreline.loader.Natives;
import net.shoreline.loader.asm.ClassScanner;
import net.shoreline.loader.context.UserContext;
import net.shoreline.loader.context.UserType;
import net.shoreline.loader.impl.ClientLoader;
import net.shoreline.loader.impl.antidump.Measure;
import net.shoreline.loader.impl.classloading.ClassLoader;
import net.shoreline.loader.impl.classloading.ShorelineMixinService;
import net.shoreline.loader.impl.natives.NativeLoader;
import net.shoreline.loader.plugin.ConfigPlugin;

import java.util.Set;

public final class ClassReflectionDisabler extends Measure
{
    @Override
    public void execute() throws Throwable
    {
        Set.of(
                ClassScanner.class,

                UserContext.class,
                UserType.class,

                AntiClassSaveDebug.class,
                AntiVirtualMachine.class,
                ClassPresenceChecker.class,
                ClassReflectionDisabler.class,
                LaunchArgumentChecker.class,

                Measure.class,

                ClassLoader.class,
                ShorelineMixinService.class,

                NativeLoader.class,

                ClientLoader.class,

                ConfigPlugin.class,

                Loader.class,
                Natives.class
        ).forEach(Natives::stop_decompiling_8);
    }
}
