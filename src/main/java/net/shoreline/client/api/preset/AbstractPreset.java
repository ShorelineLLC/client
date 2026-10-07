package net.shoreline.client.api.preset;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigContainer;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.file.IOUtils;
import net.shoreline.client.api.file.JsonConfigFile;
import net.shoreline.client.impl.file.ModConfiguration;
import net.shoreline.loader.Loader;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class AbstractPreset<T extends ConfigContainer> extends JsonConfigFile
{
    protected final List<T> containers;

    public AbstractPreset(String name, List<T> containers) throws IOException
    {
        super(Shoreline.CONFIG.getConfigsDirectory(), name);
        this.containers = containers;
    }

    @Override
    public void saveFile() throws IOException
    {
        JsonArray array = new JsonArray();
        for (T container : containers)
        {
            JsonObject object = container.toJson();
            JsonArray configs = new JsonArray();
            apply(container, object);

            for (Config<?> config : container.getConfigs())
            {
                if (config instanceof ConfigGroup || config.getName().equalsIgnoreCase("Keybind"))
                {
                    continue;
                }

                configs.add(config.toJson());
            }

            object.add("configs", configs);
            array.add(object);
        }

        IOUtils.writeFile(getFilepath(), GSON.toJson(array));
    }

    @Override
    public void loadFile() throws IOException
    {
        Path path = getFilepath();
        if (!Files.exists(path))
        {
            return;
        }

        JsonArray array = parseJson(IOUtils.readFile(path), JsonArray.class);
        if (array == null)
        {
            return;
        }

        for (JsonElement element : array)
        {
            JsonObject object = element.getAsJsonObject();
            String id = object.get("id").getAsString();
            T container = containers.stream()
                    .filter(c -> id.equalsIgnoreCase(c.getId()))
                    .findFirst()
                    .orElse(null);

            if (container == null)
            {
                continue;
            }

            load(container, object);
            JsonArray configArray = object.getAsJsonArray("configs");
            for (JsonElement configElement : configArray)
            {
                JsonObject configJson = configElement.getAsJsonObject();
                String configId = configJson.get("id").getAsString();
                Config<?> config = container.getConfigs().stream()
                        .filter(c -> configId.equalsIgnoreCase(c.getId()))
                        .findFirst()
                        .orElse(null);

                if (config == null)
                {
                    continue;
                }

                try
                {
                    updateConfigFromJson(config, configJson.get("value"));
                }
                catch (Exception e)
                {
                    Loader.error(String.format("Failed to set value for Config: %s", config.getName()), e);
                }
            }
        }
    }

    protected abstract void apply(T container, JsonObject object);

    protected abstract void load(T container, JsonObject object);

    @SuppressWarnings("unchecked")
    protected void updateConfigFromJson(Config<?> config, JsonElement value)
    {
        if (config.getValue() instanceof Boolean)
        {
            ((Config<Boolean>) config).setValue(value.getAsBoolean());
        }
        else if (config.getValue() instanceof Enum<?>)
        {
            try
            {
                ((Config<Enum<?>>) config).setValue(Enum.valueOf(((Enum<?>) config.getValue()).getDeclaringClass(), value.getAsString()));
            }
            catch (IllegalArgumentException ignored)
            {
                ignored.printStackTrace();
            }
        }
        else if (config.getValue() instanceof Float)
        {
            ((NumberConfig) config).setValue(value.getAsFloat());
        }
        else if (config.getValue() instanceof Double)
        {
            ((NumberConfig) config).setValue(value.getAsDouble());
        }
        else if (config.getValue() instanceof Integer)
        {
            ((NumberConfig) config).setValue(value.getAsInt());
        }
        else if (config.getValue() instanceof String)
        {
            ((Config<String>) config).setValue(value.getAsString());
        }
        else if (config.getValue() instanceof Color)
        {
            ((Config<Color>) config).setValue(new Color((int) Long.parseLong(value.getAsString(), 16), true));
        }
    }
}
