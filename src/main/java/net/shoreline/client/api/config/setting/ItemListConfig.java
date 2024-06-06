package net.shoreline.client.api.config.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.config.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * @param <T>
 * @author linus
 * @since 1.0
 */
public class ItemListConfig<T extends List<Item>> extends Config<T>
{

    @SuppressWarnings("unchecked")
    public ItemListConfig(String name, String desc, Item... values)
    {
        super(name, desc, (T) List.of(values));
    }

    @SuppressWarnings("unchecked")
    public ItemListConfig(String name, String desc, T values)
    {
        super(name, desc, values);
    }

    /**
     * @param obj
     * @return
     */
    public boolean contains(Object obj)
    {
        if (obj instanceof Item)
        {
            return value.contains(obj);
        }
        return false;
    }

    /**
     * Converts all data in the object to a {@link JsonObject}.
     *
     * @return The data as a json object
     */
    @Override
    public JsonObject toJson()
    {
        JsonObject jsonObj = super.toJson();
        JsonArray array = new JsonArray();
        for (Item item : getValue())
        {
            Identifier id = Registries.ITEM.getId(item);
            array.add(id.toString());
        }
        jsonObj.add("value", array);
        return jsonObj;
    }

    /**
     * Reads all data from a {@link JsonObject} and updates the values of the
     * data in the object.
     *
     * @param jsonObj The data as a json object
     * @return
     * @see #toJson()
     */
    @Override
    public T fromJson(JsonObject jsonObj)
    {
        if (jsonObj.has("value"))
        {
            JsonElement element = jsonObj.get("value");
            List<Item> temp = new ArrayList<>();
            for (JsonElement je : element.getAsJsonArray())
            {
                String val = je.getAsString();
                Item item = Registries.ITEM.get(new Identifier(val));
                temp.add(item);
            }
            return (T) temp;
        }
        return null;
    }
}
