package net.shoreline.client.api.file;

import com.google.gson.*;
import net.shoreline.client.Shoreline;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.apache.logging.log4j.core.util.IOUtils.EOF;

/**
 * @author linus
 * @since 1.0
 */
public abstract class ConfigFile
{
    //
    protected static final Gson GSON = new GsonBuilder()
            .setLenient() // leniency to allow for .cfg files
            .setPrettyPrinting()
            .create();
    // The UNIX filepath to configuration file. This filepath is always
    // within the client directory.
    private final String fileName;
    private final Path filepath;

    /**
     * @param path
     */
    public ConfigFile(Path dir, String path)
    {
        // create directory
        if (!Files.exists(dir))
        {
            try
            {
                Files.createDirectory(dir);
            }
            // create dir error
            catch (IOException e)
            {
                Shoreline.error("Could not create {} dir", dir);
                e.printStackTrace();
            }
        }
        fileName = dir.getFileName().toString();
        filepath = dir.resolve(toJsonPath(path));
    }

    /**
     * @param path
     * @return
     * @throws IOException
     */
    protected String read(Path path) throws IOException
    {
        StringBuilder content = new StringBuilder();
        InputStream in = Files.newInputStream(path);
        int b;
        while ((b = in.read()) != EOF)
        {
            content.append((char) b);
        }
        in.close();
        return content.toString();
    }

    /**
     * @param obj
     * @return
     */
    protected String serialize(Object obj)
    {
        return GSON.toJson(obj);
    }

    /**
     * @param json
     * @return
     */
    protected JsonObject parseObject(String json)
    {
        return parse(json, JsonObject.class);
    }

    /**
     * @param json
     * @return
     */
    protected JsonArray parseArray(String json)
    {
        return parse(json, JsonArray.class);
    }

    /**
     * @param json
     * @param type
     * @param <T>
     * @return
     */
    protected <T> T parse(String json, Class<T> type)
    {
        try
        {
            return GSON.fromJson(json, type);
        }
        catch (JsonSyntaxException e)
        {
            Shoreline.error("Invalid json syntax!");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param path
     * @param content
     * @throws IOException
     */
    protected void write(Path path, String content) throws IOException
    {
        OutputStream out = Files.newOutputStream(path);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        out.write(bytes, 0, bytes.length);
        out.close();
    }

    public String getFileName()
    {
        return fileName;
    }

    /**
     * Returns the <tt>UNIX</tt> {@link Path} to configuration file in the
     * client directory.
     *
     * @return The path to the file
     * @see #filepath
     */
    public Path getFilepath()
    {
        return filepath;
    }

    /**
     * Saves the configuration to a <tt>.json</tt> file in the local
     * <tt>Caspian</tt> directory
     */
    public abstract void save();

    /**
     * Loads the configuration from the associated <tt>.json</tt> file
     */
    public abstract void load();

    /**
     * @param fileName
     * @return
     */
    private String toJsonPath(String fileName)
    {
        return String.format("%s.json", fileName).toLowerCase();
    }
}
