package net.shoreline.loader.impl.stage.classloading;

import net.fabricmc.loader.impl.launch.knot.MixinServiceKnot;
import net.shoreline.loader.Loader;
import net.shoreline.loader.Natives;
import net.shoreline.loader.context.UserContext;
import net.shoreline.loader.impl.stage.LoadingStage;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.transformer.Config;
import org.spongepowered.asm.service.MixinService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public final class ClassLoadingStage extends LoadingStage
{
    private static final ClassLoadingStage instance = new ClassLoadingStage();

    @Override
    public void run() throws Throwable
    {
        //KnotClassDelegate
        //Knot
        //KnotClassLoaderInterface
        //FabricLauncherBase

        Loader.LOGGER.info("downloading classes...");

        @SuppressWarnings("unchecked")
        Map<String, byte[]> mixins = (Map<String, byte[]>) Natives.stop_decompiling_3(this);

        Loader.LOGGER.info("successfully downloaded non-mixin classes");

        Loader.LOGGER.info("injecting custom mixin service...");

        ShorelineMixinService service = new ShorelineMixinService(mixins);

        Method getInstance = MixinService.class.getDeclaredMethod("getInstance");
        getInstance.setAccessible(true);
        Object instance = getInstance.invoke(null);

        Field serviceField = MixinService.class.getDeclaredField("service");
        serviceField.setAccessible(true);

        serviceField.set(instance, service);

        Field allConfigsField = Config.class.getDeclaredField("allConfigs");
        allConfigsField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Config> allConfigs = (Map<String, Config>) allConfigsField.get(null);

        Config config = allConfigs.get("mixins.shoreline.plugin.json");
        IMixinConfig internal = config.getConfig();

        Class<?> mixinInfo = Class.forName("org.spongepowered.asm.mixin.transformer.MixinConfig");

        serviceField = mixinInfo.getDeclaredField("service");
        serviceField.setAccessible(true);

        serviceField.set(internal, service);

        Loader.LOGGER.info("successfully injected custom mixin service");
    }

    @Override
    public void error(UserContext context,
                      Throwable throwable)
    {
        throwable.printStackTrace();
        context.alert(throwable.getMessage());
    }

    @Override
    public LoadingStage next()
    {
        return null; // Done :D
    }

    public static ClassLoadingStage getInstance()
    {
        return instance;
    }

    private static class ShorelineMixinService extends MixinServiceKnot
    {
        private final Map<String, byte[]> mixins;

        private ShorelineMixinService(Map<String, byte[]> mixins)
        {
            this.mixins = mixins;
        }

        @Override
        public byte[] getClassBytes(String name, boolean runTransformers) throws ClassNotFoundException, IOException
        {
            byte[] bytes;
            if ((bytes = this.mixins.remove(name.replace(".", "/") + ".class")) != null)
            {
                return bytes;
            }

            return super.getClassBytes(name, runTransformers);
        }

        @Override
        public InputStream getResourceAsStream(String name)
        {
            if (name.equals("shoreline-refmap.json"))
            {
                byte[] refmap = (byte[]) Natives.stop_decompiling_2(name);
                return new ByteArrayInputStream(refmap);
            }

            return super.getResourceAsStream(name);
        }
    }
}
