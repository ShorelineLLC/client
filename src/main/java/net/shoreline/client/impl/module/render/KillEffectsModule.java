package net.shoreline.client.impl.module.render;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.world.RemoveEntityEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class KillEffectsModule extends ToggleModule {

    Config<KillEffect> killEffectConfig = register(new EnumConfig<>("Effect", "The kill effect to apply", KillEffect.THUNDER, KillEffect.values()));

    public KillEffectsModule() {
        super("KillEffects", "Adds effects to player deaths", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRemoveEntity(RemoveEntityEvent event) {
        if (event.getEntity() instanceof PlayerEntity entity && entity.getLastAttacker() == mc.player) {
            switch (killEffectConfig.getValue()) {
                case THUNDER -> {
                    LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);
                    lightningEntity.setPos(entity.getX(), entity.getY(), entity.getZ());
                    mc.world.spawnEntity(lightningEntity);
                }
                case FIREWORK -> {

                }
            }
        }
    }

    private enum KillEffect {
        THUNDER,
        FIREWORK
    }
}
