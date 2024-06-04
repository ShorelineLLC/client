package net.shoreline.client.api.social;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.file.ConfigFile;
import net.shoreline.client.init.Managers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author linus
 * @since 1.0
 */
public class SocialFile extends ConfigFile
{
    //
    private final SocialRelation relation;

    /**
     * @param dir
     * @param relation
     */
    public SocialFile(Path dir, SocialRelation relation)
    {
        super(dir, relation.name());
        this.relation = relation;
    }

    /**
     *
     */
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
            final JsonArray array = new JsonArray();
            for (String socials : Managers.SOCIAL.getRelations(relation))
            {
                array.add(new JsonPrimitive(socials));
            }
            write(filepath, serialize(array));
        }
        // error writing file
        catch (IOException e)
        {
            Shoreline.error("Could not save file for {}.json!",
                    relation.name().toLowerCase());
            e.printStackTrace();
        }
    }

    /**
     *
     */
    @Override
    public void load()
    {
        try
        {
            Path filepath = getFilepath();
            if (Files.exists(filepath))
            {
                final String content = read(filepath);
                JsonArray json = parseArray(content);
                if (json == null)
                {
                    return;
                }
                for (JsonElement element : json.asList())
                {
                    Managers.SOCIAL.addRelation(element.getAsString(), relation);
                }
            }
        }
        // error reading file
        catch (IOException e)
        {
            Shoreline.error("Could not read file for {}.json!",
                    relation.name().toLowerCase());
            e.printStackTrace();
        }
    }
}
