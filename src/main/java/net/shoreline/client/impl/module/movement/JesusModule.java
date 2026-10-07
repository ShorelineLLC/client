package net.shoreline.client.impl.module.movement;

import net.minecraft.registry.tag.FluidTags;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.util.text.Formatter;
import net.shoreline.eventbus.annotation.EventListener;

public class JesusModule extends Toggleable
{
    Config<Mode> modeConfig = new EnumConfig.Builder<Mode>("Mode")
            .setValues(Mode.values())
            .setDescription("The packet mode")
            .setDefaultValue(Mode.NCP).build();

    public JesusModule()
    {
        super("Jesus", "Walk on water", GuiCategory.MOVEMENT);
    }

    @EventListener
    public void onTickPost(TickEvent.Post event)
    {
        if (modeConfig.getValue() == Mode.STRICT_NCP && (mc.player.isTouchingWater() || mc.player.isInLava()))
        {
            double fluidHeight;
            if (mc.player.isInLava())
            {
                fluidHeight = mc.player.getFluidHeight(FluidTags.LAVA);
            } else
            {
                fluidHeight = mc.player.getFluidHeight(FluidTags.WATER);
            }

            double swimHeight = mc.player.getSwimHeight();
//            if (mc.player.isTouchingWater() && fluidHeight > swimHeight)
//            {
//                mc.player.swimUpwards(FluidTags.WATER);
//            } else if (mc.player.isOnGround() && fluidHeight <= swimHeight)
//            {
//                mc.player.jump();
//            } else
//            {
//                mc.player.swimUpwards(FluidTags.LAVA);
//            }
        }
    }

    @Override
    public String getModuleData()
    {
        return Formatter.formatEnum(modeConfig.getValue());
    }

    public enum Mode
    {
        NCP,
        STRICT_NCP
    }
}
