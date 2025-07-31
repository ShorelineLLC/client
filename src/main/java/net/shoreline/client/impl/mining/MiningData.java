package net.shoreline.client.impl.mining;

import lombok.Builder;
import lombok.Data;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.shoreline.client.impl.render.BoxRender;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.util.item.EnchantUtil;

@Builder
@Data
public class MiningData
{
    private final PlayerEntity player;

    private final BlockPos blockPos;
    private final Direction direction;

    private final ItemStack miningStack;

    private float blockDamage, lastDamage;

    public float tickBlockDamage()
    {
        this.lastDamage = blockDamage;
        return blockDamage += getBlockBreakingDelta();
    }

    public double squaredDistanceTo()
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
        int i = canHarvest(state) ? 30 : 100;
        return getBlockBreakingSpeed(state) / f / (float)i;
    }

    private float getBlockBreakingSpeed(BlockState block)
    {
        float f = miningStack.getMiningSpeedMultiplier(block);
        if (f > 1.0f)
        {
            int lvl = EnchantUtil.getLevel(Enchantments.EFFICIENCY, miningStack);
            f += (float) lvl * lvl;
        }
        if (StatusEffectUtil.hasHaste(player))
        {
            f *= 1.0f + (float) (StatusEffectUtil.getHasteAmplifier(player) + 1) * 0.2f;
        }

        if (player.hasStatusEffect(StatusEffects.MINING_FATIGUE))
        {
            float g = switch (player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier())
            {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1E-4f;
            };
            f *= g;
        }
        f *= (float) player.getAttributeValue(EntityAttributes.BLOCK_BREAK_SPEED);
        if (!player.isOnGround())
        {
            f /= 5.0f;
        }
        return f;
    }

    private boolean canHarvest(BlockState state)
    {
        return !state.isToolRequired() || miningStack.isSuitableFor(state);
    }

    public void render(MatrixStack matrixStack,
                       float tickDelta,
                       float miningSpeed,
                       int startColor,
                       int endColor)
    {
        VoxelShape outlineShape = VoxelShapes.fullCube();
        if (blockDamage < miningSpeed)
        {
            outlineShape = getBlockState().getOutlineShape(MinecraftClient.getInstance().world, blockPos);
            outlineShape = outlineShape.isEmpty() ? VoxelShapes.fullCube() : outlineShape;
        }

        int color = ColorUtil.interpolateColor(Math.min(blockDamage, 1.0f), startColor, endColor);
        Box render1 = outlineShape.getBoundingBox();
        Vec3d center = render1.offset(blockPos).getCenter();
        double scale = Easing.SMOOTH_STEP.ease(getLinearScale(miningSpeed, tickDelta));

        double dx = (render1.maxX - render1.minX) * scale;
        double dy = (render1.maxY - render1.minY) * scale;
        double dz = (render1.maxZ - render1.minZ) * scale;
        Box scaled = Box.of(center, dx, dy, dz);

        BoxRender.FILL.render(matrixStack, scaled, color);
    }

    public float getLinearScale(float maxProgress, float tickDelta)
    {
        return MathHelper.clamp((blockDamage + (blockDamage - lastDamage) * tickDelta) / maxProgress, 0.0f, 1.0f);
    }
}
