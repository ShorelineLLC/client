package net.shoreline.client.api.font;

import net.minecraft.util.Identifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FontManager
{
    public static FontRenderer FONT;

    public static void init()
    {
        FONT = fromResource("/assets/shoreline/font/verdana.ttf", 9.5f);
    }

    public void loadFont(Identifier identifier)
    {

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
