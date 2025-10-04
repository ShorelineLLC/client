package net.shoreline.client.impl.module.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.module.impl.hud.HudModule;

public class TotemsHudModule extends HudModule
{
    private final ItemStack stack = new ItemStack(Items.TOTEM_OF_UNDYING);

    public TotemsHudModule()
    {
        super("Totems", "Displays current totem count", 300, 300);
    }

    @Override
    public void drawHudComponent(DrawContext context, float tickDelta)
    {
        context.drawItem(stack, (int) getX(), (int) getY());
        String totemCount = String.valueOf(InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING));

        context.getMatrices().push();
        context.getMatrices().translate(getX() + 20.0f - getTextWidth(totemCount), getY() + 10.0f, 200.0f);
        context.getMatrices().scale(0.75f, 0.75f, 1.0f);
        drawText(context.getMatrices(), totemCount, 0.0f, 0.0f, -1);
        context.getMatrices().pop();
    }

    @Override
    public float getWidth()
    {
        return 12;
    }

    @Override
    public float getHeight()
    {
        return 12;
    }
}
