package net.shoreline.client.util.item;

import lombok.experimental.UtilityClass;
import net.minecraft.item.Item;

@UtilityClass
public class ItemUtil
{
    public boolean isTool(Item item)
    {
        return item.getTranslationKey().contains("shovel") || item.getTranslationKey().contains("axe")
                || item.getTranslationKey().contains("pickaxe");
    }

    public boolean isSword(Item item)
    {
        return item.getTranslationKey().contains("sword");
    }
}
