package net.shoreline.client.impl.mining;

import lombok.experimental.UtilityClass;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.shoreline.client.util.item.EnchantUtil;

@UtilityClass
public class MiningUtil
{
    public boolean isUnbreakable(BlockState state)
    {
        return state.getBlock().getHardness() == -1.0f;
    }

    public boolean isEmpty(BlockState state)
    {
        return state.isAir() || !state.getFluidState().isEmpty();
    }

    public boolean canMineBlock(BlockState state)
    {
        return !isUnbreakable(state) && !isEmpty(state);
    }

    public boolean canHarvest(ItemStack miningStack, BlockState state)
    {
        return !state.isToolRequired() || miningStack.isSuitableFor(state);
    }

    public float getBlockBreakingSpeed(PlayerEntity player, ItemStack miningStack, BlockState block)
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
}
