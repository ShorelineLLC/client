package net.shoreline.client.mixin.accessor;

import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(ClientWorld.class)
public interface AccessorClientWorld
{
    /**
     * @param x
     * @param y
     * @param z
     * @param event
     * @param category
     * @param volume
     * @param pitch
     * @param useDistance
     * @param seed
     */
    @Invoker("playSound")
    void hookPlaySound(double x, double y, double z, SoundEvent event,
                       SoundCategory category, float volume, float pitch,
                       boolean useDistance, long seed);

    /**
     * @return
     */
    @Invoker("getPendingUpdateManager")
    PendingUpdateManager hookGetPendingUpdateManager();
}
