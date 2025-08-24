package net.shoreline.client.impl.module.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.impl.module.impl.hud.HudModule;
import net.shoreline.client.impl.render.ColorUtil;

public class ArmorHudModule extends HudModule
{
    Config<Boolean> percent = new BooleanConfig.Builder("Percent")
            .setDescription("Displays the percent of the armor piece")
            .setDefaultValue(true).build();

    public ArmorHudModule()
    {
        super("Armor", "Displays your armor slots", 200, 200);
    }

    @Override
    public void drawHudComponent(DrawContext context, float tickDelta)
    {
        int offset = 19;
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR)
            {
                continue;
            }

            ItemStack stack = mc.player.getEquippedStack(slot);
            if (stack.isEmpty())
            {
                continue;
            }

            int extra = percent.getValue() ? 8 : 3;
            context.drawItem(stack, getX() + getWidth() - offset, getY() + extra);
            context.drawStackOverlay(mc.textRenderer, stack, getX() + getWidth() - offset, getY() + extra);
            if (percent.getValue())
            {
                context.getMatrices().push();
                context.getMatrices().translate(getX() + getWidth() - offset + 17, getY(), 1.0f);
                context.getMatrices().scale(0.66f, 0.66f, 0.66f);
                int damage = stack.getMaxDamage() - stack.getDamage();
                int percent = (damage / stack.getMaxDamage()) * 100;
                String text = percent + "%";
                float width = getTextWidth(text);
                int color = ColorUtil.hslToColor(percent * 120, 100f, 50f, 1f).getRGB();
                drawText(context.getMatrices(), text, -width + 1, 0, color);
                context.getMatrices().pop();
            }

            offset += 18;
        }
    }

    @Override
    public int getWidth()
    {
        return 75;
    }

    @Override
    public int getHeight()
    {
        return percent.getValue() ? 25 : 20;
    }
}
