package net.shoreline.client.impl.module.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.module.impl.hud.DynamicEntry;
import net.shoreline.client.impl.module.impl.hud.DynamicHudModule;
import net.shoreline.client.impl.render.ClientFormatting;
import net.shoreline.client.impl.render.ColorUtil;

import java.awt.*;
import java.util.function.Supplier;

public class DurabilityHudModule extends DynamicHudModule
{
    public DurabilityHudModule()
    {
        super("Durability", "Shows held item durability", 200, 500);
    }

    @Override
    public void onEnable()
    {
        getHudEntries().add(new DynamicDuraEntry(this, () -> mc.player != null && mc.player.getMainHandStack().isDamageable()));
    }

    private static class DynamicDuraEntry extends DynamicEntry
    {
        public DynamicDuraEntry(DynamicHudModule mod, Supplier<Boolean> drawing)
        {
            super(mod, () ->
            {
                String duraText = ClientFormatting.THEME + "Durability";
                if (mc.player.getMainHandStack().isDamageable())
                {
                    int n = mc.player.getMainHandStack().getMaxDamage();
                    int n2 = mc.player.getMainHandStack().getDamage();
                    return duraText + " " + Formatting.RESET + (n - n2);
                }

                return duraText;

            }, drawing);
        }

        @Override
        public void drawText(DrawContext context, String string, float x, float y)
        {
            int n = mc.player.getMainHandStack().getMaxDamage();
            int n2 = mc.player.getMainHandStack().getDamage();
            Color color = ColorUtil.hslToColor((float) (n - n2) / (float) n * 120.0f, 100.0f, 50.0f, 1.0f);
            getModule().drawTextTransparency(context.getMatrices(), string, x, y, color.getRGB(), (float) yAnimation.getFactor());
        }
    }
}
