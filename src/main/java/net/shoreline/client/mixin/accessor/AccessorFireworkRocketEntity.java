package net.shoreline.client.mixin.accessor;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FireworkRocketEntity.class)
public interface AccessorFireworkRocketEntity
{
    /**
     * @return
     */
    @Accessor("shooter")
    LivingEntity hookGetShooter();

    /**
     * @return
     */
    @Invoker("wasShotByEntity")
    boolean hookWasShotByEntity();

    @Invoker("explodeAndRemove")
    void hookExplodeAndRemove();
}
