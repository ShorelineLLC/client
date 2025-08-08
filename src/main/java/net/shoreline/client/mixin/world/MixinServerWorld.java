package net.shoreline.client.mixin.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.explosion.ExplosionImpl;
import net.shoreline.client.impl.event.world.WorldExplosionEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class MixinServerWorld
{
    @Inject(method = "createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;Lnet/minecraft/particle/ParticleEffect;Lnet/minecraft/particle/ParticleEffect;Lnet/minecraft/registry/entry/RegistryEntry;)V", at = @At(value = "HEAD"))
    private void hookCreateExplosion(Entity entity,
                                     DamageSource damageSource,
                                     ExplosionBehavior behavior,
                                     double x,
                                     double y,
                                     double z,
                                     float power,
                                     boolean createFire,
                                     World.ExplosionSourceType explosionSourceType,
                                     ParticleEffect smallParticle,
                                     ParticleEffect largeParticle,
                                     RegistryEntry<SoundEvent> soundEvent,
                                     CallbackInfo ci)
    {
        Vec3d vec3d = new Vec3d(x, y, z);
        ExplosionImpl explosionImpl = new ExplosionImpl(null, entity, damageSource, behavior,
                vec3d, power, createFire, Explosion.DestructionType.DESTROY);
        WorldExplosionEvent explosionEvent = new WorldExplosionEvent(explosionImpl);
        EventBus.INSTANCE.dispatch(explosionEvent);
    }
}
