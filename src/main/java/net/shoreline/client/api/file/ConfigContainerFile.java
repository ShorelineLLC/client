package net.shoreline.client.api.file;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigContainer;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigContainerFile extends JsonConfigFile
{
    private final ConfigContainer container;

    public ConfigContainerFile(Path directory, ConfigContainer container) throws IOException
    {
        super(directory, container.getName().toLowerCase());
        this.container = container;
    }

    @Override
    public void saveFile() throws IOException
    {
        JsonObject jsonObject = container.toJson();
        IOUtils.writeFile(getFilepath(), GSON.toJson(jsonObject));
    }

    @Override
    public void loadFile() throws IOException
    {
        Path filepath = getFilepath();
        if (!Files.exists(filepath))
        {
            return;
        }

        JsonObject jsonObject = parseJson(IOUtils.readFile(filepath), JsonObject.class);
        if (jsonObject == null || !jsonObject.has("configs"))
        {
            return;
        }

        JsonElement element = jsonObject.get("configs");
        for (JsonElement element1 : element.getAsJsonArray())
        {
            final JsonObject configObj = element1.getAsJsonObject();
            if (!configObj.has("id") || !configObj.has("value"))
            {
                continue;
            }
            final JsonElement id = configObj.get("id");
            Config<?> config = container.getConfig(id.getAsString());
            if (config == null)
            {
                continue;
            }
            updateConfigFromJson(config, configObj);
        }
    }

    private void updateConfigFromJson(Config<?> config, JsonObject jsonObject)
    {
        final JsonElement value = jsonObject.get("value");
        if (config.getValue() instanceof Boolean)
        {
            ((Config<Boolean>) config).setValue(value.getAsBoolean());
        } else if (config.getValue() instanceof Enum<?>)
        {
            try
            {
                ((Config<Enum<?>>) config).setValue((Enum<?>) Enum.valueOf((Class<Enum>) config.getValue().getClass(), value.getAsString()));
            } catch (IllegalArgumentException ignored)
            {

            }
        } else if (config.getValue() instanceof Float)
        {
            ((Config<Float>) config).setValue(value.getAsFloat());
        } else if (config.getValue() instanceof Double)
        {
            ((Config<Double>) config).setValue(value.getAsDouble());
        } else if (config.getValue() instanceof Integer)
        {
            ((Config<Integer>) config).setValue(value.getAsInt());
        } else if (config.getValue() instanceof String)
        {
            ((Config<String>) config).setValue(value.getAsString());
        }
        else if (config.getValue() instanceof Color)
        {
            ((Config<Color>) config).setValue(new Color((int) Long.parseLong(value.getAsString(), 16), true));
        }
    }
}
