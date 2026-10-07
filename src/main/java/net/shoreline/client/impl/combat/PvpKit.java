package net.shoreline.client.impl.combat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.shoreline.client.api.Serializable;

import java.util.List;

@Getter
public class PvpKit implements Serializable
{
    private final String name;
    private final DefaultedList<Item> items;

    public PvpKit(String name, Inventory inventory)
    {
        this.name = name;
        this.items = DefaultedList.ofSize(PlayerInventory.MAIN_SIZE + PlayerInventory.HOTBAR_SIZE, Items.AIR);
        setKitFromInventory(inventory);
    }

    public PvpKit(String name, List<Item> items)
    {
        this.name = name;
        this.items = DefaultedList.ofSize(PlayerInventory.MAIN_SIZE + PlayerInventory.HOTBAR_SIZE, Items.AIR);
        for (int j = 0; j < PlayerInventory.MAIN_SIZE + PlayerInventory.HOTBAR_SIZE; ++j)
        {
            Item item = items.get(j); // not sure if this maintains order all the time.
            this.items.set(j, item);
        }
    }

    public void setKitFromInventory(final Inventory inventory)
    {
        for (int j = 0; j < PlayerInventory.MAIN_SIZE + PlayerInventory.HOTBAR_SIZE; ++j)
        {
            ItemStack itemStack = inventory.getStack(j);
            items.set(j, itemStack.getItem());
        }
    }

    @Override
    public JsonObject toJson()
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", getName());

        JsonArray jsonArray = new JsonArray();
        for (Item item : items)
        {
            Identifier identifier = Registries.ITEM.getId(item);
            jsonArray.add(identifier.toString());
        }

        jsonObject.add("items", jsonArray);
        return jsonObject;
    }

    public Item getStack(int slot)
    {
        return items.get(slot);
    }
}
