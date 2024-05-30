package net.shoreline.client.impl.module.render;

import net.minecraft.client.particle.FireworksSparkParticle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.event.entity.PlayerDamageEvent;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.eventbus.annotation.EventListener;

public class KillEffectsModule extends ToggleModule {

    Config<KillEffect> killEffectConfig = register(new EnumConfig<>("Effect", "The kill effect to apply", KillEffect.LIGHTNING, KillEffect.values()));
    Config<Integer> strikes = register(new NumberConfig<>("Strikes", "The number of lightning strikes", 1, 1, 5, () -> killEffectConfig.getValue() == KillEffect.LIGHTNING));

    private Entity lastAttackedEntity;
    private long lastAttackTime;

    public KillEffectsModule() {
        super("KillEffects", "Adds effects to player deaths", ModuleCategory.RENDER);
    }

    @EventListener
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof PlayerEntity player) || !wasLastAttackedByPlayer(player))
        {
            return;
        }
        switch (killEffectConfig.getValue())
        {
            case LIGHTNING ->
            {
                for (int i = 0; i < strikes.getValue(); i++)
                {
                    LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);
                    lightningEntity.setPos(player.getX(), player.getY(), player.getZ());
                    mc.world.addEntity(lightningEntity);
                }
            }
            case FIREWORK ->
            {
                fireworkExplode(player.getX(), player.getY(), player.getZ(), 0.5, 4);
            }
        }
    }

    @EventListener
    public void onPlayerDamage(PlayerDamageEvent event)
    {
        lastAttackedEntity = event.getDamaged();
        lastAttackTime = System.currentTimeMillis();
    }
    
    private void fireworkExplode(double x, double y, double z, double size, int amount)
    {
        double d = x;
        double e = y;
        double f = z;
        for (int i = -amount; i <= amount; ++i) {
            for (int j = -amount; j <= amount; ++j) {
                for (int k = -amount; k <= amount; ++k) {
                    double g = (double)j + (RANDOM.nextDouble() - RANDOM.nextDouble()) * 0.5;
                    double h = (double)i + (RANDOM.nextDouble() - RANDOM.nextDouble()) * 0.5;
                    double l = (double)k + (RANDOM.nextDouble() - RANDOM.nextDouble()) * 0.5;
                    double m = Math.sqrt(g * g + h * h + l * l) / size + RANDOM.nextGaussian() * 0.05;
                    addExplosionParticle(d, e, f, g / m, h / m, l / m);
                    if (i == -amount || i == amount || j == -amount || j == amount) continue;
                    k += amount * 2 - 1;
                }
            }
        }
    }

    private void addExplosionParticle(double x, double y, double z, double velocityX, double velocityY, double velocityZ)
    {
        FireworksSparkParticle.Explosion explosion = (FireworksSparkParticle.Explosion) mc.particleManager.addParticle(ParticleTypes.FIREWORK, x, y, z, velocityX, velocityY, velocityZ);
        explosion.setTrail(false);
        explosion.setFlicker(false);
        explosion.setColor(ColorsModule.getInstance().getRGB());
    }

    private boolean wasLastAttackedByPlayer(Entity entity)
    {
        return entity.equals(lastAttackedEntity) && (System.currentTimeMillis() - lastAttackTime) < 5000;
    }

    private enum KillEffect {
        LIGHTNING,
        FIREWORK
    }
}
