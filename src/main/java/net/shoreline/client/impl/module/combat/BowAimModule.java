package net.shoreline.client.impl.module.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.event.entity.LookDirectionEvent;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.module.RotationModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

/**
 * @author linus
 * @since 1.0
 */
public class BowAimModule extends RotationModule
{
    //
    Config<Boolean> playersConfig = register(new BooleanConfig("Players", "Aims bow at players", true));
    Config<Boolean> monstersConfig = register(new BooleanConfig("Monsters", "Aims bow at monsters", false));
    Config<Boolean> neutralsConfig = register(new BooleanConfig("Neutrals", "Aims bow at neutrals", false));
    Config<Boolean> animalsConfig = register(new BooleanConfig("Animals", "Aims bow at animals", false));
    Config<Boolean> invisiblesConfig = register(new BooleanConfig("Invisibles", "Aims bow at invisible entities", false));
    //
    private Entity aimTarget;

    /**
     *
     */
    public BowAimModule()
    {
        super("BowAim", "Automatically aims charged bow at nearby entities", ModuleCategory.COMBAT);
    }

    @EventListener
    public void onPlayerUpdate(PlayerUpdateEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.PRE)
        {
            return;
        }
        aimTarget = null;
        if (mc.player.getMainHandStack().getItem() instanceof BowItem
                && mc.player.getItemUseTime() >= 3)
        {
            double minDist = Double.MAX_VALUE;
            for (Entity entity : mc.world.getEntities())
            {
                if (entity == null || entity == mc.player || !entity.isAlive()
                        || !isValidAimTarget(entity)
                        || Managers.SOCIAL.isFriend(entity.getName()))
                {
                    continue;
                }
                double dist = mc.player.distanceTo(entity);
                if (dist < minDist)
                {
                    minDist = dist;
                    aimTarget = entity;
                }
            }
            if (aimTarget instanceof LivingEntity target)
            {
                float[] rotations = getBowRotationsTo(target);
                setRotationClient(rotations[0], rotations[1]);
            }
        }
    }

    @EventListener
    public void onLookDirection(LookDirectionEvent event)
    {
        if (aimTarget != null)
        {
            event.cancel();
        }
    }

    private float[] getBowRotationsTo(Entity entity)
    {
        float duration = (float) (mc.player.getActiveItem().getMaxUseTime(mc.player) - mc.player.getItemUseTime()) / 20.0f;
        duration = (duration * duration + duration * 2.0f) / 3.0f;
        if (duration >= 1.0f)
        {
            duration = 1.0f;
        }
        double duration1 = duration * 3.0f;
        double coeff = 0.05000000074505806;
        float pitch = (float) (-Math.toDegrees(calculateArc(entity, duration1, coeff)));
        double ix = entity.getX() - entity.prevX;
        double iz = entity.getZ() - entity.prevZ;
        double d = mc.player.distanceTo(entity);
        d -= d % 2.0;
        ix = d / 2.0 * ix * (mc.player.isSprinting() ? 1.3 : 1.1);
        iz = d / 2.0 * iz * (mc.player.isSprinting() ? 1.3 : 1.1);
        float yaw = (float) Math.toDegrees(Math.atan2(entity.getZ() + iz - mc.player.getZ(), entity.getX() + ix - mc.player.getX())) - 90.0f;
        return new float[]{yaw, pitch};
    }

    private float calculateArc(Entity target, double duration, double coeff)
    {
        double yArc = target.getY() + (double) (target.getStandingEyeHeight() / 2.0f) - (mc.player.getY() + (double) mc.player.getStandingEyeHeight());
        double dX = target.getX() - mc.player.getX();
        double dZ = target.getZ() - mc.player.getZ();
        double dirRoot = Math.sqrt(dX * dX + dZ * dZ);
        return calculateArc(duration, coeff, dirRoot, yArc);
    }

    private float calculateArc(double duration, double coeff, double root, double yArc)
    {
        double dirCoeff = coeff * (root * root);
        yArc = 2.0 * yArc * (duration * duration);
        yArc = coeff * (dirCoeff + yArc);
        yArc = Math.sqrt(duration * duration * duration * duration - yArc);
        duration = duration * duration - yArc;
        yArc = Math.atan2(duration * duration + yArc, coeff * root);
        duration = Math.atan2(duration, coeff * root);
        return (float) Math.min(yArc, duration);
    }

    private boolean isValidAimTarget(Entity entity)
    {
        if (entity.isInvisible() && !invisiblesConfig.getValue())
        {
            return false;
        }
        return entity instanceof PlayerEntity && playersConfig.getValue()
                || EntityUtil.isMonster(entity) && monstersConfig.getValue()
                || EntityUtil.isNeutral(entity) && neutralsConfig.getValue()
                || EntityUtil.isPassive(entity) && animalsConfig.getValue();
    }
}
