package net.shoreline.client.impl.module.movement;

import net.minecraft.util.math.Box;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class ParkourModule extends Toggleable
{
    private boolean jumping;

    public ParkourModule()
    {
        super("Parkour", "Jumps at the edge of blocks", GuiCategory.MOVEMENT);
    }

    @Override
    public void onDisable()
    {
        if (jumping)
        {
            mc.options.jumpKey.setPressed(false);
            jumping = false;
        }
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        Box playerBox = mc.player.getBoundingBox().offset(0.0, -0.5, 0.0).expand(-0.001, 0.0, -0.001);
        if (mc.player.isOnGround() && !mc.player.isSneaking() && mc.world.isSpaceEmpty(playerBox))
        {
            mc.options.jumpKey.setPressed(true);
            jumping = true;
        } else if (jumping)
        {
            mc.options.jumpKey.setPressed(false);
            jumping = false;
        }
    }
}
