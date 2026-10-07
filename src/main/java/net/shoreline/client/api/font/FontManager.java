package net.shoreline.client.api.font;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FontManager
{
    private static final Map<String, FontRenderer> AVAILABLE_FONTS = new HashMap<>();

    public static FontRenderer FONT_RENDERER;
    private static boolean initialized;

    public static void init()
    {
        for (Fonts f : Fonts.values())
        {
            AVAILABLE_FONTS.put(f.getName(), new FontRenderer(f.getName(), f.getSize()));
        }

        FONT_RENDERER = fromSystem("Verdana");
        initialized = true;
    }

    public static void close()
    {
        if (initialized)
        {
            for (FontRenderer renderer : AVAILABLE_FONTS.values())
            {
                renderer.close();
            }
        }
    }

    public static void setFont(FontRenderer fontRenderer)
    {
        FONT_RENDERER = fontRenderer;
    }

    public static FontRenderer fromSystem(String name)
    {
        return AVAILABLE_FONTS.get(name);
    }

    public static FontRenderer fromResource(String resPath, float pxHeight)
    {
        try (InputStream in = FontManager.class.getResourceAsStream(resPath))
        {
            if (in == null)
            {
                throw new IllegalArgumentException("No resource " + resPath);
            }

            return new FontRenderer(in, pxHeight);

        } catch (IOException ioe)
        {
            throw new RuntimeException("Unable to load font " + resPath, ioe);
        }
    }
}
