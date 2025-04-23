package net.shoreline.client.impl.module.world;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BlockListConfig;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.BlockPlacerModule;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.math.position.PositionUtil;
import net.shoreline.client.util.player.MovementUtil;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xgraza, Shoreline
 * @since 1.0
 */
public final class ScaffoldModule extends BlockPlacerModule
{
    Config<Boolean> rotateHoldConfig = register(new BooleanConfig("RotateHold", "Holds rotations to scaffold blocks", false, () -> rotateConfig.getValue()));
    Config<Boolean> grimConfig = register(new BooleanConfig("Grim", "Uses grim interactions", false));
    Config<Boolean> grimNewConfig = register(new BooleanConfig("GrimV3", "Uses grim new interactions", false));
    Config<Selection> selectionConfig = register(new EnumConfig<>("Selection", "The selection of blocks to use for scaffold", Selection.ALL, Selection.values()));
    Config<List<Block>> whitelistConfig = register(new BlockListConfig<>("Whitelist", "Valid block whitelist", Blocks.DIRT, Blocks.OBSIDIAN));
    Config<List<Block>> blacklistConfig = register(new BlockListConfig<>("Blacklist", "Valid block blacklist", Blocks.SHULKER_BOX));
    Config<Boolean> keepYConfig = register(new BooleanConfig("KeepY", "Keeps the same y-level", false));
    Config<Boolean> towerConfig = register(new BooleanConfig("Tower", "Goes up faster when holding down space", true, () -> !grimNewConfig.getValue()));
    Config<BlockPicker> pickerConfig = register(new EnumConfig<>("BlockSelection", "How to pick a block from the hotbar", BlockPicker.NORMAL, BlockPicker.values()));
    Config<Boolean> renderConfig = register(new BooleanConfig("Render", "Renders where scaffold is placing blocks", false));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Timer for the fade", 0, 250, 1000, () -> false));

    private final Map<BlockPos, Animation> fadeList = new HashMap<>();
    private BlockData blockData;
    private BlockData renderData;
    private float[] lastAngles;
    private int groundPosY;

    public ScaffoldModule()
    {
        super("Scaffold", "Places blocks at the players feet", ModuleCategory.WORLD, 790);
    }

    @Override
    protected void onDisable()
    {
        if (mc.player != null)
        {
            Managers.INVENTORY.syncToClient();
        }
        groundPosY = -1;
        lastAngles = null;
        blockData = null;
        renderData = null;
        fadeList.clear();
    }

    @EventListener
    public void onPlayerTick(final PlayerTickEvent event)
    {

        if (!multitaskConfig.getValue() && checkMultitask())
        {
            blockData = null;
            renderData = null;
            return;
        }

        int slot = getBlockSlot();
        if (slot == -1)
        {
            blockData = null;
            renderData = null;
            return;
        }
        renderData = getBlockData(false);
        blockData = getBlockData(rotateHoldConfig.getValue());
        if (blockData == null)
        {
            if (grimNewConfig.getValue() && rotateConfig.getValue())
            {
                float yaw = mc.player.getYaw();
                if (mc.options.forwardKey.isPressed() && !mc.options.backKey.isPressed())
                {
                    if (mc.options.leftKey.isPressed() && !mc.options.rightKey.isPressed())
                    {
                        yaw -= 45.0f;
                    }
                    else if (mc.options.rightKey.isPressed() && !mc.options.leftKey.isPressed())
                    {
                        yaw += 45.0f;
                    }
                    // Forward movement - no change to yaw
                }
                else if (mc.options.backKey.isPressed() && !mc.options.forwardKey.isPressed())
                {
                    yaw += 180.0f;
                    if (mc.options.leftKey.isPressed() && !mc.options.rightKey.isPressed())
                    {
                        yaw += 45.0f;
                    }
                    else if (mc.options.rightKey.isPressed() && !mc.options.leftKey.isPressed())
                    {
                        yaw -= 45.0f;
                    }
                }
                else if (mc.options.leftKey.isPressed() && !mc.options.rightKey.isPressed())
                {
                    yaw -= 90.0f;
                }
                else if (mc.options.rightKey.isPressed() && !mc.options.leftKey.isPressed())
                {
                    yaw += 90.0f;
                }
                setRotation(MathHelper.wrapDegrees(yaw), 90.0f);
            }
            return;
        }

        calcRotations(blockData);
        if (blockData.getAngles() == null)
        {
            if (!isGrim() && rotateConfig.getValue() && lastAngles != null)
            {
                setRotation(lastAngles[0], lastAngles[1]);
            }
            return;
        }

        if (!isGrim() && Managers.INVENTORY.getServerSlot() != slot)
        {
            Managers.INVENTORY.setSlot(slot);
        }
        boolean result = Managers.INTERACT.placeBlock(blockData.getBlockPos(), slot, false, false, false, (state, angles) ->
        {
            if (rotateConfig.getValue())
            {
                final float[] rotations = blockData.getAngles();
                if (rotations == null)
                {
                    return;
                }
                lastAngles = rotations;
                if (state)
                {
                    if (grimConfig.getValue())
                    {
                        Managers.ROTATION.setRotationSilent(rotations[0], rotations[1]);
                    }
                    else
                    {
                        setRotation(rotations[0], rotations[1]);
                    }
                }
                else
                {
                    if (grimConfig.getValue())
                    {
                        Managers.ROTATION.setRotationSilentSync();
                    }
                }
            }
        });
        if (result)
        {
            if (!isGrim() && towerConfig.getValue() && mc.options.jumpKey.isPressed())
            {
                final Vec3d velocity = mc.player.getVelocity();
                final double velocityY = velocity.y;
                if ((mc.player.isOnGround() || velocityY < 0.1) || velocityY <= 0.16477328182606651)
                {
                    mc.player.setVelocity(velocity.x, 0.42f, velocity.z);
                }
            }
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (renderConfig.getValue())
        {
            RenderBuffers.preRender();
            for (Map.Entry<BlockPos, Animation> set : fadeList.entrySet())
            {
                set.getValue().setState(false);
                int boxAlpha = (int) (40 * set.getValue().getFactor());
                int lineAlpha = (int) (100 * set.getValue().getFactor());
                Color boxColor = ColorsModule.getInstance().getColor(boxAlpha);
                Color lineColor = ColorsModule.getInstance().getColor(lineAlpha);
                RenderManager.renderBox(event.getMatrices(), set.getKey(), boxColor.getRGB());
                RenderManager.renderBoundingBox(event.getMatrices(), set.getKey(), 1.5f, lineColor.getRGB());
            }
            RenderBuffers.postRender();

            if (renderData == null || renderData.getHitResult() == null)
            {
                return;
            }

            if (renderConfig.getValue())
            {
                Animation animation = new Animation(true, fadeTimeConfig.getValue());
                fadeList.put(renderData.getBlockPos(), animation);
            }

            fadeList.entrySet().removeIf(e ->
                    e.getValue().getFactor() == 0.0);
        }
    }

    private void calcRotations(final BlockData blockData)
    {
        final BlockPos pos = blockData.getHitResult().getBlockPos();
        final Direction side = blockData.getHitResult().getSide();
        final Vec3d basicHitVec = pos.toCenterPos()
                .add(side.getOffsetX() * 0.5f, side.getOffsetY() * 0.5f, side.getOffsetZ() * 0.5f);
        blockData.setAngles(RotationUtil.getRotationsTo(mc.player.getEyePos(), basicHitVec));
        blockData.setHitResult(new BlockHitResult(basicHitVec, side, pos, false));
    }

    private BlockData getBlockData(boolean hold)
    {
        int posY = (int) Math.round(mc.player.getY()) - 1;
        if (keepYConfig.getValue() && MovementUtil.isInputtingMovement())
        {
            if (mc.player.isOnGround() || groundPosY == -1)
            {
                groundPosY = (int) Math.floor(mc.player.getY()) - 1;
            }
            posY = groundPosY;
        }
        final BlockPos pos = PositionUtil.getRoundedBlockPos(
                mc.player.getX(), posY, mc.player.getZ());
        if (!hold && !mc.world.getBlockState(pos).isReplaceable())
        {
            return null;
        }
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

    private int getBlockSlot()
    {
        final ItemStack serverStack = Managers.INVENTORY.getServerItem();
        if (!serverStack.isEmpty() && serverStack.getItem() instanceof BlockItem blockItem && validScaffoldBlock(blockItem.getBlock()))
        {
            return Managers.INVENTORY.getServerSlot();
        }

        int blockSlot = -1;
        int count = 0;
        for (int i = 0; i < 9; ++i)
        {
            final ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem blockItem && validScaffoldBlock(blockItem.getBlock()))
            {
                if (pickerConfig.getValue() == BlockPicker.NORMAL)
                {
                    return i;
                }

                if (blockSlot == -1 || itemStack.getCount() > count)
                {
                    blockSlot = i;
                    count = itemStack.getCount();
                }
            }
        }

        return blockSlot;
    }

    private boolean validScaffoldBlock(Block block)
    {
        return switch (selectionConfig.getValue())
        {
            case WHITELIST -> ((BlockListConfig<?>) whitelistConfig).contains(block);
            case BLACKLIST -> !((BlockListConfig<?>) blacklistConfig).contains(block);
            case ALL -> true;
        };
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

        public BlockPos getBlockPos()
        {
            return hitResult.getBlockPos().offset(hitResult.getSide());
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

    public boolean isGrim()
    {
        return grimConfig.getValue() || grimNewConfig.getValue();
    }

    public enum Selection
    {
        WHITELIST,
        BLACKLIST,
        ALL
    }

    private enum BlockPicker
    {
        NORMAL,
        GREATEST
    }
}
