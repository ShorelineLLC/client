package net.shoreline.loader.impl.classloading;

import net.fabricmc.loader.impl.launch.knot.MixinServiceKnot;
import net.shoreline.loader.Natives;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public final class ShorelineMixinService extends MixinServiceKnot
{
    private final Map<String, byte[]> mixins;

    @SuppressWarnings("unchecked")
    ShorelineMixinService(Map<String, byte[]> mixins)
    {
        // Add all loader class bytecode necessary
        mixins.putAll((Map<String, byte[]>) Natives.m(mixins));
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
            byte[] refmap = (byte[]) Natives.c(name);
            return new ByteArrayInputStream(refmap);
        }

        return super.getResourceAsStream(name);
    }


}