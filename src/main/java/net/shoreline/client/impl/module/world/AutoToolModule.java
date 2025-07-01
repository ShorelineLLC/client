package net.shoreline.client.impl.module.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.network.AttackBlockEvent;
import net.shoreline.client.util.item.EnchantUtil;
import net.shoreline.client.util.item.ItemUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class AutoToolModule extends Toggleable
{
    public static AutoToolModule INSTANCE;

    public AutoToolModule()
    {
        super("AutoTool", "Automatically switches to a tool before mining", GuiCategory.WORLD);
        INSTANCE = this;
    }

    @EventListener
    public void onAttackBlock(AttackBlockEvent event)
    {
        int blockSlot = getBestToolNoFallback(event.getState());
        if (blockSlot != -1)
        {
            mc.player.getInventory().setSelectedSlot(blockSlot);
        }
    }

    public int getBestTool(final BlockState state)
    {
        int slot = getBestToolNoFallback(state);
        return slot != -1 ? slot : mc.player.getInventory().getSelectedSlot();
    }

    public int getBestToolNoFallback(final BlockState state)
    {
        if (state.getBlock() == Blocks.COBWEB)
        {
            for (int i = 0; i < 9; i++)
            {
                final ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.isEmpty() || !ItemUtil.isSword(stack.getItem()))
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
            if (stack.isEmpty() || !ItemUtil.isTool(stack.getItem()))
            {
                continue;
            }
            float speed = stack.getMiningSpeedMultiplier(state);
            final int efficiency = EnchantUtil.getLevel(Enchantments.EFFICIENCY, stack);
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
