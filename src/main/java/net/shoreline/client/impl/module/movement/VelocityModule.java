package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.entity.PushEvent;
import net.shoreline.client.impl.event.network.ExplosionEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PushOutOfBlocksEvent;
import net.shoreline.client.impl.imixin.IEntityVelocityUpdateS2CPacket;
import net.shoreline.client.impl.imixin.IExplosionS2CPacket;
import net.shoreline.client.impl.module.combat.util.PhaseUtil;
import net.shoreline.client.util.text.Formatter;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.Optional;

public class VelocityModule extends Toggleable
{
    Config<Boolean> cancelKnockback = new BooleanConfig.Builder("Knockback")
            .setDescription("Cancels player knockback")
            .setDefaultValue(true).build();
    Config<Boolean> cancelExplosion = new BooleanConfig.Builder("Explosion")
            .setDescription("Cancels explosion knockback")
            .setDefaultValue(true).build();
    Config<VelocityMode> modeConfig = new EnumConfig.Builder<VelocityMode>("Mode")
            .setValues(VelocityMode.values())
            .setDescription("The bypass mode for anti knockback")
            .setDefaultValue(VelocityMode.NORMAL).build();
    Config<Boolean> noPushEntitiesConfig = new BooleanConfig.Builder("Entities")
            .setDescription("Prevents getting pushed by other entities")
            .setDefaultValue(false).build();
    Config<Boolean> noPushBlocksConfig = new BooleanConfig.Builder("Blocks")
            .setDescription("Prevents being pushed out of blocks")
            .setDefaultValue(false).build();
    Config<Boolean> noPushLiquidsConfig = new BooleanConfig.Builder("Liquid")
            .setDescription("Prevents getting pushed by liquids")
            .setDefaultValue(false).build();
    Config<Void> noPushGroup = new ConfigGroup.Builder("NoPush")
            .addAll(noPushEntitiesConfig, noPushBlocksConfig, noPushLiquidsConfig).build();
    Config<Boolean> fishhookConfig = new BooleanConfig.Builder("NoFishhook")
            .setDescription("Prevents getting knocked back by fishing hooks")
            .setDefaultValue(false).build();
    Config<Integer> horizontalConfig = new NumberConfig.Builder<Integer>("Horizontal")
            .setDefaultValue(0).setMin(0).setMax(100).setFormat("%")
            .setVisible(() -> modeConfig.getValue() == VelocityMode.NORMAL)
            .setDescription("The horizontal velocity reduction").build();
    Config<Integer> verticalConfig = new NumberConfig.Builder<Integer>("Vertical")
            .setDefaultValue(0).setMin(0).setMax(100).setFormat("%")
            .setVisible(() -> modeConfig.getValue() == VelocityMode.NORMAL)
            .setDescription("The vertical velocity reduction").build();
    Config<Boolean> groundOnlyConfig = new BooleanConfig.Builder("GroundOnly")
            .setDescription("Only applies wall velocity when grounded.")
            .setVisible(() -> modeConfig.getValue().equals(VelocityMode.WALLS))
            .setDefaultValue(false).build();
    Config<Boolean> concealConfig = new BooleanConfig.Builder("Conceal")
            .setDescription("Prevents excessive lagbacks on servers with strict movement anticheats")
            .setDefaultValue(false).build();

    private boolean concealVelocity;
    private boolean cancelVelocity;

    public VelocityModule()
    {
        super("Velocity", new String[] {"AntiKB"}, "Prevents player knockback", GuiCategory.MOVEMENT);
    }

    @Override
    public String getModuleData()
    {
        if (modeConfig.getValue() == VelocityMode.NORMAL)
        {
            return String.format("H:%s%%, V:%s%%",
                    DECIMAL.format(horizontalConfig.getValue()),
                    DECIMAL.format(verticalConfig.getValue()));
        }

        return Formatter.formatEnum(modeConfig.getValue());
    }

    @Override
    public void onDisable()
    {
        concealVelocity = false;
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet && packet.getEntityId() == mc.player.getId())
        {
            if (concealVelocity && packet.getVelocityX() == 0.0 && packet.getVelocityZ() == 0.0 && packet.getVelocityZ() == 0.0)
            {
                concealVelocity = false;
                return;
            }

            if (!cancelKnockback.getValue())
            {
                return;
            }

            if (shouldCancelKnockback())
            {
                event.cancel();
            } else if (modeConfig.getValue() == VelocityMode.NORMAL)
            {
                double e = packet.getVelocityX() * (horizontalConfig.getValue() / 100.0f);
                double f = packet.getVelocityY() * (verticalConfig.getValue() / 100.0f);
                double g = packet.getVelocityZ() * (horizontalConfig.getValue() / 100.0f);
                ((IEntityVelocityUpdateS2CPacket) packet).setX((int) (e * 8000.0));
                ((IEntityVelocityUpdateS2CPacket) packet).setY((int) (f * 8000.0));
                ((IEntityVelocityUpdateS2CPacket) packet).setZ((int) (g * 8000.0));
            }
        }

        if (event.getPacket() instanceof ExplosionS2CPacket packet && cancelExplosion.getValue())
        {
            if (shouldCancelExplosions())
            {
                event.cancel();
            }
            else if (modeConfig.getValue() == VelocityMode.NORMAL)
            {
                Vec3d velocity = packet.playerKnockback().orElse(Vec3d.ZERO);
                ((IExplosionS2CPacket) event.getPacket()).setPlayerKnockback(Optional.of(new Vec3d(
                        velocity.x * (horizontalConfig.getValue() / 100f),
                        velocity.y * (verticalConfig.getValue() / 100f),
                        velocity.z * (horizontalConfig.getValue() / 100f))));
            }
        }

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket && concealConfig.getValue())
        {
            concealVelocity = true;
        }

        if (event.getPacket() instanceof EntityStatusS2CPacket packet
                && packet.getStatus() == EntityStatuses.PULL_HOOKED_ENTITY && fishhookConfig.getValue())
        {
            final Entity entity = packet.getEntity(mc.world);
            if (entity instanceof FishingBobberEntity hook && hook.getHookedEntity() == mc.player)
            {
                event.cancel();
            }
        }
    }

    @EventListener
    public void onExplosion(ExplosionEvent event)
    {
        if (!cancelExplosion.getValue())
        {
            return;
        }

        if (shouldCancelExplosions())
        {
            event.cancel();
            if (mc.isInSingleplayer())
            {
                event.setPlayerVelocity(Vec3d.ZERO);
            }

        } else if (modeConfig.getValue() == VelocityMode.NORMAL)
        {
            Vec3d knockback = event.getPlayerVelocity();
            double e = knockback.x * (horizontalConfig.getValue() / 100.0f);
            double f = knockback.y * (verticalConfig.getValue() / 100.0f);
            double g = knockback.z * (horizontalConfig.getValue() / 100.0f);
            event.cancel();
            event.setPlayerVelocity(new Vec3d(e, f, g));
        }
    }

    @EventListener
    public void onPushOutOfBlocks(PushOutOfBlocksEvent event)
    {
        if (noPushBlocksConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onPushEntity(PushEvent.Entity event)
    {
        if (noPushEntitiesConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onPushLiquid(PushEvent.Liquid event)
    {
        if (noPushLiquidsConfig.getValue())
        {
            event.cancel();
        }
    }

    private boolean shouldCancelKnockback()
    {
        return switch (modeConfig.getValue())
        {
            case WALLS -> PhaseUtil.isInsideBlock(mc.player) && (!groundOnlyConfig.getValue() || mc.player.isOnGround());
            case NORMAL -> horizontalConfig.getValue() == 0 && verticalConfig.getValue() == 0;
            default -> true;
        };
    }

    private boolean shouldCancelExplosions()
    {
        return switch (modeConfig.getValue())
        {
            case WALLS -> PhaseUtil.isInsideBlock(mc.player);
            case NORMAL -> horizontalConfig.getValue() == 0 && verticalConfig.getValue() == 0;
            default -> true;
        };
    }

    private enum VelocityMode
    {
        NORMAL,
        WALLS,
        GRIM_V2,
        JUMP
    }
}
