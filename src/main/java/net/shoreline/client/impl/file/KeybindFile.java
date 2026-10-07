package net.shoreline.client.impl.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.shoreline.client.api.file.IOUtils;
import net.shoreline.client.api.file.JsonConfigFile;
import net.shoreline.client.api.macro.HoldKeybind;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.api.macro.ModuleKeybind;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class KeybindFile extends JsonConfigFile
{
    public KeybindFile(Path directory) throws IOException
    {
        super(directory, "keybinds");
    }

    @Override
    public void saveFile() throws IOException
    {
        final JsonArray macroArray = new JsonArray();
        for (Macro macro : Managers.MACROS.getMacros())
        {
            if (macro instanceof ModuleKeybind keybind)
            {
                macroArray.add(keybind.toJson());
            }
        }
        IOUtils.writeFile(getFilepath(), GSON.toJson(macroArray));
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
            JsonObject jsonObject = element.getAsJsonObject();
            if (jsonObject.has("module") && jsonObject.has("keycode"))
            {
                JsonElement moduleId = jsonObject.get("module");
                JsonElement keycode = jsonObject.get("keycode");
                Toggleable module = (Toggleable) Managers.MODULES.getModule(moduleId.getAsString());
                if (module == null)
                {
                    continue;
                }

                int key = keycode.getAsInt();
                ModuleKeybind keybind =  jsonObject.has("hold") && jsonObject.get("hold").getAsBoolean() ?
                        new HoldKeybind(key, module) : new ModuleKeybind(key, module);

                module.setKeybind(keybind);
            }
        }
    }
}
