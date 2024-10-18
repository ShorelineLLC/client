package net.shoreline.client.api.module.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.shoreline.client.BuildConfig;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.file.ConfigFile;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.init.Managers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModuleConfigFile extends ConfigFile
{

    /**
     * @param dir
     * @param path
     */
    public ModuleConfigFile(Path dir, String path)
    {
        super(dir, path);
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
            final JsonObject out = new JsonObject();
            out.addProperty("version", String.format("%s %s (%s%s%s)",
                    ShorelineMod.MOD_NAME, ShorelineMod.MOD_VER, BuildConfig.BUILD_IDENTIFIER,
                    !BuildConfig.BUILD_IDENTIFIER.equals("dev") ? "-" + BuildConfig.BUILD_NUMBER : "",
                    !BuildConfig.HASH.equals("null") ? "-" + BuildConfig.HASH : ""));
            final JsonArray array = new JsonArray();
            for (Module module : Managers.MODULE.getModules())
            {
                array.add(module.toJson());
            }
            out.add("configs", array);
            write(filepath, serialize(out));
        }
        // error writing file
        catch (IOException e)
        {
            // Shoreline.error("Could not save file for {}!", getFilepath());
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
                JsonObject in = parseObject(content);
                if (!in.has("configs"))
                {
                    return;
                }
                JsonArray array = in.getAsJsonArray("configs");
                for (JsonElement element : array.asList())
                {
                    JsonObject object = element.getAsJsonObject();
                    if (object.has("id"))
                    {
                        Module module = Managers.MODULE.getModuleById(object.get("id").getAsString());
                        if (module == null)
                        {
                            return;
                        }
                        module.fromJson(object);
                    }
                }
            }
        }
        // error writing file
        catch (IOException e)
        {
            // Shoreline.error("Could not read file for {}!", getFilepath());
            e.printStackTrace();
        }
    }
}
