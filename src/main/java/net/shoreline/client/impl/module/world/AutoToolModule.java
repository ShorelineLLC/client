package net.shoreline.client.impl.module.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.AttackBlockEvent;
import net.shoreline.client.util.player.EnchantmentUtil;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author xgraza
 * @since 1.0
 */
public final class AutoToolModule extends ToggleModule
{
    private static AutoToolModule INSTANCE;

    public AutoToolModule()
    {
        super("AutoTool", "Automatically switches to a tool before mining", ModuleCategory.WORLD);
        INSTANCE = this;
    }

    public static AutoToolModule getInstance()
    {
        return INSTANCE;
    }

    @EventListener
    public void onBreakBlock(final AttackBlockEvent event)
    {
        final BlockState state = mc.world.getBlockState(event.getPos());
        final int blockSlot = getBestToolNoFallback(state);
        if (blockSlot != -1)
        {
            mc.player.getInventory().selectedSlot = blockSlot;
        }
    }

    public int getBestTool(final BlockState state)
    {
        int slot = getBestToolNoFallback(state);
        if (slot != -1)
        {
            return slot;
        }
        return mc.player.getInventory().selectedSlot;
    }

    public int getBestToolNoFallback(final BlockState state)
    {
        if (state.getBlock() == Blocks.COBWEB)
        {
            for (int i = 0; i < 9; i++)
            {
                final ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.isEmpty() || !(stack.getItem() instanceof SwordItem))
                {
                    continue;
                }
                return i;
            }
        }
        int slot = -1;
        float bestTool = 0.0f;
        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof ToolItem))
            {
                continue;
            }
            float speed = stack.getMiningSpeedMultiplier(state);
            final int efficiency = EnchantmentUtil.getLevel(stack, Enchantments.EFFICIENCY);
            if (efficiency > 0)
            {
                speed += efficiency * efficiency + 1.0f;
            }
            if (speed > bestTool)
            {
                bestTool = speed;
                slot = i;
            }
        }
        return slot;
    }
}
