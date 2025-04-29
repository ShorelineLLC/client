package net.shoreline.client.impl.module.combat;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.module.ObsidianPlacerModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.client.util.math.position.PositionUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;

/**
 * @author linus
 * @since 1.0
 */
public class SelfFillModule extends ObsidianPlacerModule
{
    //
    Config<Mode> modeConfig = register(new EnumConfig<>("Mode", "The mode for block fill", Mode.BLOCK_LAG, Mode.values()));
    // Config<Boolean> strictConfig = register(new BooleanConfig("Strict", "Allows you to fake lag on strict servers", false);
    Config<Boolean> attackConfig = register(new BooleanConfig("Attack", "Attacks crystals in the way of block", true, () -> modeConfig.getValue() == Mode.BLOCK_LAG));
    Config<Boolean> autoDisableConfig = register(new BooleanConfig("AutoDisable", "Automatically disables after placing block", false));
    //
    private double prevY;

    /**
     *
     */
    public SelfFillModule()
    {
        super("SelfFill", "Fills in the block your standing on", ModuleCategory.COMBAT);
    }

    @Override
    public void onEnable()
    {
        if (mc.player == null)
        {
            return;
        }
        prevY = mc.player.getY();
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event)
    {
        disable();
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (Math.abs(mc.player.getY() - prevY) > 0.5)
        {
            disable();
            return;
        }

        if (checkMultitask() && !multitaskConfig.getValue())
        {
            return;
        }

        boolean inBlock = PositionUtil.getAllInBox(mc.player.getBoundingBox()).stream().anyMatch(p -> !mc.world.getBlockState(p).isReplaceable());
        final BlockPos pos = EntityUtil.getRoundedBlockPos(mc.player);
        if (!inBlock && mc.player.isOnGround())
        {
            if (modeConfig.getValue() == Mode.BLOCK_LAG)
            {
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY() + 0.42,
                        mc.player.getZ(), true));
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY() + 0.75,
                        mc.player.getZ(), true));
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY() + 1.01,
                        mc.player.getZ(), true));
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY() + 1.16,
                        mc.player.getZ(), true));

                if (!mc.world.isSpaceEmpty(mc.player.getBoundingBox().offset(0.0, 2.34, 0.0)))
                {
                    return;
                }

                double y = mc.player.getY();
                Managers.POSITION.setPositionClient(mc.player.getX(), y + 1.167, mc.player.getZ());
                attackPlace(pos);
                Managers.POSITION.setPositionClient(mc.player.getX(), y, mc.player.getZ());

                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(),
                        mc.player.getY() + 2.34, mc.player.getZ(), false));
            }

            else if (modeConfig.getValue() == Mode.WEB)
            {
                int slot = getBlockItemSlot(Blocks.COBWEB);
                if (slot == -1)
                {
                    return;
                }
                Managers.INTERACT.placeBlock(pos, slot, strictDirectionConfig.getValue(), false, (state, angles) ->
                {
                    if (rotateConfig.getValue())
                    {
                        if (state)
                        {
                            Managers.ROTATION.setRotationSilent(angles[0], angles[1]);
                        }
                        else
                        {
                            Managers.ROTATION.setRotationSilentSync();
                        }
                    }
                });
            }
        }

        if (autoDisableConfig.getValue())
        {
            disable();
        }
    }

    private void attackPlace(BlockPos targetPos)
    {
        final int slot = getResistantBlockItem();
        if (slot == -1)
        {
            return;
        }
        attackPlace(targetPos, slot);
    }

    private void attackPlace(BlockPos targetPos, int slot)
    {
        if (attackConfig.getValue())
        {
            Entity entity = mc.world.getOtherEntities(null, new Box(targetPos)).stream().filter(e -> e instanceof EndCrystalEntity).findFirst().orElse(null);
            if (entity != null)
            {
                Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
                Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        }

        Managers.INTERACT.placeBlock(targetPos, slot, false, strictDirectionConfig.getValue(), false, (state, angles) ->
        {
            if (rotateConfig.getValue() && state)
            {
                Managers.ROTATION.setRotationSilent(angles[0], angles[1]);
            }
        });
    }

    private enum Mode
    {
        BLOCK_LAG,
        WEB
    }
}
