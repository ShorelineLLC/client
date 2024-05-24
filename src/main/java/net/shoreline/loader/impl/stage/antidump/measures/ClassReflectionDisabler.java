package net.shoreline.loader.impl.stage.antidump.measures;

import net.shoreline.loader.Loader;
import net.shoreline.loader.Natives;
import net.shoreline.loader.asm.ClassScanner;
import net.shoreline.loader.context.UserContext;
import net.shoreline.loader.context.UserType;
import net.shoreline.loader.impl.ClientLoader;
import net.shoreline.loader.impl.natives.NativeLoader;
import net.shoreline.loader.impl.stage.LoadingStage;
import net.shoreline.loader.impl.stage.antidump.AntiDumpStage;
import net.shoreline.loader.impl.stage.antidump.Measure;
import net.shoreline.loader.impl.stage.authentication.AuthenticationStage;
import net.shoreline.loader.impl.stage.classloading.ClassLoadingStage;
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

                NativeLoader.class,

                ClassReflectionDisabler.class,

                AntiDumpStage.class,
                Measure.class,

                AuthenticationStage.class,

                ClassLoadingStage.class,

                LoadingStage.class,

                ClientLoader.class,

                ConfigPlugin.class,

                Loader.class,
                Natives.class
        ).forEach(Natives::stop_decompiling_8);
    }
}
