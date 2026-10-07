package net.shoreline.client.impl.file;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.shoreline.client.api.file.IOUtils;
import net.shoreline.client.api.file.JsonConfigFile;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.api.macro.ModuleKeybind;
import net.shoreline.client.impl.Managers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class MacroFile extends JsonConfigFile
{
    public MacroFile(Path directory) throws IOException
    {
        super(directory, "macros");
    }

    @Override
    public void saveFile() throws IOException
    {
        JsonObject object = new JsonObject();
        for (Macro macro : Managers.MACROS.getMacros())
        {
            if (macro instanceof ModuleKeybind)
            {
                continue;
            }

            object.add(macro.getName(), macro.toJson());
        }

        IOUtils.writeFile(getFilepath(), GSON.toJson(object));
    }

    @Override
    public void loadFile() throws IOException
    {
        Path filepath = getFilepath();
        if (!Files.exists(filepath))
        {
            return;
        }

        JsonObject object = parseJson(IOUtils.readFile(filepath), JsonObject.class);
        if (object == null)
        {
            return;
        }

        for (Map.Entry<String, JsonElement> entry : object.entrySet())
        {
            String name = entry.getKey();
            JsonObject mObject = entry.getValue().getAsJsonObject();

            int keyCode = mObject.get("keycode").getAsInt();
            String cmd = mObject.get("command").getAsString();
            Macro macro = new Macro(name, keyCode, cmd);
            Managers.MACROS.register(macro);
        }
    }
}
