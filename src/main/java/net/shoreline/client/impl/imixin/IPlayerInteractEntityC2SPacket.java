package net.shoreline.client.impl.imixin;

import net.minecraft.entity.Entity;
import net.shoreline.client.util.network.InteractType;

/**
 *
 */
@IMixin
public interface IPlayerInteractEntityC2SPacket
{
    /**
     * @return
     */
    Entity getEntity();

    /**
     * @return
     */
    InteractType getType();
}
