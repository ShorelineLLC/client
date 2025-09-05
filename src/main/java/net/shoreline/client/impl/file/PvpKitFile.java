package net.shoreline.client.impl.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.file.IOUtils;
import net.shoreline.client.api.file.JsonConfigFile;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.combat.PvpKit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

        for (JsonElement element : object)
        {
            JsonObject kitObject = element.getAsJsonObject();
            String name = kitObject.get("name").getAsString();
            JsonArray itemsArray = kitObject.getAsJsonArray("items");

            List<Item> items = new ArrayList<>();
            for (JsonElement itemElement : itemsArray)
            {
                String itemId = itemElement.getAsString();
                Identifier identifier = Identifier.tryParse(itemId);
                Item item = Registries.ITEM.get(identifier);
                items.add(item);
            }

            PvpKit kit = new PvpKit(name, items);
            Managers.KIT.saveKit(name, kit);
        }
    }
}
