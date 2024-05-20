package net.shoreline.client.impl.module.world;

import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.RotationModule;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.player.PlayerUtil;
import net.shoreline.client.util.player.RayCastUtil;
import net.shoreline.client.util.player.RotationUtil;

import static net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.START_SPRINTING;
import static net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.STOP_SPRINTING;

/**
 * @author xgraza
 * @since 05/15/24
 */
public final class ScaffoldModule extends RotationModule
{
    Config<Mode> modeConfig = new EnumConfig<>("Mode", "", Mode.VANILLA, Mode.values());
    Config<Boolean> keepYConfig = new BooleanConfig("KeepY", "", false);

    private boolean stoppedServerSprint;
    private float[] lastAngles;
    private int groundPosY;

    public ScaffoldModule()
    {
        super("Scaffold", "", ModuleCategory.WORLD);
    }

    @Override
    protected void onDisable()
    {
        if (mc.player != null)
        {
            if (stoppedServerSprint && mc.player.isSprinting())
            {
                Managers.NETWORK.sendQuietPacket(new ClientCommandC2SPacket(
                        mc.player, START_SPRINTING));
            }
            Managers.INVENTORY.syncToClient();
        }
        groundPosY = -1;
        stoppedServerSprint = false;
        lastAngles = null;
    }

    @EventListener
    public void onPlayerTick(final PlayerTickEvent event)
    {
        final BlockData blockData = getBlockData();
        if (blockData == null)
        {
            return;
        }
        calcRotations(blockData);
        if (blockData.getAngles() == null && modeConfig.getValue() != Mode.VANILLA)
        {
            if (modeConfig.getValue() != Mode.GRIM && lastAngles != null)
            {
                setRotation(lastAngles[0], lastAngles[1]);
            }
            return;
        }

        if (modeConfig.getValue() == Mode.NCP && !stoppedServerSprint && mc.player.isSprinting())
        {
            stoppedServerSprint = true;
            Managers.NETWORK.sendQuietPacket(new ClientCommandC2SPacket(
                    mc.player, STOP_SPRINTING));
        }

        int slot = -1;
        {
            for (int i = 0; i < 9; ++i)
            {
                final ItemStack itemStack = mc.player.getInventory().getStack(i);
                if (!itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem)
                {
                    slot = i;
                    break;
                }
            }
            if (slot == -1)
            {
                return;
            }
        }

        if (modeConfig.getValue() == Mode.NCP && Managers.INVENTORY.getServerSlot() != slot)
        {
            Managers.INVENTORY.setSlot(slot);
        }
        Managers.INTERACT.placeBlock(blockData.getHitResult(), slot, false, false, (state, angles) ->
        {
            final float[] rotations = blockData.getAngles();
            if (rotations == null)
            {
                return;
            }
            lastAngles = rotations;
            if (state)
            {
                if (modeConfig.getValue() == Mode.GRIM)
                {
                    Managers.ROTATION.setRotationSilent(rotations[0], rotations[1], true);
                } else
                {
                    setRotation(rotations[0], rotations[1]);
                }
            } else
            {
                if (modeConfig.getValue() == Mode.GRIM)
                {
                    Managers.ROTATION.setRotationSilentSync(true);
                }
            }
        });
    }

    @EventListener
    public void onPacketOutbound(final PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof ClientCommandC2SPacket packet)
        {
            if (stoppedServerSprint && (packet.getMode() == START_SPRINTING || packet.getMode() == STOP_SPRINTING))
            {
                event.setCanceled(true);
            }
        }
    }

    private void calcRotations(final BlockData blockData)
    {
        final BlockPos pos = blockData.getHitResult().getBlockPos();
        final Direction side = blockData.getHitResult().getSide();
        final Vec3d basicHitVec = pos.toCenterPos()
                .add(side.getOffsetX() * 0.5f, side.getOffsetY() * 0.5f, side.getOffsetZ() * 0.5f);

        switch (modeConfig.getValue())
        {
            case VANILLA -> blockData.setAngles(RotationUtil.getRotationsTo(mc.player.getEyePos(), basicHitVec));
            //case NCP -> blockData.setAngles(new float[] { mc.player.getYaw() - 180, 86 });
            case NCP, GRIM ->
            {
                float yaw = mc.player.getYaw() - 180;
                float pitch = 75.0f;
                for (float offsetYaw = -55.0f; offsetYaw <= 55.0f; offsetYaw += 0.5f)
                {
                    for (float offsetPitch = 0.0f; offsetPitch <= 15.0f; offsetPitch += 0.5f)
                    {
                        final float[] angles = { yaw + offsetYaw, pitch + offsetPitch };
                        final HitResult hitResult = RayCastUtil.rayCast(4.0, angles);
                        if (hitResult instanceof BlockHitResult blockHitResult
                                && blockHitResult.getBlockPos().equals(pos)
                                && blockHitResult.getSide().equals(side))
                        {
                            blockData.setHitResult(blockHitResult);
                            blockData.setAngles(angles);
                            return;
                        }
                    }
                }
            }
        }

        blockData.setHitResult(new BlockHitResult(basicHitVec, side, pos, false));
    }

    private BlockData getBlockData()
    {
        int posY = (int)Math.floor(mc.player.getY()) - 1;
        if (keepYConfig.getValue())
        {
            if (mc.player.isOnGround() || groundPosY == -1)
            {
                groundPosY = (int)Math.floor(mc.player.getY()) - 1;
            }
            posY = groundPosY;
        }
        final BlockPos pos = PlayerUtil.getRoundedBlockPos(
                mc.player.getX(), posY, mc.player.getZ());
        for (final Direction direction : Direction.values())
        {
            final BlockPos neighbor = pos.offset(direction);
            if (!mc.world.getBlockState(neighbor).isReplaceable())
            {
                return BlockData.basic(neighbor, direction.getOpposite());
            }
        }
        for (final Direction direction : Direction.values())
        {
            final BlockPos neighbor = pos.offset(direction);
            if (mc.world.getBlockState(neighbor).isReplaceable())
            {
                for (final Direction direction1 : Direction.values())
                {
                    final BlockPos neighbor1 = neighbor.offset(direction1);
                    if (!mc.world.getBlockState(neighbor1).isReplaceable())
                    {
                        return BlockData.basic(neighbor1, direction1.getOpposite());
                    }
                }
            }
        }
        return null;
    }

    private static class BlockData
    {
        private BlockHitResult hitResult;
        private float[] angles;

        public BlockData(final BlockHitResult hitResult, final float[] angles)
        {
            this.hitResult = hitResult;
            this.angles = angles;
        }

        public BlockHitResult getHitResult()
        {
            return hitResult;
        }

        public void setHitResult(BlockHitResult hitResult)
        {
            this.hitResult = hitResult;
        }

        public float[] getAngles()
        {
            return angles;
        }

        public void setAngles(float[] angles)
        {
            this.angles = angles;
        }

        public static BlockData basic(final BlockPos pos, final Direction direction)
        {
            return new BlockData(new BlockHitResult(pos.toCenterPos(), direction, pos, false), null);
        }
    }

    private enum Mode
    {
        VANILLA, NCP, GRIM
    }
}
