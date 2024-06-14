package net.shoreline.loader.plugin;

import net.shoreline.loader.Loader;
import net.shoreline.loader.Natives;
import net.shoreline.loader.impl.ClientLoader;
import net.shoreline.loader.impl.natives.NativeLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.*;
import java.util.stream.Collectors;

public final class ConfigPlugin implements IMixinConfigPlugin
{
    @Override
    public void onLoad(String mixinPackage)
    {
        Loader.info("Loading Shoreline...");

        try
        {
            NativeLoader.load();
        } catch (Throwable t)
        {
            Loader.error("Encountered an error loading Shoreline natives.", t);
            NativeLoader.crashNotNatively();
        }

        NativeLoader.setUserCredentials();

        Natives.stop_decompiling_6(Loader.VERSION);

        ClientLoader.loadClient();
    }

    @Override
    public String getRefMapperConfig()
    {
        return "shoreline-refmap.json";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName,
                                    String mixinClassName)
    {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets,
                              Set<String> otherTargets)
    {
    }

    @Override
    public List<String> getMixins()
    {
        HashSet<?> mixinSet = (HashSet<?>) Natives.stop_decompiling_1(this);

        return mixinSet.stream()
                .map(obj ->
                {
                    String mixin = (String) obj;
                    mixin = mixin.replace("/", ".");
                    mixin = mixin.substring("net.shoreline.client.".length());
                    return  mixin.substring(0, mixin.length() - ".class".length());
                }).toList();
    }

    @Override
    public void preApply(String targetClassName,
                         ClassNode targetClass,
                         String mixinClassName,
                         IMixinInfo mixinInfo)
    {
    }

    @Override
    public void postApply(String targetClassName,
                          ClassNode targetClass,
                          String mixinClassName,
                          IMixinInfo mixinInfo)
    {
    }
}
