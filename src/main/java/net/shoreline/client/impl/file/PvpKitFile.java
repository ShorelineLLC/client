package net.shoreline.client.impl.file;

import com.google.gson.JsonArray;
import net.shoreline.client.api.file.IOUtils;
import net.shoreline.client.api.file.JsonConfigFile;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.combat.PvpKit;

import java.io.IOException;
import java.nio.file.Path;

public class PvpKitFile extends JsonConfigFile
{

    public PvpKitFile(Path directory) throws IOException
    {
        super(directory, "pvp_kits");
    }

    @Override
    public void saveFile() throws IOException
    {
        final JsonArray kitsArray = new JsonArray();
        for (PvpKit pvpKit : Managers.KIT.getKits())
        {
            kitsArray.add(pvpKit.toJson());
        }

        IOUtils.writeFile(getFilepath(), GSON.toJson(kitsArray));
    }

    @Override
    public void loadFile() throws IOException
    {

    }
}
