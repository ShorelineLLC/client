package net.shoreline.client.impl.module.world;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.player.rotation.Rotation;
import net.shoreline.client.impl.event.network.RotationUpdateEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class NoRotateModule extends Toggleable
{
    private Rotation clientRotations;

    public NoRotateModule()
    {
        super("NoRotate", "Prevents server from rotating the player", GuiCategory.WORLD);
    }

    @Override
    public void onDisable()
    {
        clientRotations = null;
    }

    @EventListener
    public void onRotationUpdatePre(RotationUpdateEvent.Pre event)
    {
        if (!checkNull())
        {
            clientRotations = new Rotation(mc.player);
        }
    }

    @EventListener
    public void onRotationUpdatePre(RotationUpdateEvent.PrePacket event)
    {
        if (!checkNull() && clientRotations != null)
        {
            clientRotations.apply(mc.player);
            clientRotations = null;
        }
    }
}
