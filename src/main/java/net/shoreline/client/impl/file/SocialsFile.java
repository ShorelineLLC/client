package net.shoreline.client.impl.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.shoreline.client.api.file.IOUtils;
import net.shoreline.client.api.file.JsonConfigFile;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.social.SocialManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class SocialsFile extends JsonConfigFile
{
    public SocialsFile(Path directory) throws IOException
    {
        super(directory, "socials");
    }

    @Override
    public void saveFile() throws IOException
    {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, SocialManager.SocialType> entry : Managers.SOCIAL.getSocials().entrySet())
        {
            object.addProperty(entry.getKey(), entry.getValue().name());
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
            String name     = entry.getKey();
            String typeName = entry.getValue().getAsString();

            try
            {
                SocialManager.SocialType socialType =
                        SocialManager.SocialType.valueOf(typeName);

                Managers.SOCIAL.addSocial(name, socialType);
            }
            catch (IllegalArgumentException ignored)
            {
            }
        }
    }
}
