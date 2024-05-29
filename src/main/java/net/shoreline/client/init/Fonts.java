package net.shoreline.client.init;

import net.shoreline.client.Shoreline;
import net.shoreline.client.impl.font.AWTFontRenderer;
import net.shoreline.client.impl.font.VanillaTextRenderer;

public class Fonts {
    //
    public static final VanillaTextRenderer VANILLA = new VanillaTextRenderer();
    public static AWTFontRenderer CLIENT;
    //
    private static boolean initialized;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        CLIENT = new AWTFontRenderer(Shoreline.class.getClassLoader().getResourceAsStream("assets/shoreline/font/verdana.ttf"), 9.0f);
        Shoreline.info("Loaded fonts!");
        initialized = true;
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
