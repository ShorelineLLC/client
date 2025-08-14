package net.shoreline.client.impl.imixin;

import net.minecraft.client.network.PendingUpdateManager;

@IMixin
public interface IClientWorld
{
    PendingUpdateManager getUpdateManager();
}
