package net.shoreline.client.impl.module.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.manager.rotation.ClientRotationEvent;
import net.shoreline.client.impl.manager.rotation.RotationUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class AuraModule extends Toggleable
{
    Config<Float> rangeConfig = new NumberConfig.Builder<Float>("Range")
            .setDefaultValue(4.0f).setMin(0.5f).setMax(6.0f).setFormat("m")
            .setDescription("The range to attack entities").build();
    Config<Boolean> rotateConfig = new BooleanConfig.Builder("Rotate")
            .setDescription("Rotates to the entity before attacking")
            .setDefaultValue(false).build();

    private Entity target;

    public AuraModule()
    {
        super("Aura", "Attacks nearby entities", GuiCategory.COMBAT);
    }

    @EventListener(priority = -1000)
    public void onClientRotation(ClientRotationEvent event)
    {
        target = null;
        for (Entity entity : mc.world.getEntities())
        {
            if (entity.equals(mc.player) || !entity.isAlive())
            {
                continue;
            }

            double dist = mc.player.squaredDistanceTo(entity);
            if (dist > rangeConfig.getValue() * rangeConfig.getValue())
            {
                continue;
            }

            if (entity instanceof PlayerEntity)
            {
                target = entity;
            }
        }

        if (target == null)
        {
            return;
        }

        float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), target.getEyePos());
        event.cancel();
        event.setYaw(rotations[0]);
        event.setPitch(rotations[1]);
    }
}
