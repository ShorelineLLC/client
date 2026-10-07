package net.shoreline.client.impl.module.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.RegistryConfig;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.module.impl.hud.HudModule;

import java.util.Collection;

public class CountsHudModule extends HudModule
{
    Config<Collection<Item>> selectedItems = new RegistryConfig.Builder<Item>("Items")
            .setRegistry(Registries.ITEM)
            .setValues(Items.TOTEM_OF_UNDYING, Items.END_CRYSTAL, Items.OBSIDIAN, Items.ENDER_PEARL)
            .setDescription("Item whitelist for counts").build();

    private float height;

    public CountsHudModule()
    {
        super("Counts", "Shows the counts of selected blocks/items.", 200, 200);
    }

    @Override
    public void drawHudComponent(DrawContext context, float tickDelta)
    {
        height = 0;
        selectedItems.getValue().forEach(item -> drawCount(context, item));
    }

    public void drawCount(DrawContext context, Item item)
    {
        int count = InventoryUtil.getItemCount(item);
        context.drawItem(item.getDefaultStack(), (int) getX(), (int) (getY() + height));
        height += 20;

        String str = String.valueOf(count);
        float textW = getTextWidth(str) / 2f;
        drawText(context.getMatrices(), String.valueOf(count), getX() + 9 - textW, getY() + height - 8);
    }

    @Override
    public float getWidth()
    {
        return 16;
    }

    @Override
    public float getHeight()
    {
        return height;
    }
}
