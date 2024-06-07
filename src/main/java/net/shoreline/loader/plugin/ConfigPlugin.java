package net.shoreline.loader.plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.shoreline.loader.Loader;
import net.shoreline.loader.Natives;
import net.shoreline.loader.impl.ClientLoader;
import net.shoreline.loader.impl.natives.NativeLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        List<String> mixins = new ArrayList<>();

        String mixinConfig = new String((byte[]) Natives.stop_decompiling_1(this));

        JsonObject configObj = JsonParser.parseString(mixinConfig).getAsJsonObject();
        JsonArray clientMixins = configObj.getAsJsonArray("client");

        for (JsonElement mixin : clientMixins)
        {
            mixins.add(mixin.getAsString());
        }

        return mixins;
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
