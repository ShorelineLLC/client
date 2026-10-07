package net.shoreline.client.impl.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.shoreline.client.api.file.IOUtils;
import net.shoreline.client.api.file.JsonConfigFile;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ToggleStateFile extends JsonConfigFile
{
    public ToggleStateFile(Path directory) throws IOException
    {
        super(directory, "toggled");
    }

    @Override
    public void saveFile() throws IOException
    {
        final JsonArray moduleArray = new JsonArray();
        for (Module module : Managers.MODULES.getAllModules())
        {
            if (module instanceof Toggleable toggleable && toggleable.isEnabled())
            {
                moduleArray.add(toggleable.getId());
            }
        }

        IOUtils.writeFile(getFilepath(), GSON.toJson(moduleArray));
    }

    @Override
    public void loadFile() throws IOException
    {
        Path filepath = getFilepath();
        if (!Files.exists(filepath))
        {
            return;
        }

        JsonArray object = parseJson(IOUtils.readFile(filepath), JsonArray.class);
        if (object == null)
        {
            return;
        }

        for (JsonElement element : object.getAsJsonArray())
        {
            String id = element.getAsString();
            Toggleable module = (Toggleable) Managers.MODULES.getModule(id);
            if (module == null)
            {
                continue;
            }

            module.enable();
        }
    }
}
