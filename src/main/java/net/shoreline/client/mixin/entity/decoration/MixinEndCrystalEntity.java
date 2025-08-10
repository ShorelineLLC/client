package net.shoreline.client.mixin.entity.decoration;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.server.world.ServerWorld;
import net.shoreline.client.impl.event.entity.decoration.EndCrystalExplosionEvent;
import net.shoreline.client.mixin.entity.MixinEntity;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCrystalEntity.class)
public abstract class MixinEndCrystalEntity extends MixinEntity
{
    @Inject(method = "damage", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;)V"))
    private void hookCreateExplosion(ServerWorld world,
                                     DamageSource source,
                                     float amount,
                                     CallbackInfoReturnable<Boolean> cir)
    {
        EndCrystalExplosionEvent explosionEvent = new EndCrystalExplosionEvent(getPos(), source);
        EventBus.INSTANCE.dispatch(explosionEvent);
    }
}
