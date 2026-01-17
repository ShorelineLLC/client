package net.shoreline.client.api.file;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.config.*;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
            if (config == null || config instanceof ConfigGroup)
            {
                continue;
            }

            final JsonElement value = configObj.get("value");
            if (value != null)
            {
                updateConfigFromJson(config, value);
            }
        }
    }

    private void updateConfigFromJson(Config<?> config, JsonElement value)
    {
        try
        {
            if (config.getValue() instanceof Boolean)
            {
                ((Config<Boolean>) config).setValue(value.getAsBoolean());
            } else if (config.getValue() instanceof Enum<?>)
            {
                ((Config<Enum<?>>) config).setValue(Enum.valueOf(((Enum<?>) config.getValue()).getDeclaringClass(), value.getAsString()));
            } else if (config.getValue() instanceof Float)
            {
                ((NumberConfig) config).setValue(value.getAsFloat());
            } else if (config.getValue() instanceof Double)
            {
                ((NumberConfig) config).setValue(value.getAsDouble());
            } else if (config.getValue() instanceof Integer)
            {
                ((NumberConfig) config).setValue(value.getAsInt());
            } else if (config.getValue() instanceof String)
            {
                ((Config<String>) config).setValue(value.getAsString());
            }
            else if (config.getValue() instanceof Color)
            {
                ColorConfig colorConfig = (ColorConfig) config;
                String str = value.getAsString();
                if (str.contains("-")) // remove this when u have updated ur configs linus
                {
                    String[] split = str.split("-");
                    if (split[0] != null)
                    {
                        Color color = new Color((int) Long.parseLong(split[0], 16), true);
                        colorConfig.setValue(color);
                    }

                    if (split[1] != null)
                    {
                        boolean global = Boolean.parseBoolean(split[1]);
                        colorConfig.setGlobal(global);
                    }
                }
                else
                {
                    // and this
                    if (str.equalsIgnoreCase("global"))
                    {
                        colorConfig.setGlobal(true);
                    }
                    else
                    {
                        Color color = new Color((int) Long.parseLong(str, 16), true);
                        colorConfig.setValue(color);
                    }
                }
            } else if (config.getValue() instanceof Collection<?>)
            {
                Set<Object> entries = new HashSet<>();
                for (JsonElement element : value.getAsJsonArray())
                {
                    Identifier id = Identifier.of(element.getAsString());
                    Object entry = ((RegistryConfig<?>) config).getRegistry().get(id);
                    entries.add(entry);
                }

                ((Config<Collection<?>>) config).setValue(entries);
            }

        } catch (IllegalArgumentException | InvalidIdentifierException e)
        {
            Shoreline.info("Failed to load config: " + config.getName());
            e.printStackTrace();
        }
    }
}
