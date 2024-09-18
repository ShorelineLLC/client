package net.shoreline.client.util.world;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.Set;

public class ItemUtil
{
    private static final Set<Item> SHULKERS = Set.of(
            Items.SHULKER_BOX, Items.WHITE_SHULKER_BOX, Items.LIGHT_GRAY_SHULKER_BOX,
            Items.GRAY_SHULKER_BOX, Items.BLACK_SHULKER_BOX, Items.BROWN_SHULKER_BOX,
            Items.RED_SHULKER_BOX, Items.ORANGE_SHULKER_BOX, Items.YELLOW_SHULKER_BOX,
            Items.LIME_SHULKER_BOX, Items.GREEN_SHULKER_BOX, Items.CYAN_SHULKER_BOX,
            Items.LIGHT_BLUE_SHULKER_BOX, Items.BLUE_SHULKER_BOX, Items.PURPLE_SHULKER_BOX,
            Items.PINK_SHULKER_BOX, Items.MAGENTA_SHULKER_BOX
    );

    public static boolean isShulker(Item item)
    {
        return SHULKERS.contains(item);
    }
}
