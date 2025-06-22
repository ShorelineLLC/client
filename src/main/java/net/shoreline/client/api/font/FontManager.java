package net.shoreline.client.api.font;

import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FontManager
{
    private static FreeTypeLibrary FT;
    public static Font FONT;

    public static void init()
    {
        FT = new FreeTypeLibrary();
        FONT = fromResource(FT, "/assets/shoreline/font/verdana.ttf", 9);
        FontScalingRegistry.register(FONT);
    }

    public void loadFont(Identifier identifier)
    {

    }

    public static Font fromResource(FreeTypeLibrary ft, String resPath, int pxHeight)
    {
        try (InputStream in = FontManager.class.getResourceAsStream(resPath))
        {
            if (in == null)
            {
                throw new IllegalArgumentException("No resource " + resPath);
            }

            Path tmp = Files.createTempFile("font", ".ttf");
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            tmp.toFile().deleteOnExit();

            return new Font(ft, tmp.toString(), 0, pxHeight);

        } catch (IOException ioe)
        {
            throw new RuntimeException("Unable to load font " + resPath, ioe);
        }
    }
}
