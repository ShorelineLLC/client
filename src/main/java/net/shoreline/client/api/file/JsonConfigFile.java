package net.shoreline.client.api.file;

import com.google.gson.*;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class JsonConfigFile
{
    protected static final Gson GSON = new GsonBuilder()
            .setLenient() // leniency to allow for .cfg files
            .setPrettyPrinting()
            .create();

    @Getter
    private final Path filepath;

    public JsonConfigFile(Path directory, String filepath) throws IOException
    {
        this.filepath = directory.resolve(String.format("%s.cfg", filepath).toLowerCase());
        if (!Files.exists(this.filepath))
        {
            Files.createFile(this.filepath);
        }
    }

    protected <T> T parseJson(String json,
                              Class<T> type)
    {
        try
        {
            return GSON.fromJson(json, type);
        } catch (JsonSyntaxException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public abstract void saveFile() throws IOException;

    public abstract void loadFile() throws IOException;
}
