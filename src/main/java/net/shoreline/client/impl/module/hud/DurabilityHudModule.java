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
    public void loadEntries()
    {
        getHudEntries().add(new DynamicDuraEntry(this, () -> mc.player != null && mc.player.getMainHandStack().isDamageable()));
    }

    private static class DynamicDuraEntry extends DynamicEntry
    {
        public DynamicDuraEntry(DynamicHudModule mod, Supplier<Boolean> drawing)
        {
            super(mod, () ->
            {
                if (mc.player.getMainHandStack().isDamageable())
                {
                    int maxDmg = mc.player.getMainHandStack().getMaxDamage();
                    int dmg = mc.player.getMainHandStack().getDamage();
                    int color = ColorUtil.hslToColor((float) (maxDmg - dmg) / (float) maxDmg * 120.0f,
                                100.0f,
                                50.0f,
                                1.0f).getRGB();
                    return "Durability " + ClientFormatting.HEX + Integer.toHexString(color) + (maxDmg - dmg);
                }

                return "Durability";
            }, drawing);
        }
    }
}
