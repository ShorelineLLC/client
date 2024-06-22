package net.shoreline.loader.impl.classloading;

import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.minecraft.resource.ResourcePack;
import net.shoreline.loader.Loader;
import net.shoreline.loader.Natives;
import net.shoreline.loader.context.UserContext;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.transformer.Config;
import org.spongepowered.asm.service.MixinService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public final class ClassLoader
{
    public static void loadAllClasses() throws Throwable
    {
        UserContext context = Loader.getContext();

        @SuppressWarnings("unchecked")
        Map<String, byte[]> mixins = (Map<String, byte[]>) Natives.d(context.getInformationArray());

        // Inject mixin service
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
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ShorelineResourcePack transformPack(ResourcePack pack)
    {
        if (pack instanceof ModNioResourcePack parent)
        {
            return new ShorelineResourcePack(parent);
        }

        // won't happen (pray to god)
        throw new IllegalStateException("pack not instance of ModNioResourcePack");
    }
}
