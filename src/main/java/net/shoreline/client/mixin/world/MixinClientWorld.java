package net.shoreline.client.mixin.world;

import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import net.shoreline.client.impl.imixin.IClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld implements IClientWorld
{
    @Override
    @Accessor("pendingUpdateManager")
    public abstract PendingUpdateManager getUpdateManager();
}
