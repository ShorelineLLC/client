package net.shoreline.client.impl.mining;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShapes;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.BoxRender;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;

@Builder
@Data
public class MiningData
{
    @Builder.Default
    private final PlayerEntity player = MinecraftClient.getInstance().player;

    private final BlockPos blockPos;
    private final Direction direction;

    private final float maxProgress;

    @EqualsAndHashCode.Exclude
    private final ItemStack miningStack;

    @EqualsAndHashCode.Exclude
    private transient float blockDamage, lastDamage;

    @EqualsAndHashCode.Exclude
    private transient int ticksMining;

    @Builder.Default
    @EqualsAndHashCode.Exclude
    private final Animation fadeOutAnim = new Animation(true, 300L);

    public float tickDelta()
    {
        this.lastDamage = blockDamage;
        this.blockDamage += getBlockBreakingDelta();

        if (blockDamage >= maxProgress)
        {
            ticksMining++;
        }

        return blockDamage;
    }

    public void resetTicksMining()
    {
        ticksMining = 0;
    }

    public void render(MatrixStack matrixStack,
                       float tickDelta,
                       int startColor,
                       int endColor)
    {
        render(matrixStack, tickDelta, startColor, endColor, maxProgress);
    }

    public void render(MatrixStack matrixStack,
                       float tickDelta,
                       int startColor,
                       int endColor,
                       float miningSpeed)
    {
        if (fadeOutAnim.getFactor() < 0.01)
        {
            return;
        }

        final BlockState state = getBlockState();
        Box fullBox = VoxelShapes.fullCube().getBoundingBox();

        double scale;
        Box outlineShape;
        if (isBlockMined() || hasMinedFor(30))
        {
            scale = 1.0;
            outlineShape = fullBox;
            fadeOutAnim.setState(false);
        } else
        {
            scale = Easing.SMOOTH_STEP.ease(getLinearScale(miningSpeed, tickDelta));
            outlineShape = state.getOutlineShape(MinecraftClient.getInstance().world, blockPos).getBoundingBox();
        }

        int color = ColorUtil.interpolateColor(Math.min(blockDamage, 1.0f), endColor, startColor);
        Vec3d center = outlineShape.offset(blockPos).getCenter();

        double dx = (outlineShape.maxX - outlineShape.minX) * scale;
        double dy = (outlineShape.maxY - outlineShape.minY) * scale;
        double dz = (outlineShape.maxZ - outlineShape.minZ) * scale;
        Box scaled = Box.of(center, dx, dy, dz);

        BoxRender.FILL.render(matrixStack, scaled, color, (float) fadeOutAnim.getFactor());
    }

    private float getLinearScale(float maxProgress, float tickDelta)
    {
        return MathHelper.clamp((blockDamage + (blockDamage - lastDamage) * tickDelta) / maxProgress, 0.0f, 1.0f);
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

    public boolean isBlockMined()
    {
        return blockDamage >= maxProgress && !MiningUtil.canMineBlock(getBlockState());
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
