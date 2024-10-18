package net.shoreline.client.api.font;

import com.google.gson.JsonObject;
import net.shoreline.client.api.file.ConfigFile;
import net.shoreline.client.init.Fonts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FontFile extends ConfigFile
{
    public FontFile(Path dir)
    {
        super(dir, "fonts");
    }

    @Override
    public void save()
    {
        try
        {
            Path filepath = getFilepath();
            if (!Files.exists(filepath))
            {
                Files.createFile(filepath);
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("fontpath", Fonts.FONT_FILE_PATH);
            write(getFilepath(), serialize(jsonObject));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void load()
    {
        try
        {
            Path filepath = getFilepath();
            if (Files.exists(filepath))
            {
                final String content = read(filepath);
                JsonObject object = parseObject(content);
                Fonts.FONT_FILE_PATH = object.get("fontpath").getAsString();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
