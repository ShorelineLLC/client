package net.shoreline.client.impl.gui.click;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.file.ConfigFile;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.gui.click.impl.config.CategoryFrame;
import net.shoreline.client.impl.module.client.ClickGuiModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClickGuiFile extends ConfigFile
{
    public ClickGuiFile(Path dir)
    {
        super(dir, "clickgui");
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
            JsonArray object = new JsonArray();
            for (CategoryFrame frame : ClickGuiModule.CLICK_GUI_SCREEN.getCategoryFrames())
            {
                object.add(frame.toJson());
            }
            write(filepath, serialize(object));
        }
        // error writing file
        catch (IOException e)
        {
            Shoreline.error("Could not save clickgui file!");
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
                String content = read(filepath);
                JsonArray object = parseArray(content);
                for (JsonElement element : object.getAsJsonArray())
                {
                    JsonObject jsonObject = element.getAsJsonObject();
                    if (jsonObject.has("category"))
                    {
                        JsonElement category = jsonObject.get("category");
                        CategoryFrame frame = getFrame(ModuleCategory.valueOf(category.getAsString()));
                        frame.fromJson(jsonObject);
                    }
                }
            }
        }
        // error reading file
        catch (IOException e)
        {
            Shoreline.error("Could not read clickgui file!");
            e.printStackTrace();
        }
    }

    public CategoryFrame getFrame(ModuleCategory category)
    {
        for (CategoryFrame frame : ClickGuiModule.CLICK_GUI_SCREEN.getCategoryFrames())
        {
            if (frame.getCategory().equals(category))
            {
                return frame;
            }
        }
        return null;
    }
}
