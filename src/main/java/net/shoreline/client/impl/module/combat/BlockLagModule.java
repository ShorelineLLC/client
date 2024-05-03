package net.shoreline.client.impl.module.combat;

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
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ObsidianPlacerModule;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.math.position.PositionUtil;

import java.util.List;

/**
 * @author linus
 * @since 1.0
 */
public class BlockLagModule extends ObsidianPlacerModule {
    //
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotates before placing the block", false);
    // Config<Boolean> strictConfig = new BooleanConfig("Strict", "Allows you to fake lag on strict servers", false);
    Config<Boolean> attackConfig = new BooleanConfig("Attack", "Attacks crystals in the way of block", true);
    Config<Boolean> autoDisableConfig = new BooleanConfig("AutoDisable", "Automatically disables after placing block", false);
    //
    private double prevY;

    /**
     *
     */
    public BlockLagModule() {
        super("BlockLag", "Lags you into a block", ModuleCategory.COMBAT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            return;
        }
        prevY = mc.player.getY();
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event) {
        disable();
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        if (Math.abs(mc.player.getY() - prevY) > 0.5) {
            disable();
            return;
        }
        final BlockPos pos = mc.player.getBlockPos();
        if (!isInsideBlock()) {
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
            Managers.POSITION.setPositionClient(mc.player.getX(),
                    mc.player.getY() + 1.167, mc.player.getZ());
            attackPlace(pos);
            Managers.POSITION.setPositionClient(mc.player.getX(),
                    mc.player.getY() - 1.167, mc.player.getZ());
            final Vec3d dist = getLagOffsetVec();
            Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                    dist.x, dist.y, dist.z, false));
        }
        if (autoDisableConfig.getValue()) {
            disable();
        }
    }

    private void attack(Entity entity) {
        Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
        Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
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

    private void attackPlace(BlockPos targetPos, int slot) {
        if (attackConfig.getValue()) {
            List<Entity> entities = mc.world.getOtherEntities(null, new Box(targetPos)).stream().filter(e -> e instanceof EndCrystalEntity).toList();
            for (Entity entity : entities) {
                attack(entity);
            }
        }

        Managers.INTERACT.placeBlock(targetPos, slot, strictDirectionConfig.getValue(), false, (state, angles) ->
        {
            if (rotateConfig.getValue())
            {
                if (state)
                {
                    Managers.ROTATION.setRotationSilent(angles[0], angles[1], grimConfig.getValue());
                }
                else
                {
                    Managers.ROTATION.setRotationSilentSync(grimConfig.getValue());
                }
            }
        });
    }

    public boolean isInsideBlock() {
        return PositionUtil.getAllInBox(mc.player.getBoundingBox(), mc.player.getBlockPos()).stream().anyMatch(pos -> !mc.world.getBlockState(pos).isReplaceable());
    }

    // TODO: strict offset calcs
    public Vec3d getLagOffsetVec() {
        return new Vec3d(mc.player.getX(), mc.player.getY() + 3.5, mc.player.getZ());
    }
}
