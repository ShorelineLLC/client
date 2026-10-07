package net.shoreline.client.impl.mining;

import lombok.*;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.network.NetworkHandler;
import net.shoreline.client.impl.render.BoxRender;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Getter
@Setter
public class MiningData
{
    @Builder.Default
    private final PlayerEntity player = MinecraftClient.getInstance().player;

    @EqualsAndHashCode.Include
    private final BlockPos blockPos;
    private final Direction direction;

    private boolean started;
    private final float maxProgress;
    private final ItemStack miningStack;

    private float blockDamage, lastDamage;

    private int ticksMining;

    public void abort(NetworkHandler handler)
    {
        handler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, direction));
    }

    public float tickDelta()
    {
        return tickDelta(false);
    }

    public float tickDelta(boolean isMultitasking)
    {
        this.lastDamage = blockDamage;
        if (isDoneMining())
        {
            if (!isMultitasking)
            {
                ticksMining++;
            }

            return blockDamage;
        }

        this.blockDamage += getBlockBreakingDelta();
        return blockDamage;
    }

    public void resetTicksMining()
    {
        ticksMining = 0;
    }

    public void render(MatrixStack matrixStack,
                       float tickDelta,
                       BoxRender boxRender,
                       int startColor,
                       int endColor,
                       float alpha)
    {
        render(matrixStack, tickDelta, boxRender, startColor, endColor, alpha, maxProgress);
    }

    public void render(MatrixStack matrixStack,
                       float tickDelta,
                       BoxRender boxRender,
                       int startColor,
                       int endColor,
                       float alpha,
                       float miningSpeed)
    {
        final BlockState state = getBlockState();

        VoxelShape outlineShape = state.getOutlineShape(MinecraftClient.getInstance().world, blockPos);
        Box boundingBox = outlineShape != null && !outlineShape.isEmpty() ? outlineShape.getBoundingBox() : VoxelShapes.fullCube().getBoundingBox();
        double scale = isDoneMining() ? 1.0 : Easing.SMOOTH_STEP.ease(getLinearScale(miningSpeed, tickDelta));

        int color = ColorUtil.interpolateColor(Math.min(blockDamage / miningSpeed, 1.0f), endColor, startColor);
        Vec3d center = boundingBox.offset(blockPos).getCenter();

        double dx = (boundingBox.maxX - boundingBox.minX) * scale;
        double dy = (boundingBox.maxY - boundingBox.minY) * scale;
        double dz = (boundingBox.maxZ - boundingBox.minZ) * scale;
        Box scaled = Box.of(center, dx, dy, dz);

        boxRender.render(matrixStack, scaled, color, alpha);
    }

    private float getLinearScale(float maxProgress, float tickDelta)
    {
        return MathHelper.clamp((blockDamage + (blockDamage - lastDamage) * tickDelta) / (float) Math.max(0.001, maxProgress), 0.0f, 1.0f);
    }

    public float getProgress()
    {
        return MathHelper.clamp(blockDamage / (float) Math.max(0.001, maxProgress), 0.0f, 1.0f);
    }

    public double getSquaredDistanceTo()
    {
        return player.squaredDistanceTo(blockPos.toCenterPos());
    }

    public BlockState getBlockState()
    {
        return MinecraftClient.getInstance().world.getBlockState(blockPos);
    }

    public float getBlockBreakingDelta()
    {
        BlockState state = getBlockState();
        float f = state.getHardness(MinecraftClient.getInstance().world, blockPos);
        if (f == -1.0f)
        {
            return 0.0f;
        }

        int i = MiningUtil.canHarvest(miningStack, state) ? 30 : 100;
        return MiningUtil.getBlockBreakingSpeed(player, miningStack, state) / f / (float) i;
    }

    public boolean isDoneMining()
    {
        return blockDamage >= maxProgress;
    }

    public boolean isAlmostDone(int ticks)
    {
        return !isBlockMined() && blockDamage + (ticks * getBlockBreakingDelta()) >= maxProgress;
    }

    public boolean isBlockMined()
    {
        return isDoneMining() && isAir();
    }

    public boolean isAir()
    {
        return !MiningUtil.canMineBlock(getBlockState());
    }

    public boolean hasMinedFor(int ticksMining)
    {
        return this.ticksMining >= ticksMining;
    }

    public MiningData copy(float maxProgress)
    {
        return MiningData.builder()
                .player(this.player)
                .blockPos(this.blockPos)
                .direction(this.direction)
                .maxProgress(maxProgress)
                .miningStack(this.miningStack.copy())
                .blockDamage(this.blockDamage)
                .lastDamage(this.lastDamage)
                .ticksMining(this.ticksMining)
                .build();
    }
}
