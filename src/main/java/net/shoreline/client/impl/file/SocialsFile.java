package net.shoreline.client.impl.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.shoreline.client.api.file.IOUtils;
import net.shoreline.client.api.file.JsonConfigFile;
import net.shoreline.client.impl.Managers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SocialsFile extends JsonConfigFile
{
    public SocialsFile(Path directory) throws IOException
    {
        super(directory, "socials");
    }

    @Override
    public void saveFile() throws IOException
    {
        JsonArray array = new JsonArray();
        for (String string : Managers.SOCIAL.getFriends())
        {
            array.add(string);
        }

        IOUtils.writeFile(getFilepath(), GSON.toJson(array));
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
            Managers.SOCIAL.addFriend(element.getAsString());
        }
    }
}
